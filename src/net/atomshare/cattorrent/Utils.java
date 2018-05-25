package net.atomshare.cattorrent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static final int SHA1_BYTES = 20;

    public static ByteString computeSha1Hash(ByteString s) {
        try{
            MessageDigest msgHash = MessageDigest.getInstance("SHA-1");
            msgHash.update(s.getBytes());
            return (new ByteString(msgHash.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
