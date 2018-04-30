package net.atomshare.cattorrent;
import java.io.*;
import java.util.*;
import static java.lang.Character.isDigit;

public class Bencoder {

    public static InputStream input(String name) throws  FileNotFoundException{
        return new FileInputStream(name);
    }

    public static Object decode(InputStream in) throws IOException {

        char b = (char)in.read();

        if(isDigit(b)){
            byte [] out = parseString(b, in);
            return new ByteString(out);
        }

        if(b == 'i') {
            Integer out = parseInt(in);
            return out;
        }

        if(b == 'l'){
            byte d = (byte)in.read();
            List lista = new ArrayList<Object>();
            lista = parseList(in, (ArrayList<Object>) lista, d);
            return lista;
        }

        if(b == 'd'){
            Map<Object, Object> mapa = new HashMap<>();
            mapa =  parseDictionary(mapa, in);
            return mapa;
        }

        throw new IOException("invalid type '" + b + "'");
    }

    static Map<Object, Object> parseDictionary (Map<Object, Object> mapa, InputStream in)throws IOException{
        byte d = (byte)in.read();
        if(d != 'e'){
            PushbackInputStream pis = new PushbackInputStream(in, 1);
            pis.unread(d);
            Object key = decode(pis);
            Object value = decode(in);
            mapa.put(key, value);
            parseDictionary(mapa, in);
        }

        return mapa;
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
        Integer out = Integer.parseInt(s);
        return out;
    }


    static ArrayList<Object> parseList(InputStream in, ArrayList<Object> lista, byte b) throws IOException{
       while((char)b != 'e'){
            PushbackInputStream pis = new PushbackInputStream(in, 1);
            pis.unread(b);
            Object o = decode(pis);
            lista.add(o);
            b = (byte) in.read();
        }
        return  lista;
    }

    //overload

    public static Object decode(byte[] a) throws IOException {
        return decode(new ByteArrayInputStream(a));
    }
    public static Object decode(ByteString a) throws IOException {
        return decode(new ByteArrayInputStream(a.getBytes()));
    }

    public static void main(String[] args)  {

    }


}
