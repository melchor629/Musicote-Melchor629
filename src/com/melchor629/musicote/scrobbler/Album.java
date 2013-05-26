package com.melchor629.musicote.scrobbler;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		String sign = sign(artista, album);
		String request = Peticiones.HTTPpost(sign);
		JSONObject j = null;
		try {
			j = Peticiones.getJSONObject(request);

			JSONObject album = j.getJSONObject("album");
			JSONArray image = album.getJSONArray("image");
			JSONObject images = image.getJSONObject(4);
			albumUrl = images.getString("#text");
			
			return album;
		} catch (JSONException e) {
			Log.e("Last.FM->Album","Error: "+ e.toString());
		}
		return null;
	}
	
	/**
	 * Gets the url album image
	 * @return <i>The url in a String</i>
	 */
	public String getAlbumUrl() {
		return albumUrl;
	}
	
    private String sign(String artista, String album){
        Map<String, String> datos = Peticiones.map("method","album.getinfo","artist",artista,"album",album);
        String peticion = Peticiones.request(datos);
        return peticion;
    }
}
