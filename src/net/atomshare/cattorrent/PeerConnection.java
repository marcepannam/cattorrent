package net.atomshare.cattorrent;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.atomshare.cattorrent.PeerConnection.Message.BITFIELD;
import static net.atomshare.cattorrent.PeerConnection.Message.HAVE;
import static net.atomshare.cattorrent.PeerConnection.Message.PIECE;
import static net.atomshare.cattorrent.PeerConnection.Message.*;

/**
 * PeerConnection downloads data from a connected peer.
 */
public class PeerConnection {
    private Metainfo metainfo;
    private Socket socket;

    /**
     * Which pieces this peer has?
     */
    private ArrayList<Boolean> hasPieces;

    /**
     * Are we chocked?
     */
    private boolean chocked = true;

    public boolean isChocked() {
        return chocked;
    }

    private byte[] peerInfo;
    private Downloader.DownloadProgressListener listener;

    public PeerConnection(Metainfo metainfo, byte[] peerInfo, Downloader.DownloadProgressListener listener) {
        this.metainfo = metainfo;
        this.peerInfo = peerInfo;
        this.listener = listener;
        hasPieces = new ArrayList<>();
        for (int i = 0; i < metainfo.getPieceCount(); i++)
            hasPieces.add(false);
    }

    public boolean hasPiece(int j) {
        return hasPieces.get(j);
    }

    public static class Message {
        public int kind;
        public int index;
        public int begin;
        public byte[] body;

        public static int CHOKE = 0;
        public static int UNCHOKE = 1;
        public static int INTERESTED = 2;
        public static int NOT_INTERESTED = 3;

        // new pieces of file is avalible
        public static int HAVE = 4;

        // bitmask of avalible pieces
        public static int BITFIELD = 5;

        //used to request a block
        public static int REQUEST = 6;

        public static int PIECE = 7;

        //used to cancel block requests
        public static int CANCEL = 8;

        public static int PORT = 9;
        public int length;

        public String toString() {
            if (this.body != null) {
                return this.kind + " " + new ByteString(this.body);
            }
            return this.kind + " null";
        }
    }

    public void sendRequest(int piece, int begin, int length) throws IOException {
        // if (!hasPieces.get(piece)) {
        //     throw new IOException("peer doesn't have this piece");
        // }

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(13);
        out.writeByte(Message.REQUEST);
        out.writeInt(piece);
        out.writeInt(begin);
        out.writeInt(length);
    }

    public void sendUnchoke() throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(1);
        out.writeByte(Message.UNCHOKE);
    }

    public void sendBitmap(List<Boolean> bitmap) throws IOException {
        byte[] b = new byte[(bitmap.size() + 7) / 8];
        for (int i=0; i < bitmap.size(); i ++) {
            if (bitmap.get(i))
                b[i / 8] += ( 1 << (7 - (i % 8)) );
        }
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(b.length + 1);
        out.writeByte(Message.BITFIELD);
        out.write(b);
    }

    public void sendPiece(int index, int begin, ByteString s) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(9 + s.length());
        out.writeByte(Message.IECE);
        out.writeInt(index);
        out.writeInt(begin);
        out.write(s.getBytes());
    }

    public Message readMessage() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int length = in.readInt();
        int kind = (int) in.readByte();

        Message message = new Message();
        message.kind = kind;

        System.err.println("message: " + kind);
        if (kind == PIECE) {
            message.index = in.readInt();
            message.begin = in.readInt();
            message.body = new byte[length - 1 - 8];
            in.readFully(message.body);
        } else if (kind == REQUEST) {
            message.index = in.readInt();
            message.begin = in.readInt();
            message.length = in.readInt();
            System.out.println("REQUEST " + message.index + " " + message.begin + " " + message.length);
        } else if (kind == BITFIELD) {
            byte[] mask = new byte[length - 1];
            in.readFully(mask);
            for (int i = 0; i < hasPieces.size(); i++) {
                if (((mask[i / 8] >> (7 - i % 8)) & 1) != 0) {
                    //System.out.println("hasPiece " + i);
                    hasPieces.set(i, true);
                }
            }
        } else if (kind == HAVE) {
            int piece = in.readInt();
            hasPieces.set(piece, true);
        } else if (kind == CHOKE) {
            chocked = true;
        } else if (kind == UNCHOKE) {
            chocked = false;
        } else {
            byte[] bytes = new byte[length - 1];
            in.readFully(bytes);
        }

        return message;
    }

    public void init() throws IOException {
        String ipAddress = Byte.toUnsignedInt(peerInfo[0]) + "." + Byte.toUnsignedInt(peerInfo[1])
                + "." + Byte.toUnsignedInt(peerInfo[2]) + "." + Byte.toUnsignedInt(peerInfo[3]);
        int port = Byte.toUnsignedInt(peerInfo[5]) + Byte.toUnsignedInt(peerInfo[4]) * 256;
        System.out.println(ipAddress + " " + port);

        socket = new Socket(ipAddress, port);
        System.out.println("connected");
        listener.onLog("Connected to peer " + ipAddress + " at port " + port);

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
        if (!Arrays.equals(this.metainfo.getInfoHash(), b)) throw new IOException("bad hash");
        b = new byte[20];
        in.readFully(b);
        System.out.println("peer client id: " + new ByteString(b));

        // write "Interested" status
        DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
        dout.writeInt(1);
        dout.writeByte(Message.INTERESTED);
        dout.flush();
    }
}
