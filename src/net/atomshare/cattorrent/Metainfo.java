package net.atomshare.cattorrent;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.atomshare.cattorrent.Bencoder.decode;
import static net.atomshare.cattorrent.Bencoder.encode;
import static net.atomshare.cattorrent.Bencoder.encodeAsArray;

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

    public List<String> getFilesName() {
        return filesName;
    }

    public List<Integer> getFilesBegin() {
        return filesBegin;
    }

    public List<Integer> getFilesLength() {
        return filesLength;
    }

    private List<String> filesName = new ArrayList<>();
    private List<Integer> filesBegin = new ArrayList<>();
    private List<Integer> filesLength = new ArrayList<>();

    //This constructor takes absolute path from gui,
    //locates file in the filesystem
    //and provides access to its contents
    public Metainfo(String str) throws IOException {
        Path path = FileSystems.getDefault().getPath(str);
        Map metainfo = readInfo(path);
        //assume that torrent file is correctly bencoded
        info = (Map) metainfo.get(new ByteString("info"));
        announceUrl = (ByteString) metainfo.get(new ByteString("announce"));
        pieceLength = readPieceLength(info);
        readLength();
        pieces = readPieces(info);
        name = readName(info);
    }

    private void readLength() {
        if (info.containsKey(new ByteString("length"))) {
            // single file torrent
            length = (Integer) info.get(new ByteString("length"));
            filesBegin.add(0);
            filesLength.add(length);
            filesName.add("");
        } else {
            // multi file torrent
            List fs = (List) info.get(new ByteString("files"));
            length = 0; // length of whole torrent
            System.out.println("fs: " + fs);
            for (Object f : fs) {
                Map fileInfo = (Map) f;
                int flength = (Integer) fileInfo.get(new ByteString("length"));
                filesBegin.add(length);
                filesLength.add(flength);
                String name = "";
                for (Object part : (List) fileInfo.get(new ByteString("path"))) {
                    // a/b/c.txt -> [a, b, c.txt]
                    String pathStr = ((ByteString) part).toString();
                    if (pathStr.equals("..") || pathStr.equals(".") || pathStr.contains("/") || pathStr.contains("\\"))
                        throw new SecurityException("bad path");
                    name += "/" + pathStr;
                }
                name = name.substring(1); // remove first slash
                filesName.add(name);
                length += flength;
            }
        }
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
    public ByteString getPieceHashes() {
        return pieces;
    }

    public ByteString getPieceHash(int i) {
        return new ByteString(Arrays.copyOfRange(pieces.getBytes(), i * Utils.SHA1_BYTES, (i + 1) * Utils.SHA1_BYTES));
    }

    public ByteString getInfo() {
        return encode(info);
    }

    public byte[] getInfoHash() throws IOException {
        byte[] buff = new byte[20];
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(encodeAsArray(info));
            md.digest(buff, 0, buff.length);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Java does not recognize SHA-1 as algorithm.", e);
        } catch (DigestException e) {
            throw new IOException("Unable to create SHA-1 hash: ", e);
        }
        return buff;
    }

    private Integer readPieceLength(Map info) {
        return (Integer) info.get(new ByteString("piece length"));
    }

    private ByteString readPieces(Map info) {
        return (ByteString) info.get(new ByteString("pieces"));
    }

    private String readName(Map info) {
        ByteString name = new ByteString("name");
        if (info.containsKey(name)) {
            return info.get(name).toString();
        } else {
            return "";
        }
    }

    private Map readInfo(Path path) throws IOException {
        try {
            ByteString bs = new ByteString(Files.readAllBytes(path));
            //assume that torrent file is correctly bencoded
            return (Map) decode(bs);
        } catch (IOException e) {
            throw new IOException("Error occurred while reading the file. Check if provided filepath is correct", e);
        } catch (OutOfMemoryError e) {
            throw new IOException("This file is too large. Probably not a torrent file", e);
            //return null;
        } catch (SecurityException e) {
            throw new IOException("No permission to read the file", e);
        }
    }

    public int getPieceCount() {
        return (getLength() + getPieceLength() - 1) / getPieceLength();
    }
}
