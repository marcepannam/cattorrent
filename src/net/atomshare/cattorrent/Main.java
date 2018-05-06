package net.atomshare.cattorrent;

import java.io.IOException;

public class Main{

    public static void main(String[] args) throws IOException {
        // code only for testing, will be refactored later
        Metainfo metainfo = new Metainfo(args[0]);
        Downloader d = new Downloader(metainfo);
        d.init();
    }
}
