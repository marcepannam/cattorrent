package net.atomshare.cattorrent;

import java.io.*;
import java.net.URL;

public final class TrackerResponse {
    public static byte[] get(URL url) throws IOException {
        InputStream is = url.openStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buff = new byte[2048];
        int read;
        while((read = is.read(buff, 0, buff.length)) != -1) {
            os.write(buff, 0, read);
        }
        os.flush();
        is.close();
        return os.toByteArray();
    }
}
