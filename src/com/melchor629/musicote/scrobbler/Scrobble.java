package com.melchor629.musicote.scrobbler;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Envia scrobblings
 * @author melchor
 *
 */
public class Scrobble {

    /**
     * SK code from <link>Auth.SK</link>
     */
    private static final String SK = Auth.SK;

    /**
     * Working title
     */
    private String titulo = null;
    
    /**
     * Working artist
     */
    private String artista = null;

    /**
     * Constructor for the class <i>Scrobble</i>
     * @param Titulo <i>Song title</i>
     * @param Artista <i>Song artist</i>
     */
    public Scrobble(String Titulo, String Artista){
        titulo = Titulo;
        artista = Artista;
    }
    
    private final String TAG = "Scrobbler->Scrobble";
    private AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Envia el scrobbling a Last.FM
     * @return status Estado resultante del Scrobbling
     */
    public int scrobble(){
        int status = 0;
        long timestamp = System.currentTimeMillis()/1000;
        final HashMap<String, String> sign = sign(titulo, artista, timestamp);
        client.post(Peticiones.uRl, new RequestParams(sign), new AsyncHttpResponseHandler() {
        @SuppressWarnings("deprecation")
        @Override
           public void onSuccess(String response) {
               Log.d(TAG, response);
               int status = Peticiones.error(response);
               if(status != 0) {
                   switch(status) {
                       case 8:
                       case 16:
                           Peticiones.HTTPpost(sign);
                           break;
                   }
               }
           }
        });
        this.nowPlaying();
        //Make this variables again null for other uses
        titulo = null;
        artista = null;
        return status;
    }

    /**
     * Envia a Last.FM que estás escuchando dicha canción
     * @return status Estado resultante del updateNowPlaying
     */
    public int nowPlaying(){
        int status = 0;
        final HashMap<String, String> sign = sign(titulo, artista);
        client.post(Peticiones.uRl, new RequestParams(sign), new AsyncHttpResponseHandler() {
        @SuppressWarnings("deprecation")
        @Override
           public void onSuccess(String response) {
               Log.d(TAG, response);
               int status = Peticiones.error(response);
               if(status != 0) {
                   switch(status) {
                       case 8:
                       case 16:
                           Peticiones.HTTPpost(sign);
                           break;
                   }
               }
           }
        });
        return status;
    }

    private HashMap<String, String> sign(String titulo, String artista, long time){
        Map<String, String> datos = Peticiones.map("method","track.scrobble","track",titulo,"artist",artista,"timestamp",""+time,"sk",SK);
        HashMap<String, String> peticion = Peticiones.request(datos);
        return peticion;
    }

    private HashMap<String, String> sign(String titulo, String artista){
        Map<String, String> datos = Peticiones.map("method","track.updateNowPlaying","track",titulo,"artist",artista,"sk",SK);
        HashMap<String, String> peticion = Peticiones.request(datos);
        return peticion;
    }
}
