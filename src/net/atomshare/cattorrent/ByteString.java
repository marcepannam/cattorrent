package net.atomshare.cattorrent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteString implements Comparable<ByteString> {
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

    public int length() {
        return data.length;
    }

    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public int compareTo(ByteString byteString) {
        int len;
        len = Math.min(length(), byteString.length());
        for(int i=0; i<len; i++){
            if(byteString.data[i] > this.data[i]) return -1;
            if(byteString.data[i] < this.data[i]) return 1;
        }
        if(byteString.length() > length()) return -1;
        else return 1;
    }
}
