package net.atomshare.cattorrent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Downloader {


    public Downloader(Metainfo metainfo) {
        this.metainfo = metainfo;
    }

    public static void main(String[] args) throws IOException {
        // code only for testing, will be refactored later
        Metainfo metainfo = new Metainfo(args[0]);
        Downloader d = new Downloader(metainfo);
        d.run();
    }

    private Metainfo metainfo;
    private List<List<ByteString>> pieces = new ArrayList<>();
    private int chunksLeft = 0;
    private PeerConnection peerConnection;

    final int CHUNK_LENGTH = 16000;

    /**
     * .
     */
    public void run() throws IOException {
        peerConnection = new PeerConnection(metainfo);
        peerConnection.init();

        initChunks();

        for (int j = 0; j < pieces.size(); j++) {
            requestPiece(j);
        }

        while (true) {
            PeerConnection.Message msg = peerConnection.readMessage();
            if (msg.kind == PeerConnection.Message.PIECE) {
                pieces.get(msg.index).set(msg.begin/CHUNK_LENGTH, new ByteString(msg.body));
                //System.out.println(msg.toString());
                chunksLeft --;
                if(chunksLeft == 0) break;
            } else {
                System.out.println("Sth else");
            }
        }

        for (int j = 0; j < pieces.size(); j++) {
            checkPiece(j);
        }

        FileOutputStream file = new FileOutputStream("a.txt");

        for (int j = 0; j < pieces.size(); j++) {
            for (int i = 0; i < pieces.get(i).size(); i++) {
                file.write(pieces.get(j).get(i).getBytes());
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
    private void requestPiece(int pieceIndex) throws IOException {
        for (int i = 0; i < pieces.get(pieceIndex).size(); i++) {
            int maxLength1 = metainfo.getLength() - pieceIndex * metainfo.getPieceLength() - i * CHUNK_LENGTH;
            int maxLength2 = metainfo.getPieceLength() - i * CHUNK_LENGTH;
            peerConnection.sendRequest(pieceIndex, i * CHUNK_LENGTH, Math.min(CHUNK_LENGTH, Math.min(maxLength1, maxLength2)));
            chunksLeft++;
        }
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
                int lastLength = metainfo.getPieceLength() * pieceCount - metainfo.getLength();
                if (lastLength != 0) {
                    count = (lastLength + CHUNK_LENGTH - 1) / CHUNK_LENGTH;
                }
            }
            ArrayList<ByteString> l = new ArrayList<ByteString>();
            for (int j = 0; j < count; j++) l.add(null);
            pieces.add(l);
        }
    }
}
