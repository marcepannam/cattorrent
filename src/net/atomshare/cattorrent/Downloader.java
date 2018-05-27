package net.atomshare.cattorrent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class Downloader implements Runnable {

    public interface DownloadProgressListener {
        /**
         * Report download progress.
         *
         * @param p Progress (from 0 to 1)
         */
        void onProgress(float p);

        void onLog(String message);
    }

    public Downloader(Metainfo metainfo, DownloadProgressListener listener) {
        this.metainfo = metainfo;
        this.listener = listener;
    }

    public static void main(String[] args) throws IOException {
        Metainfo metainfo = new Metainfo(args[0]);
        Downloader d = new Downloader(metainfo, new DownloadProgressListener() {
            @Override
            public void onProgress(float p) {
                System.out.println("progress: " + p);
            }

            @Override
            public void onLog(String message) {
                System.out.println("log: " + message);
            }
        });
        d.run();
        //d.saveToFile("a.txt");
        d.saveToFile(args[1]);
    }

    private DownloadProgressListener listener;
    private Metainfo metainfo;
    private List<List<ByteString>> pieces = new ArrayList<>();
    private List<PeerConnection> peers = new ArrayList<>();
    private int chunksLeft = 0;
    private int allChunkCount = 0;

    private final int CHUNK_LENGTH = 16000;

    /**
     * Run download.
     */
    public void run() {
        try {
            List<byte[]> peersInfos = requestPeersFromTracker();
            for (byte[] info : peersInfos) {
                peers.add(new PeerConnection(metainfo, info, listener));
            }
            initChunks();

            for (PeerConnection peer : peers) {
                peerStart(peer);
            }

            while (chunksLeft > 0) {
                Thread.sleep(1000);
                // TODO: make request to tracker


            }

            listener.onLog("download finished");
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printState() {
         System.out.println(chunksLeft);

                System.out.println("+++");
                for (List<ByteString> p : pieces) {
                    for (ByteString a : p)
                        System.out.print((a == null) + " ");
                    //System.out.println();
                }

                System.out.println("---");
    }

    private void peerStart(PeerConnection peer) {
        new Thread(() -> {
            try {
                peerRun(peer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void peerRun(PeerConnection peer) throws IOException {
        peer.init();

        //for (int j = 0; j < pieces.size(); j++) {
        //    for (int i=0; i < pieces.get(j).size(); i ++)
        //        requestPiece(peer, j, i);
        //}

        int pendingRequests = 0;

        while (chunksLeft > 0) {
            System.out.println("left " + chunksLeft);
            PeerConnection.Message msg = peer.readMessage();

            if (msg.kind == PeerConnection.Message.PIECE) {
                synchronized (this) {
                    if (pieces.get(msg.index).get(msg.begin / CHUNK_LENGTH) == null) {
                        pieces.get(msg.index).set(msg.begin / CHUNK_LENGTH, new ByteString(msg.body));
                        chunksLeft--;
                        listener.onProgress((float) chunksLeft / allChunkCount);
                        if (chunksLeft == 0) break;
                    } else {
                        System.out.println("duplicate chunk " + msg.index + " " + msg.begin);
                    }
                }
                pendingRequests --;
            }

            if (!peer.isChocked()) {
                while (pendingRequests < 10) {
                    if (requestRandomPiece(peer))
                        pendingRequests ++;
                    else
                        break;
                }
            }
        }


    }

    private synchronized boolean requestRandomPiece(PeerConnection peer) throws IOException {
        if (chunksLeft == 0) return false;

        List<Integer> candidates = new ArrayList<>();

        for (int j = 0; j < pieces.size(); j++) {
            if (!isPieceComplete(j) && peer.hasPiece(j)) {
                candidates.add(j);
            }
        }

        if(candidates.size() == 0) return false;

        int k = (new Random()).nextInt(candidates.size());
        int piece = candidates.get(k);
        int chunk = 0;

        for (int i=0; i < pieces.get(piece).size(); i ++) {
            if (pieces.get(piece).get(i) == null) {
                chunk = i;
                break;
            }
        }

        requestPiece(peer, piece, chunk);
        return true;
    }

    private boolean isPieceComplete(int j) {
        for (ByteString s : pieces.get(j)) {
            if (s == null) return false;
        }
        return true;
    }

    public List<byte[]> requestPeersFromTracker() {
        listener.onLog("Building tracker request...");
        TrackerRequest tracker_request = new TrackerRequest(metainfo, TrackerRequest.Event.STARTED);
        Object trackerResp;
        URL url;
        try {
            url = new URL(tracker_request.buildBaseUrl());
        } catch (IOException e) {
            listener.onLog("Aborting, unable to build baseURL");
            return null;
        }
        byte[] trackerData;
        try {
            trackerData = TrackerResponse.get(url);
        } catch (IOException e) {
            listener.onLog("Aborting, error while getting tracker response");
            return null;
        }
        try {
            trackerResp = Bencoder.decode(trackerData);
        } catch (IOException e) {
            listener.onLog("Aborting, unable to decode tracker response");
            return null;
        }

        ByteString peers1 = (ByteString) ((Map<Object, Object>) trackerResp).get(new ByteString("peers"));

        byte[] peersArray = peers1.getBytes();
        List<byte[]> peers = new ArrayList<>();
        for (int i = 0; i <= peersArray.length - 6; i += 6) {
            peers.add(Arrays.copyOfRange(peersArray, i, i + 6));
        }
        return peers;
    }

    public void saveToFile(String path) throws IOException {
        for (int j = 0; j < pieces.size(); j++) {
            checkPiece(j);
        }

        FileOutputStream file = new FileOutputStream(path);

        for (List<ByteString> piece : pieces) {
            for (ByteString chunk : piece) {
                file.write(chunk.getBytes());
            }
        }

        file.close();
    }

    /**
     * Compute hash of a piece and compare to metainfo hash.
     */
    private void checkPiece(int pieceIndex) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();

        System.out.println("check " + pieceIndex + " " + pieces.get(pieceIndex).size());
        for (ByteString b : pieces.get(pieceIndex)) {
            o.write(b.getBytes());
        }

        ByteString h = Utils.computeSha1Hash(new ByteString(o.toByteArray()));
        if (metainfo.getPieceHash(pieceIndex).equals(h)) {
            System.out.println("piece " + pieceIndex + " ok");
        } else {
            System.out.println("piece " + pieceIndex + " FAILED (length: " + o.toByteArray().length + " " + metainfo.getPieceLength() + ")");
        }
    }

    /**
     * Send request for a piece to the peer.
     */
    private void requestPiece(PeerConnection peer, int pieceIndex, int chunkIndex) throws IOException {
        int maxLength1 = metainfo.getLength() - pieceIndex * metainfo.getPieceLength() - chunkIndex * CHUNK_LENGTH;
        int maxLength2 = metainfo.getPieceLength() - chunkIndex * CHUNK_LENGTH;
        int length = Math.min(CHUNK_LENGTH, Math.min(maxLength1, maxLength2));
        if (length <= 0) throw new RuntimeException("bad length " + maxLength1 + " " + maxLength2);

        peer.sendRequest(pieceIndex, chunkIndex * CHUNK_LENGTH, length);
    }

    /**
     * Initializes array when file data will be kept.
     */
    private void initChunks() {
        int pieceCount = (metainfo.getLength() + metainfo.getPieceLength() - 1) / metainfo.getPieceLength();
        int chunkCount = (metainfo.getPieceLength() + CHUNK_LENGTH - 1) / CHUNK_LENGTH;

        for (int i = 0; i < pieceCount; i++) {
            int count = chunkCount;
            if (i == pieceCount - 1) {
                int lastLength = metainfo.getLength() - metainfo.getPieceLength() * (pieceCount-1);
                if (lastLength == 0) throw new RuntimeException("calculation error");
                count = (lastLength + CHUNK_LENGTH - 1) / CHUNK_LENGTH;
            }
            ArrayList<ByteString> l = new ArrayList<ByteString>();
            for (int j = 0; j < count; j++) {
                l.add(null);
                chunksLeft++;
                allChunkCount++;
            }
            pieces.add(l);
        }
    }
}
