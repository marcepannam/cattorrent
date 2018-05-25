package net.atomshare.cattorrent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.UIManager.put;

public class Main {

    public static void main(String[] args) throws IOException {
        // code only for testing, will be refactored later

        Metainfo metainfo = new Metainfo(args[0]);
        Downloader d = new Downloader(metainfo);
        d.init();

        final int CHUNK_LENGTH = 16000;

        int pieceCount = (metainfo.getLength() + metainfo.getPieceLength() - 1) / metainfo.getPieceLength();
        int chunkCount = (metainfo.getPieceLength() + CHUNK_LENGTH - 1) / CHUNK_LENGTH;

        List<List<ByteString>> pieces = new ArrayList<>();
        int counter = 0;

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

        for (int j = 0; j < pieceCount; j++) {
            for (int i = 0; i < pieces.get(j).size(); i++) {
                int length = CHUNK_LENGTH;
                int maxLength1 = metainfo.getLength() - j * metainfo.getPieceLength() - i * CHUNK_LENGTH;
                int maxLength2 = metainfo.getPieceLength() - i * CHUNK_LENGTH;
                d.sendRequest(j, i * CHUNK_LENGTH, Math.min(length, Math.min(maxLength1, maxLength2)));
                counter++;
            }
        }

        while (true) {
            Downloader.Message msg = d.readMessage();
            if (msg.kind == Downloader.Message.PIECE) {
                pieces.get(msg.index).set(msg.begin/CHUNK_LENGTH, new ByteString(msg.body));
                //System.out.println(msg.toString());
                counter --;
                if(counter == 0) break;
            } else {
                System.out.println("Sth else");
            }
        }

        for (int j = 0; j < pieceCount; j++) {
            ByteArrayOutputStream o = new ByteArrayOutputStream();

            System.out.println("check " + j + " " + pieces.get(j).size());
            for (ByteString b : pieces.get(j)) {
                o.write(b.getBytes());
            }

            ByteString h = Utils.computeSha1Hash(new ByteString(o.toByteArray()));
            if (metainfo.getPieceHash(j).equals(h)) {
                System.out.println("piece " + j + " ok");
            } else {
                System.out.println("piece " + j + " FAILED (length: " + o.toByteArray().length + " " + metainfo.getPieceLength() + ")");
            }
        }

        FileOutputStream file = new FileOutputStream("a.txt");

        for (int j = 0; j < pieceCount; j++) {
            for (int i = 0; i < pieces.get(i).size(); i++) {
                file.write(pieces.get(j).get(i).getBytes());
            }
        }

        file.close();


    }
}
