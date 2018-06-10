package net.atomshare.cattorrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileChunker {
    public static final int CHUNK_LENGTH = 16000;
    private final Metainfo metainfo;
    private final String path;
    private List<RandomAccessFile> files = new ArrayList<>();

    public FileChunker(String path, Metainfo metainfo) throws IOException {
        this.metainfo = metainfo;
        this.path = path;

        if (metainfo.getFilesName().size() == 1) {
            RandomAccessFile f = new RandomAccessFile(path, "rw");
            f.setLength(metainfo.getLength());
            files.add(f);
        } else {
            new File(path).mkdirs();
            for (int i=0; i < metainfo.getFilesName().size(); i ++) {
                String filePath = path + "/" + metainfo.getFilesName().get(i);
                RandomAccessFile f = new RandomAccessFile(filePath, "rw");
                f.setLength(metainfo.getFilesLength().get(i));
                files.add(f);
            }
        }
    }

    public synchronized ByteString read(int piece, int begin, int length) throws IOException {
        byte[] out = new byte[length];
        int pos = piece * this.metainfo.getPieceLength() + begin;
        for (int i=0; i < metainfo.getFilesName().size(); i ++) {
            int filePos = pos - metainfo.getFilesBegin().get(i);
            int fileLength = metainfo.getFilesLength().get(i);
            if (filePos >= fileLength) continue;

            int readLength;
            int targetPos = 0;
            if (filePos < 0) {
                readLength = length + filePos;
                targetPos = -filePos;
                filePos = 0;
            } else {
                readLength = length;
                targetPos = 0;
            }
            readLength = Math.min(readLength, metainfo.getFilesLength().get(i) - filePos);

            if (readLength <= 0) continue;

            RandomAccessFile file = files.get(i);
            file.seek(filePos);
            file.read(out, targetPos, readLength);
        }
        return new ByteString(out);
    }

    public synchronized void write(int piece, int begin, ByteString b) throws IOException {
        int length = b.length();
        int pos = piece * this.metainfo.getPieceLength() + begin;
        //System.out.println("piece " + piece + " begin " + begin + " " + b.length() + " ->" + metainfo.getFilesName().size());
        for (int i=0; i < metainfo.getFilesName().size(); i ++) {
            int filePos = pos - metainfo.getFilesBegin().get(i);
            int fileLength = metainfo.getFilesLength().get(i);
            if (filePos >= fileLength) continue;

            int readLength;
            int targetPos = 0;
            if (filePos < 0) {
                readLength = length + filePos;
                targetPos = -filePos;
                filePos = 0;
            } else {
                readLength = length;
                targetPos = 0;
            }
            readLength = Math.min(readLength, metainfo.getFilesLength().get(i) - filePos);

            //System.out.println("readLength " + readLength + " targetPos " + targetPos + " filePos " + filePos);
            if (readLength <= 0) continue;

            RandomAccessFile file = files.get(i);
            file.seek(filePos);
            file.write(b.getBytes(), targetPos, readLength);
        }
    }

    public synchronized void flush() {

    }
}
