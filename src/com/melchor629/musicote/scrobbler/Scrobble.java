package com.melchor629.musicote.scrobbler;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * Envia scrobblings
 *
 * @author melchor
 */
public class Scrobble {

    /** SK code from <link>Auth.SK</link> */
    private static final String SK = Auth.SK;

    /** Working title */
    private String titulo = null;

    /** Working artist */
    private String artista = null;

    /** The status code for a petition */
    private int statusCode = 0;

    /**
     * Constructor for the class <i>Scrobble</i>
     *
     * @param Titulo  <i>Song title</i>
     * @param Artista <i>Song artist</i>
     */
    public Scrobble(String Titulo, String Artista) {
        titulo = Titulo;
        artista = Artista;
    }

    private final String TAG = "Scrobbler->Scrobble";

    /**
     * Envia el scrobbling a Last.FM
     *
     * @return status Estado resultante del Scrobbling
     */
    public int scrobble() {
        long timestamp = System.currentTimeMillis() / 1000;
        final HashMap<String, String> sign = sign(titulo, artista, timestamp);
        statusCode = Peticiones.error(Peticiones.HTTPSpost(sign));
        Log.d(TAG, "Scrobbling enviado");
        //Make this variables again null for other uses
        titulo = null;
        artista = null;
        return statusCode;
    }

    /**
     * Envia a Last.FM que estás escuchando dicha canción
     * @param duration Duration of the song, in seconds
     * @return status Estado resultante del updateNowPlaying
     */
    public int nowPlaying(int duration) {
        final HashMap<String, String> sign = sign(titulo, artista, duration);
        statusCode = Peticiones.error(Peticiones.HTTPSpost(sign));
        Log.d(TAG, "nowPlaying canviado");
        return statusCode;
    }

    private HashMap<String, String> sign(String titulo, String artista, long time) {
        Map<String, String> datos = Peticiones.map("method", "track.scrobble", "track", titulo, "artist", artista, "timestamp", "" + time, "sk", SK);
        HashMap<String, String> peticion = Peticiones.request(datos);
        return peticion;
    }

    private HashMap<String, String> sign(String titulo, String artista, int duration) {
        Map<String, String> datos = Peticiones.map("method", "track.updateNowPlaying", "track", titulo, "artist", artista, "duration", "" + duration, "sk", SK);
        HashMap<String, String> peticion = Peticiones.request(datos);
        return peticion;
    }
}
