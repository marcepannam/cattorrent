package net.atomshare.cattorrent;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static net.atomshare.cattorrent.Bencoder.decode;

/**
 * This class digests information contained in the Metainfo file
 * (ending with ".torrent") located on the user local machine
 * and presents it in an easily accessible manner
 */

public class Metainfo {
    private Path path;
    private ByteString announceUrl;
    private Map info;
    private Integer pieceLength;
    private Integer length;
    private ByteString pieces;
    private String name;

    //This constructor takes absolute path from gui,
    //locates file in the filesystem
    //and provides access to its contents
    public Metainfo(String str) {
        Path path = FileSystems.getDefault().getPath(str);
        Map metainfo = readInfo(path);
        //assume that torrent file is correctly bencoded
        info = (Map) metainfo.get(new ByteString("info"));
        announceUrl = (ByteString) metainfo.get(new ByteString("announce"));
        pieceLength = readPieceLength(info);
        length = readLength(info);
        pieces = readPieces(info);
        name = readName(info);
    }

    //this methods allows to access utf-8 encoded url of the tracker
    public String getDecodedAnnounceUrl() {
        return announceUrl.toString();
    }
    //this methods allows to access bencoded url of the tracker
    public ByteString getAnnounceUrl() {
        return announceUrl;
    }
    //this methods allow to access decoded contents of the .torrent file
    public String getName() {
        return name;
    }
    public Integer getLength() {
        return length;
    }
    public Integer getPieceLength() {
        return pieceLength;
    }
    //this method allows to access SHA1 hashes of the pieces in the .torrent file
    public ByteString getPieces() {
        return pieces;
    }

    private Integer readPieceLength(Map info) {
        return (Integer) info.get(new ByteString("piece length"));
    }
    private Integer readLength(Map info) {
        return (Integer) info.get(new ByteString("length"));
    }
    private ByteString readPieces(Map info) {
        return (ByteString) info.get(new ByteString("pieces"));
    }
    private String readName (Map info) {
        ByteString name = new ByteString("name");
        if (info.containsKey(name)) {
            return info.get(name).toString();
        } else {
            return "";
        }
    }

    private Map readInfo(Path path){
        try {
            ByteString bs = new ByteString(Files.readAllBytes(path));
            //assume that torrent file is correctly bencoded
            return (Map) decode(bs);
        } catch (IOException e) {
            System.out.println("Error occurred while reading the file. Check if provided filepath is correct");
            return null;
        } catch (OutOfMemoryError e) {
            System.out.println("This file is too large. Probably not a torrent file");
            return null;
        } catch (SecurityException e) {
            System.out.println("No permission to read the file");
            return null;
        }
    }
}