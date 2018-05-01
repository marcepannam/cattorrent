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
    //This constructor takes absolute path from gui,
    //locates file in the filesystem
    //and provides access to its contents
    public Metainfo(String str) {
        Path path = FileSystems.getDefault().getPath(str);
        Map metainfo = readInfo(path);
        //assume that torrent file is correctly bencoded
        info = (Map) metainfo.get(new ByteString("info"));
        announceUrl = (ByteString) metainfo.get(new ByteString("announce"));
    }

    //this methods allows to access bencoded url of the tracker
    public String getDecodedAnnounceUrl() throws IOException {
        return (String) decode(announceUrl);
    }
    //this methods allows to access utf-8 encoded url of the tracker
    public ByteString getAnnounceUrl() {
        return announceUrl;
    }
    //this methods allows to access contents of the .torrent file
    public Map getInfo() {
        return info;
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
