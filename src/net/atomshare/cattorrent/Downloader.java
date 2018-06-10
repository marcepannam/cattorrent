package net.atomshare.cattorrent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Downloader implements Runnable {

    public interface DownloadProgressListener {
        /**
         * Report download progress.
         *
         * @param p Progress (from 0 to 1)
         */
        void onProgress(float p);

        /**
         * Log a message.
         */
        void onLog(String message);
    }

    public Downloader(Metainfo metainfo, String targetFile, DownloadProgressListener listener) throws IOException {
        this.metainfo = metainfo;
        this.listener = listener;
        this.chunker = new FileChunker(targetFile, metainfo);
    }

    public static void main(String[] args) throws IOException {
        Metainfo metainfo = new Metainfo(args[0]);
        Downloader d = new Downloader(metainfo, args[1], new DownloadProgressListener() {
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
    }

    private FileChunker chunker;
    private DownloadProgressListener listener;
    private Metainfo metainfo;
    private List<List<Boolean>> pieces = new ArrayList<>(); // which chunks are already downloaded?
    private List<PeerConnection> peers = new ArrayList<>();
    private int chunksLeft = 0;
    private int allChunkCount = 0;

    /**
     * Run download.
     */
    public void run() {
        try {
            initChunks();
            checkPieces();
            tellProgress();

            List<ByteString> connectedPeers = new ArrayList<>();

            List<byte[]> peersInfos;
            while (true) {

                try {
                    peersInfos = requestPeersFromTracker();
                } catch (Exception ex) {
                    Thread.sleep(5000);
                    continue;
                }

                for (byte[] info : peersInfos) {
                    if(connectedPeers.contains(new ByteString(info))) continue;
                    PeerConnection peer = new PeerConnection(metainfo, info, listener);
                    peerStart(peer);
                    peers.add(peer);
                    connectedPeers.add(new ByteString(info));
                }

                Thread.sleep(30000);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void tellProgress() {
        listener.onProgress((float) chunksLeft / allChunkCount);
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

    private void peerRun(PeerConnection peer) throws IOException {
        peer.init();

        int pendingRequests = 0;

        List<Boolean> bitmap = new ArrayList<>();
        for (int i=0; i < pieces.size(); i ++) {
            bitmap.add(isPieceComplete(i));
        }
        peer.sendBitmap(bitmap);
        peer.sendUnchoke();

        while (true) {
            //System.out.println("left " + chunksLeft);
            PeerConnection.Message msg = peer.readMessage();

            if (msg.kind == PeerConnection.Message.PIECE) {
                synchronized (this) {
                    if (!pieces.get(msg.index).get(msg.begin / FileChunker.CHUNK_LENGTH)) {
                        pieces.get(msg.index).set(msg.begin / FileChunker.CHUNK_LENGTH, true);
                        chunker.write(msg.index, msg.begin, new ByteString(msg.body));
                        chunksLeft--;
                        tellProgress();
                        if (chunksLeft == 0) break;
                    } else {
                        System.out.println("duplicate chunk " + msg.index + " " + msg.begin);
                    }
                }
                pendingRequests--;
            }

            if (msg.kind == PeerConnection.Message.REQUEST) {
                ByteString s = chunker.read(msg.index, msg.begin, msg.length);
                peer.sendPiece(msg.index, msg.begin, s);
            }

            if (!peer.isChocked()) {
                //System.out.println("REQ:" + pendingRequests);
                while (pendingRequests < 10) {
                    if (requestRandomPiece(peer))
                        pendingRequests++;
                    else
                        break;
                }
            }
        }


    }

    private boolean requestRandomPiece(PeerConnection peer) throws IOException {
        int piece, chunk;
        synchronized (this) {
            if (chunksLeft == 0) return false;

            List<Integer> candidates = new ArrayList<>();

            for (int j = 0; j < pieces.size(); j++) {
                if (!isPieceComplete(j) && peer.hasPiece(j)) {
                    candidates.add(j);
                }
            }

            if (candidates.size() == 0) return false;

            int k = (new Random()).nextInt(candidates.size());
            piece = candidates.get(k);

            List<Integer> chunks = new ArrayList<>();
            for (int i = 0; i < pieces.get(piece).size(); i++) {
                if (!pieces.get(piece).get(i)) {
                    chunks.add(i);
                }
            }

            if(chunks.size() == 0) return false;
            chunk = chunks.get((new Random()).nextInt(chunks.size()));
        }


        requestPiece(peer, piece, chunk);
        return true;
    }

    private boolean isPieceComplete(int j) {
        for (Boolean s : pieces.get(j)) {
            if (!s) return false;
        }
        return true;
    }

    /**
     * Requests peer information from the tracker.
     * Returns 6-byte address for each peer (4 byte of IP, 2 bytes of port).
     */
    private List<byte[]> requestPeersFromTracker() throws IOException {
        listener.onLog("Requesting data from tracker...");
        TrackerRequest tracker_request = new TrackerRequest(metainfo, TrackerRequest.Event.STARTED);

        URL url = new URL(tracker_request.buildBaseUrl());
        byte[] trackerData = TrackerResponse.get(url);
        Object trackerResp = Bencoder.decode(trackerData);

        ByteString peers1 = (ByteString) ((Map) trackerResp).get(new ByteString("peers"));

        byte[] peersArray = peers1.getBytes();
        List<byte[]> peers = new ArrayList<>();
        for (int i = 0; i <= peersArray.length - 6; i += 6) {
            peers.add(Arrays.copyOfRange(peersArray, i, i + 6));
        }
        return peers;
    }

    private synchronized void checkPieces() throws IOException {
        int okCount = 0;
        chunksLeft = 0;
        for (int i=0; i < pieces.size(); i ++) {
            boolean ok = checkPiece(i);
            if (ok) okCount ++;
            for (int j=0; j < pieces.get(i).size(); j ++) {
                pieces.get(i).set(j, ok);
                if (!ok) chunksLeft ++;
            }
        }

        listener.onLog(okCount + "/" + pieces.size() + " of pieces are already downloaded");
    }

    /**
     * Compute hash of a piece and compare to metainfo hash.
     */
    private boolean checkPiece(int pieceIndex) throws IOException {
        int length = Math.min(metainfo.getPieceLength(), metainfo.getLength() - metainfo.getPieceLength() * pieceIndex);
        //System.out.println("len " + length);
        ByteString h = Utils.computeSha1Hash(chunker.read(pieceIndex, 0, length));
        return metainfo.getPieceHash(pieceIndex).equals(h);
    }

    /**
     * Send request for a piece to the peer.
     */
    private void requestPiece(PeerConnection peer, int pieceIndex, int chunkIndex) throws IOException {
        int maxLength1 = metainfo.getLength() - pieceIndex * metainfo.getPieceLength() - chunkIndex * FileChunker.CHUNK_LENGTH;
        int maxLength2 = metainfo.getPieceLength() - chunkIndex * FileChunker.CHUNK_LENGTH;
        int length = Math.min(FileChunker.CHUNK_LENGTH, Math.min(maxLength1, maxLength2));
        if (length <= 0) throw new RuntimeException("bad length " + maxLength1 + " " + maxLength2);

        peer.sendRequest(pieceIndex, chunkIndex * FileChunker.CHUNK_LENGTH, length);
    }

    /**
     * Initializes status array.
     */
    private void initChunks() {
        int pieceCount = (metainfo.getLength() + metainfo.getPieceLength() - 1) / metainfo.getPieceLength();
        int chunkCount = (metainfo.getPieceLength() + FileChunker.CHUNK_LENGTH - 1) / FileChunker.CHUNK_LENGTH;

        for (int i = 0; i < pieceCount; i++) {
            int count = chunkCount;
            if (i == pieceCount - 1) {
                int lastLength = metainfo.getLength() - metainfo.getPieceLength() * (pieceCount - 1);
                if (lastLength == 0) throw new RuntimeException("calculation error");
                count = (lastLength + FileChunker.CHUNK_LENGTH - 1) / FileChunker.CHUNK_LENGTH;
            }

            ArrayList<Boolean> l = new ArrayList<Boolean>();
            for (int j = 0; j < count; j++) {
                l.add(false);
                chunksLeft++;
                allChunkCount++;
            }
            pieces.add(l);
        }
    }
}
