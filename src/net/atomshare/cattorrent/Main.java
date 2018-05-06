package net.atomshare.cattorrent;

import java.io.IOException;
import java.util.ArrayList;

public class Main{

    public static void main(String[] args) throws IOException {
        // code only for testing, will be refactored later
        Metainfo metainfo = new Metainfo(args[0]);
        Downloader d = new Downloader(metainfo);
        d.init();
        d.sendRequest(0, 0, 16 * 1024);

        while(true) {
            Downloader.Message msg = d.readMessage();
            System.out.println(msg);

        }
    }
}
