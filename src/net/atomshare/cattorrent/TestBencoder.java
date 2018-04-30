package net.atomshare.cattorrent;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import static net.atomshare.cattorrent.Bencoder.decode;
import static net.atomshare.cattorrent.Bencoder.input;
import static org.junit.Assert.assertEquals;

public class TestBencoder {


    @Test
    public void test() throws IOException {

        Object o = decode("4:spam".getBytes(StandardCharsets.UTF_8));
        assertEquals(o, new ByteString("spam"));

        o = decode("12:spamspamspam".getBytes(StandardCharsets.UTF_8));
        assertEquals(o, new ByteString("spamspamspam") );

        o = decode("i51e".getBytes(StandardCharsets.UTF_8));
        assertEquals(o, 51) ;

        o = decode("i-51e".getBytes(StandardCharsets.UTF_8));
        assertEquals(o, -51) ;

        o = decode("i-55551e".getBytes(StandardCharsets.UTF_8));
        assertEquals(o, -55551) ;

    }

    @Test
    public void testList() throws  IOException {
        List lista = new ArrayList<String>();
        lista.add(new ByteString ("spam"));
        lista.add(new ByteString ("eggs"));
        Object o = decode(new ByteString ("l4:spam4:eggse"));
        assertEquals(lista, o) ;
    }

    @Test
    public void testList1() throws  IOException {
        List lista = new ArrayList();
        lista.add(new ByteString ("spam") );
        lista.add(new ByteString ("eggs") );
        List lista2 = new ArrayList();
        lista2.add(lista);
        lista2.add(lista);
        Object o = decode(new ByteString ("ll4:spam4:eggsel4:spam4:eggsee"));
        assertEquals(lista2, o) ;
    }

    @Test
    public void testMap1() throws  IOException {
        //System.out.println("TEST 3 ");
        Map<ByteString, ByteString> mapa = new HashMap<>();
        mapa.put(new ByteString ("cow"), new ByteString ("moo") );
        mapa.put(new ByteString ("spam") , new ByteString ("eggs") );
        Object o = decode(new ByteString ("d3:cow3:moo4:spam4:eggse"));
        assertEquals(mapa, o) ;
    }

    @Test
    public void testMap2() throws  IOException {
        Map<ByteString, ByteString> mapa = new HashMap<>();
        mapa.put(new ByteString ("c"), new ByteString ("m"));
        mapa.put(new ByteString("s"), new ByteString ("e"));
        Object o = decode(new ByteString("d1:c1:m1:s1:ee"));  //"d1:c1:m1:s1:ee"
        assertEquals(mapa, o);
    }


    @Test
    public void test5() throws IOException {
        String in = "/home/marianna/cattorrent/tmp/lorem.txt.torrent";
        Object o = decode(input(in));
    }
}


