import static org.junit.Assert.*;

import net.atomshare.cattorrent.Metainfo;
import net.atomshare.cattorrent.TrackerRequest;
import org.junit.Test;
import java.io.IOException;
import java.net.URL;

public class TrackerRequestTest {
    @Test
    public void ClientIdTest() throws IOException {
        Metainfo met = new Metainfo("tmp/lorem.txt.torrent");
        TrackerRequest tr = new TrackerRequest(met, TrackerRequest.Event.STARTED);
        assertEquals(tr.getPeerId(), tr.getPeerId());
    }
    @Test
    public void baseUrlBuilderTest() throws IOException {
        Metainfo met = new Metainfo("tmp/lorem.txt.torrent");
        TrackerRequest tr = new TrackerRequest(met, TrackerRequest.Event.STARTED);
        System.out.println(tr.buildBaseUrl());
        try {
            URL myUrl = new URL(tr.buildBaseUrl());
        } catch (Exception e) {
            fail("Unable to build url");
        }
    }
    @Test
    public void urlEncodingTest() {
        byte[] b2 = {0x12, 0x34, 0x56, 0x78, (byte) 0x9a,
                (byte) 0xbc, (byte) 0xde, (byte) 0xf1, 0x23, 0x45, 0x67, (byte) 0x89, (byte)0xab,
                (byte)0xcd, (byte)0xef, 0x12, 0x34, 0x56, 0x78, (byte)0x9a};
        assertEquals("%124Vx%9A%BC%DE%F1%23Eg%89%AB%CD%EF%124Vx%9A",TrackerRequest.urlEncode(b2));
    }
}
