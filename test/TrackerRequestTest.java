import static org.junit.Assert.*;

import net.atomshare.cattorrent.Metainfo;
import net.atomshare.cattorrent.TrackerRequest;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

public class TrackerRequestTest {
    @Test
    public void ClientIdTest() {
        Metainfo met = new Metainfo("/home/guzik/Moje/TCS/II_semestr/Obiektowe/Projekt/lorem.txt.torrent");
        TrackerRequest tr = new TrackerRequest(met, TrackerRequest.Event.STARTED);
        assertEquals(tr.getPeerId(), tr.getPeerId());
    }
    @Test
    public void baseUrlBuilderTest() throws IOException {
        Metainfo met = new Metainfo("/home/guzik/Moje/TCS/II_semestr/Obiektowe/Projekt/lorem.txt.torrent");
        TrackerRequest tr = new TrackerRequest(met, TrackerRequest.Event.STARTED);
        System.out.println(tr.buildBaseUrl());
        try {
            URL myUrl = new URL(tr.buildBaseUrl());
        } catch (Exception e) {
            fail("Unable to build url");
        }
    }

}
