package com.melchor629.musicote.scrobbler;

import java.util.Map;

import android.util.Log;

/**
 * Envia scrobblings
 * TODO enviar el now playing
 * @author melchor
 *
 */
public class Scrobble {

	private static final String SK = Auth.SK;
	private static final String TAG = "Scrobbler->Scrobble";
	
	private static String titulo = null;
	private static String artista = null;

	public Scrobble(String Titulo, String Artista){
		titulo = Titulo;
		artista = Artista;
	}
	
	/**
	 * Envia el scrobbling a Last.FM
	 * @return status Estado resultante del Scrobbling TODO crear el sistema
	 */
	public int scrobble(){
		int status = 0;
		long timestamp = System.currentTimeMillis()/1000;
		String sign = sign(titulo, artista, timestamp);
		Log.e(TAG, Peticiones.HTTPpost(sign));
		//Make this variables again null for other uses
		titulo = null;
		artista = null;
		return status;
	}
	
	/**
	 * Envia a Last.FM que estás escuchando dicha canción
	 * @return status Estado resultante del updateNowPlaying TODO crear el sistema
	 */
	public int nowPlaying(){
		int status = 0;
		String sign = sign(titulo, artista);
		Log.e(TAG, Peticiones.HTTPpost(sign));
		return status;
	}
	
	private String sign(String titulo, String artista, long time){
		Map<String, String> datos = Peticiones.map("method","track.scrobble","track",titulo,"artist",artista,"timestamp",""+time,"sk",SK);
		String petición = Peticiones.request(datos);
		return petición;
	}
	
	private String sign(String titulo, String artista){
		Map<String, String> datos = Peticiones.map("method","track.updateNowPlaying","track",titulo,"artist",artista,"sk",SK);
		String petición = Peticiones.request(datos);
		return petición;
	}
}
