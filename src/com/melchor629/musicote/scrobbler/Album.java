package com.melchor629.musicote.scrobbler;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.util.Log;

/**
 * Gets album things from Last.FM API
 * @author melchor9000
 *
 */
public class Album {

    public String album;
    public String artista;
    public String albumUrl;
    
    private AsyncHttpClient client = new AsyncHttpClient();
    private final String TAG = "Scrobbler->Album";
    
    /**
     * Gets album things from Last.FM API
     * @param artista <i>The album artist</i>
     * @param album <i>The album</i>
     */
    public Album(String artista, String album) {
        if (album == null || artista == null)
            new Throwable("Falta el álbum ("+album+") o el artista ("+artista+")");
        this.artista = artista;
        this.album = album;
    }
    
    /**
     * Gets info from an album
     */
    public JSONObject getInfo() {
        final HashMap<String, String> sign = sign(artista, album);
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
               
               JSONObject j = null;
               try {
                   j = Peticiones.getJSONObject(response);

                   JSONObject album = j.getJSONObject("album");
                   String alb = getAlbumUrl(album, 3);
               } catch (JSONException e) {
                   Log.e("Last.FM->Album","Error: "+ e.toString());
               }
           }
        });//TODO hacer que esta parte funcione a la perfección
        return null;
    }
    
    /**
     * Gets the url album image
     * @param album <i>JSONObject with the album data</i>
     * @param id <i>ID of the image size [0-5]</i>
     * @return <i>The url in a String</i>
     */
    public String getAlbumUrl(JSONObject album, int id) {
        if(id > 5 || id < 0)
            id = 3;
        try {
            JSONArray image = album.getJSONArray("image");
            JSONObject images = image.getJSONObject(id);
            albumUrl = images.getString("#text");
            return albumUrl;
        } catch (JSONException e) {
            Log.e("Last.FM->Album","Error: "+ e.toString());
        } catch (NullPointerException e) {
            Log.e("Last.FM->Album", "Last.FM no ha encontrado el álbum");
        }
        return null;
    }
    
    private HashMap<String, String> sign(String artista, String album){
        Map<String, String> datos = Peticiones.map("method","album.getinfo","artist",artista,"album",album);
        HashMap<String, String> peticion = Peticiones.request(datos);
        return peticion;
    }
}
