package net.atomshare.cattorrent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteString {
    // https://ideone.com/edCdbc
    private byte[] data;

    public ByteString(byte[] b) {
        data = Arrays.copyOf(b, b.length);
    }

    public byte[] getBytes() {
        return data;
    }

    public ByteString(String a) {
        data = a.getBytes(StandardCharsets.UTF_8);
    }

    public String toString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof ByteString) {
            ByteString b = (ByteString)o;
            return Arrays.equals(b.data, data);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
