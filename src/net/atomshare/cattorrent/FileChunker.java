package net.atomshare.cattorrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class FileChunker {
    public static final int CHUNK_LENGTH = 16000;
    private final Metainfo metainfo;
    private final String path;
    private RandomAccessFile file;

    public FileChunker(String path, Metainfo metainfo) throws IOException {
        this.metainfo = metainfo;
        this.path = path;
        this.file = new RandomAccessFile(path, "rw");
        file.setLength(metainfo.getLength());
    }

    public synchronized ByteString read(int piece, int begin, int length) throws IOException {
        file.seek(piece * this.metainfo.getPieceLength() + begin);
        byte[] r = new byte[length];
        file.read(r);
        return new ByteString(r);
    }

    public synchronized void write(int piece, int begin, ByteString b) throws IOException {
        file.seek(piece * this.metainfo.getPieceLength() + begin);
        file.write(b.getBytes());
    }

    public synchronized void flush() {

    }
}
