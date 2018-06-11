package net.atomshare.cattorrent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.net.URL;

import static java.lang.Character.toUpperCase;

/**
 * TrackerRequest
 * This class constructs message that is sent to
 * tracker basing on the data from torrent file
 */

public class TrackerRequest {
    private static final String clientVersion = "-CA0001-";
    public static final String clientId = computeClientId();

    public enum Event {STARTED, STOPPED, COMPLETED}

    private Event event;
    private int numWant;
    private int compact;
    private int noPeerId;
    private String baseUrl;
    private Metainfo metainfo;

    public TrackerRequest(Metainfo metainfo, Event event) {
        this.metainfo = metainfo;
        this.event = event;
        this.numWant = 20;
        //change this option to 1 in order to cooperate with more trackers
        //requires implementation of additional functionalities
        this.compact = 1;
        //change this option to 1 in order to exclude peer id from tracker response
        this.noPeerId = 0;
    }

    public String buildBaseUrl() throws IOException {
        StringBuilder baseUrlBuilder = new StringBuilder();
        try {
            baseUrlBuilder.append(metainfo.getDecodedAnnounceUrl());
            baseUrlBuilder.append("?info_hash=");
            baseUrlBuilder.append(urlEncode(metainfo.getInfoHash()));
            baseUrlBuilder.append("&peer_id=");
            baseUrlBuilder.append(getPeerId());
            baseUrlBuilder.append("&port=");
            baseUrlBuilder.append(getPort());
            baseUrlBuilder.append("&uploaded=");
            baseUrlBuilder.append(getUploaded());
            baseUrlBuilder.append("&downloaded=");
            baseUrlBuilder.append(getDownloaded());
            baseUrlBuilder.append("&left=");
            baseUrlBuilder.append(getLeft());
            baseUrlBuilder.append("&compact=");
            baseUrlBuilder.append(getCompact());
            baseUrlBuilder.append("&no_peer_id=");
            baseUrlBuilder.append(getNoPeerId());
            baseUrlBuilder.append("&event=");
            baseUrlBuilder.append(getEvent());
            //optional parameters
            /*baseURL += &ip=;
            baseURL += getIp();
            */
            baseUrlBuilder.append("&numwant=");
            baseUrlBuilder.append(getNumWant());
            /*baseURL += "&key=";
            baseURL += getKey();
            baseURL += "&trackerId=";
            baseURL += getTrackerId();
            */
        } catch (IOException e) {
            throw new IOException("Unable to create URL for tracker request: ", e);
        }
        return baseUrlBuilder.toString();
    }

    private int getLeft() {
        return 0;
    }

    private int getUploaded() {
        return 0;
    }

    private int getDownloaded() {
        return 0;
    }

    private int getNumWant() {
        return numWant;
    }

    private int getCompact() {
        return compact;
    }

    public String getPeerId() {
        return clientId;
    }

    private int getPort() throws IOException {
        Integer port = 6881;
        while (port < 6890) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(port);
                return port;
            } catch (IOException e) {
                port++;
            } finally {
                try {
                    if (ss != null) {
                        ss.close();
                    }
                } catch (IOException e) {
                    System.out.println("Cannot close port " + port);
                }
            }
        }
        //impossible to establish connection in range reserved for BitTorrent Clients
        throw new IOException("Impossible to connect to the port");
    }

    private int getNoPeerId() {
        return noPeerId;
    }

    private String getEvent() {
        switch (event) {
            case STARTED:
                return "started";
            case STOPPED:
                return "stopped";
            case COMPLETED:
                return "completed";
            default:
                throw new IllegalArgumentException("Bad or no event specified.");
        }
    }

    public static String urlEncode(byte[] buff) {
        StringBuilder url = new StringBuilder();
        for (byte b : buff) {
            if (isAllowed(b)) {
                url.append(String.format("%c", b));
            } else {
                url.append("%");
                url.append(String.format("%02X", b));
            }
        }
        return url.toString();
    }

    private static String computeClientId() {
        SecureRandom random = new SecureRandom();
        byte[] peerId = new byte[20];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            stringBuilder.append(ThreadLocalRandom.current().nextInt(10));
        }
        System.arraycopy(clientVersion.getBytes(), 0, peerId, 0, 8);
        System.arraycopy(stringBuilder.toString().getBytes(), 0, peerId, 8, 12);
        return new String(peerId);
    }

    private static boolean isAllowed(byte b) {
        //0-9, a-z, A-Z, ., -, _, ~ ASCII
        return (b >= 48 && b <= 57) || (b >= 65 && b <= 90) ||
                (b >= 97 && b <= 122) || b == 45 || b == 46 || b == 95 || b == 126;
    }
}
