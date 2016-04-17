package com.melchor629.musicote.basededatos;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Java Object representation for a row in Songs Table
 */
public final class SongRow implements Map<String, String>, Serializable {
    private transient String titulo, artista, album, archivo;
    private transient boolean descargado;
    private transient long duración;
    private transient int id;

    /**
     * Creates the object from the cursor
     * @param c Cursor
     */
    public SongRow(Cursor c) {
        id = c.getInt(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ID));
        titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_TITULO));
        artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARTISTA));
        album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ALBUM));
        archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARCHIVO));
        descargado = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DOWNLOADED)).equals("true");
        duración = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DURACION));
    }

    public SongRow(String str) {
        Scanner sc = new Scanner(str);
        sc.useDelimiter("\\n");
        titulo = sc.next();
        artista = sc.next();
        album = sc.next();
        archivo = sc.next();
        descargado = sc.nextBoolean();
        duración = sc.nextLong();
        id = sc.nextInt();
    }

    /**
     * @return the title of the song
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * @return the artist of the song
     */
    public String getArtista() {
        return artista;
    }

    /**
     * @return the album of the song
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @return the file name of the song from the server
     */
    public String getArchivo() {
        return archivo;
    }

    /**
     * @return true if the song is on the device
     */
    public boolean isDownloaded() {
        return descargado;
    }

    /**
     * @return gets a String representation of Downloaded for FontAwesome icons
     */
    public String getDownloadedRepresentation() {
        return descargado ? "{fa-mobile}" : "{fa-cloud}";
    }

    /**
     * @return gets a String representation of the duration of the song
     */
    public String getDuracion() {
        long horas = duración / 3600;
        long minutos = duración / 60 % 60;
        long segundos = duración % 60;
        String representación;

        if(horas == 0)
            representación = String.format("%.2d:%.2d", minutos, segundos);
        else
            representación = String.format("%.2d:%.2d:%.2d", horas, minutos, segundos);
        return representación;
    }

    /**
     * @return the ID of the song
     */
    public int getId() {
        return id;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean containsKey(Object o) {
        try {
            return this.getClass().getMethod(getMethodName(o.toString())) != null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean containsValue(Object o) {
        if(o instanceof String) {
            return titulo.equals(o) || artista.equals(o) || album.equals(o) || archivo.equals(o);
        } else if(o instanceof Long) {
            return ((Long) o).compareTo(duración) == 0;
        } else if(o instanceof Integer) {
            return 0 == ((Integer) o).compareTo(id);
        } else if(o instanceof Boolean) {
            return ((Boolean) o).compareTo(descargado) == 0;
        }
        return false;
    }

    @NonNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return new HashSet<>();
    }

    @Override
    public String get(Object o) {
        try {
            String cuero = getMethodName(o.toString());
            return this.getClass().getMethod(cuero).invoke(this).toString();
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NonNull
    @Override
    public Set<String> keySet() {
        HashSet<String> keys = new HashSet<>();
        for(Method f : this.getClass().getMethods())
            if(f.getName().startsWith("get") && !f.getName().equals("get"))
                keys.add(f.getName().substring(3));
        return keys;
    }

    @Override
    public String put(String s, String s2) {
        try {
            this.getClass().getField(s.toLowerCase()).set(this, s2);
            return s2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        for(String key : map.keySet())
            put(key, map.get(key));
    }

    @Override
    public String remove(Object o) {
        return null;
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @NonNull
    @Override
    public Collection<String> values() {
        return new HashSet<>();
    }

    public String toString() {
        return titulo + "\n" + artista + "\n" + album + "\n" + archivo + "\n" + descargado + "\n" +
                duración + "\n" + id;
    }

    boolean getDownloaded() { return isDownloaded(); }

    private String getMethodName(String field) {
        return "get" +  field.toLowerCase().replaceFirst(field.substring(0, 1), field.substring(0, 1).toUpperCase());
    }
}
