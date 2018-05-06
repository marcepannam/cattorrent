import static org.junit.Assert.*;

import net.atomshare.cattorrent.Metainfo;
import net.atomshare.cattorrent.TrackerRequest;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MetainfoTest {
    @Test
    public void buildMetainfoTest() throws IOException {
        Metainfo met = new Metainfo("/home/guzik/Moje/TCS/II_semestr/Obiektowe/Projekt/lorem.txt.torrent");
        assertEquals(met.getName(), "lorem.txt");
        assertEquals(met.getPieceLength(),new Integer(4096));
        assertEquals(met.getLength(),new Integer(483828));
        assertEquals(met.getDecodedAnnounceUrl(),"https://torrent.zielmicha.com/tracker/6NQ6EPWU2V5KC_/announce");
        //assertEquals(met.getInfoHash().toLowerCase(), "6c5a4684d054c3bf84d125aa9a69d31ab52a2ebe");
    }
}
