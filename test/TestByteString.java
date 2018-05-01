
import net.atomshare.cattorrent.ByteString;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import static net.atomshare.cattorrent.Bencoder.decode;
import static net.atomshare.cattorrent.Bencoder.input;
import static org.junit.Assert.assertEquals;

public class TestByteString {
    @Test
    public void compareTo(){
        ByteString a = new ByteString ("spam");
        ByteString b = new ByteString ("spam3");
        int i  = a.compareTo(b);
        assertEquals(i, 1) ;
    }
}
