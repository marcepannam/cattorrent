package net.atomshare.cattorrent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.Character.isDigit;

public class Bencoder {
    public static Object decode(InputStream in) throws IOException {

        char b = (char)in.read();

        if(isDigit(b)){
            byte [] out = parseString(b, in);
            return out;
            //String result = toString(out);
            //return result;
        }

        if(b == 'i') {
            Integer out = parseInt(in);
            return out;
        }

        if(b == 'l'){
            char d = (char)in.read();
            List lista = new ArrayList<Object>();
            lista = parseList(in, (ArrayList<Object>) lista, d);
            return lista;
        }

        if(b == 'd'){
            byte d = (byte)in.read();
            Map<Object, Object> mapa = new Hashtable<Object, Object>();
            mapa = parseDictionary(mapa, (char) d, in);
            return mapa;
        }

        return "nie dziala";
    }

    static Map<Object, Object> parseDictionary (Map<Object, Object> mapa, char d, InputStream in)throws IOException{
        if(d != 'e'){
            char b = (char)in.read();
            byte [] value = parseString(b, in);
            b = (char)in.read();
            byte[] k  = parseString(b, in);
            String key = toString(k);
            mapa.put(value, key);
            b = (char)in.read();
            parseDictionary(mapa, b, in);
        }

        return mapa;
    }

    static String toString(byte [] out){
        String str = "";

        for(int i = 0; i < out.length; i++)
        {
            str += (char)out[i];
        }

        return str;
    }

    static byte [] parseString (char b, InputStream in) throws IOException{
        String s = ""+b;
        while(true){
            char d = (char)in.read();
            if(d ==':') break;
            else s+=d;
        }
        int len = Integer.parseInt(s);
        byte[] out = new byte[len];
        for(int i=0; i<len; i++){
            byte d = (byte)in.read();
            out[i] = d;
        }
        return out;
    }

    static Integer parseInt(InputStream in) throws IOException{
        char d = (char) in.read();
        String s = "";
        while (isDigit(d) || d == '-') {
            s += d;
            d = (char) in.read();
        }
        d = (char) in.read();
        Integer out = Integer.parseInt(s);
        return out;
    }


    static ArrayList<Object> parseList(InputStream in, ArrayList<Object> lista, char d) throws IOException{
        String s = "";
        while (d != ':') {
            s += d;
            d = (char) in.read();
        }
        Integer len = Integer.parseInt(s);
        String ss = "";
        for (int i = 0; i < len; i++) {
            d = (char) in.read();
            ss += d;
        }
        lista.add(ss);

        d = (char) in.read();
        if(d != 'e') parseList(in, lista, d);
        return  lista;
    }

    public static Object decode(byte[] a) throws IOException {
        return decode(new ByteArrayInputStream(a));
    }

    public static void main(String[] s) throws IOException {


        Object o = decode("4:spam".getBytes(StandardCharsets.UTF_8));
        byte[] bb = (byte[])o;
        if (!Objects.equals(new String(bb), "spam")) throw new AssertionError();

        o = decode("i51e".getBytes(StandardCharsets.UTF_8));
        if (!Objects.equals(o, 51)) throw new AssertionError();


        o = decode("i-51e".getBytes(StandardCharsets.UTF_8));
        if (!Objects.equals(o, -51)) throw new AssertionError();
        
    }

}
