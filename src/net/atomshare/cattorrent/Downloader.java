package net.atomshare.cattorrent;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
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

    public static class Message {
        public int length;
        public int kind;
        public byte[] body;

        public static int CHOKE = 0;
        public static int UNCHOKE = 1;

        public String toString() {
            return this.length+ " "+ this.kind+" "+ new ByteString(this.body);
        }
    }

    public Message readMessage() throws IOException {

        DataInputStream in = new DataInputStream(socket.getInputStream());
        int length = in.readInt();
        int kind = (int)in.readByte();

        System.out.println(length);
        System.out.println(kind);

        byte[] bytes = new byte[length-1];

        in.readFully(bytes);
        System.out.println("new  mssgr");


        Message message = new Message();

        message.length = length;
        message.kind = kind;
        message.body = bytes;

        System.out.println(message.toString());

        return message;
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

        // write handshake
        OutputStream out = socket.getOutputStream();
        out.write(19);
        out.write(new ByteString("BitTorrent protocol").getBytes());
        out.write(new byte[8]);
        out.write(this.metainfo.getInfoHash());
        if (TrackerRequest.clientId.length() != 20) throw new AssertionError();
        out.write(new ByteString(TrackerRequest.clientId).getBytes());
        out.flush();

        // read handshake
        DataInputStream in = new DataInputStream(socket.getInputStream());
        if (in.readByte() != 19) throw new IOException("bad");
        byte[] b = new byte[19];
        in.readFully(b);
        if (!new ByteString(b).equals(new ByteString("BitTorrent protocol"))) throw new IOException("bad protocol");
        in.readFully(new byte[8]);
        b = new byte[20];
        in.readFully(b);
        if(!Arrays.equals(this.metainfo.getInfoHash(), b)) throw new IOException("bad hash");
        b = new byte[20];
        in.readFully(b);
        System.out.println("peer client id: " + new ByteString(b));

    }
}
