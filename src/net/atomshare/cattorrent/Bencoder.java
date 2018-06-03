package net.atomshare.cattorrent;

import java.io.*;
import java.util.*;

import static java.lang.Character.isDigit;

/**
 * Bencoding implements way to organize data in torrent files.
 * It supports strings, integers, lists, dictionaries.
 */


public class Bencoder {

    public static InputStream input(String name) throws FileNotFoundException {
        return new FileInputStream(name);
    }

    public static Object decode(InputStream in) throws IOException {

        char b = (char) in.read();

        if (isDigit(b)) {
            byte[] out = parseString(b, in);
            return new ByteString(out);
        }

        if (b == 'i') {
            Integer out = parseInt(in);
            return out;
        }

        if (b == 'l') {
            byte d = (byte) in.read();
            List lista = new ArrayList<Object>();
            lista = parseList(in, (ArrayList<Object>) lista, d);
            return lista;
        }

        if (b == 'd') {
            Map<Object, Object> mapa = new HashMap<>();
            mapa = parseDictionary(mapa, in);
            return mapa;
        }

        throw new IOException("invalid type '" + b + "'");
    }

    static Map<Object, Object> parseDictionary(Map<Object, Object> mapa, InputStream in) throws IOException {
        byte d = (byte) in.read();
        if (d != 'e') {
            PushbackInputStream pis = new PushbackInputStream(in, 1);
            pis.unread(d);
            Object key = decode(pis);
            Object value = decode(in);
            mapa.put(key, value);
            parseDictionary(mapa, in);
        }

        return mapa;
    }

    static byte[] parseString(char b, InputStream in) throws IOException {
        String s = "" + b;
        while (true) {
            char d = (char) in.read();
            if (d == ':') break;
            else s += d;
        }
        int len = Integer.parseInt(s);
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            byte d = (byte) in.read();
            out[i] = d;
        }
        return out;
    }

    static Integer parseInt(InputStream in) throws IOException {
        char d = (char) in.read();
        String s = "";
        while (isDigit(d) || d == '-') {
            s += d;
            d = (char) in.read();
        }
        Integer out = Integer.parseInt(s);
        return out;
    }


    static ArrayList<Object> parseList(InputStream in, ArrayList<Object> lista, byte b) throws IOException {
        while ((char) b != 'e') {
            PushbackInputStream pis = new PushbackInputStream(in, 1);
            pis.unread(b);
            Object o = decode(pis);
            lista.add(o);
            b = (byte) in.read();
        }
        return lista;
    }

    //overload

    public static Object decode(byte[] a) throws IOException {
        return decode(new ByteArrayInputStream(a));
    }

    public static Object decode(ByteString a) throws IOException {
        return decode(new ByteArrayInputStream(a.getBytes()));
    }

    public static void encode(Object o, OutputStream out) throws IOException {
        if (o instanceof ByteString) {
            ByteString str = (ByteString) o;
            out.write(new ByteString(str.length() + ":").getBytes());
            out.write(str.getBytes());
            return;
        }

        if (o instanceof Integer) {
            Integer inte = (Integer) o;
            out.write(new ByteString("i" + inte + "e").getBytes());
            return;
        }

        if (o instanceof List) {
            List l = (List) o;
            out.write(new ByteString("l").getBytes());
            for (Object item : l) {
                encode(item, out);
            }
            out.write(new ByteString("e").getBytes());
            return;
        }

        if (o instanceof Map) {
            // keys must be sorted (because we compute SHA1 hash)
            Map<Object, Object> sortedMap = new TreeMap<Object, Object>((Map) o);
            out.write(new ByteString("d").getBytes());
            for (Map.Entry entry : sortedMap.entrySet()) {
                encode(entry.getKey(), out);
                encode(entry.getValue(), out);
            }
            out.write(new ByteString("e").getBytes());
            return;
        }

        throw new IllegalArgumentException(o + " has unknown type");

    }

    public static ByteString encode(Object o) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            encode(o, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteString(out.toByteArray());
    }

    static byte[] encodeAsArray(Object o) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            encode(o, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    public static void main(String[] args) {

    }

}
