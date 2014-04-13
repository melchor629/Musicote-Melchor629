package com.melchor629.musicote.scrobbler;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Gets album things from Last.FM API
 *
 * @author melchor9000
 */
public class Album {

    private String album;
    private String artista;
    private String albumUrl;

    private final String TAG = "Scrobbler->Album";

    /**
     * Gets album things from Last.FM API
     *
     * @param artista <i>The album artist</i>
     * @param album   <i>The album</i>
     */
    public Album(String artista, String album) {
        if(album == null || artista == null)
            new Throwable("Falta el álbum (" + album + ") o el artista (" + artista + ")");
        this.artista = artista;
        this.album = album;
        this.albumUrl = null;
    }

    /** Gets info from an album */
    @SuppressWarnings ("deprecation")
    public String getInfo() {
        HashMap<String, String> sign = sign(artista, album);
        String request = Peticiones.HTTPpost(sign);
        JSONObject j = null;
        try {
            j = Peticiones.getJSONObject(request);

            JSONObject album = j.getJSONObject("album");

            /*JSONArray image = album.getJSONArray("image");
            JSONObject images = image.getJSONObject(4);
            albumUrl = images.getString("#text");
            return albumUrl;*/
            for(int i = 5; i > 0; i--) {
                if(albumUrl == null)
                    albumUrl = getAlbumUrl(album, i);
                else
                    return albumUrl;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e.toString());
        }
        return null;
    }

    /**
     * Gets the url album image
     *
     * @param album <i>JSONObject with the album data</i>
     * @param id    <i>ID of the image size [0-5]</i>
     * @return <i>The url in a String</i>
     */
    String getAlbumUrl(JSONObject album, int id) {
        if(id > 5 || id < 0)
            id = 3;
        try {
            JSONArray image = album.getJSONArray("image");
            JSONObject images = image.getJSONObject(id);
            albumUrl = images.getString("#text");
            return albumUrl;
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            Log.e(TAG, "Last.FM no ha encontrado el álbum");
        }
        return null;
    }

    private HashMap<String, String> sign(String artista, String album) {
        Map<String, String> datos = Peticiones.map("method", "album.getinfo", "artist", artista, "album", album);
        return Peticiones.request(datos);
    }
}
