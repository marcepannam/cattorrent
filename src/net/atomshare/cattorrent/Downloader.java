package net.atomshare.cattorrent;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

/**
 * Downloader downloads data from a connected peer.
 */
public class Downloader {
    private Metainfo metainfo;
    private Socket socket;

    public Downloader (Metainfo metainfo) throws IOException {
        this.metainfo = metainfo;
    }

    public void init() throws IOException {
        TrackerRequest tracker_request = new TrackerRequest(metainfo, TrackerRequest.Event.STARTED);
        URL url = new URL(tracker_request.buildBaseUrl());
        byte[] trackerData = TrackerResponse.get(url);
        Object trackerResp = Bencoder.decode(trackerData);

        ByteString peers = (ByteString)((Map<Object,Object>)trackerResp).get(new ByteString("peers"));

        byte[] peersArray = peers.getBytes();
        String ipAddress = Byte.toUnsignedInt(peersArray[0]) + "." + Byte.toUnsignedInt(peersArray[1])
                + "." + Byte.toUnsignedInt(peersArray[2]) + "." + Byte.toUnsignedInt(peersArray[3]);
        int port = peersArray[5] + peersArray[4] * 256;
        System.out.println(ipAddress + " "  + port);

        socket = new Socket(ipAddress, port);
        System.out.println("connected");
    }
}
