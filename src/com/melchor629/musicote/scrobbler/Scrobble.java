package com.melchor629.musicote.scrobbler;

import java.util.Map;

/**
 * Envia scrobblings
 * TODO enviar el now playing
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

    /**
     * Envia el scrobbling a Last.FM
     * @return status Estado resultante del Scrobbling
     */
    public int scrobble(){
        int status = 0;
        long timestamp = System.currentTimeMillis()/1000;
        String sign = sign(titulo, artista, timestamp);
        String request = Peticiones.HTTPpost(sign);
        status = Peticiones.error(request);
        if(status != 0) {
        	switch(status) {
        		case 8:
        		case 16:
        			this.scrobble();
        			break;
        	}
        }
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
        String sign = sign(titulo, artista);
        String request = Peticiones.HTTPpost(sign);
        status = Peticiones.error(request);
        if(status != 0) {
        	switch(status) {
        		case 8:
        		case 16:
        			this.nowPlaying();
        			break;
        	}
        }
        return status;
    }

    private String sign(String titulo, String artista, long time){
        Map<String, String> datos = Peticiones.map("method","track.scrobble","track",titulo,"artist",artista,"timestamp",""+time,"sk",SK);
        String peticion = Peticiones.request(datos);
        return peticion;
    }

    private String sign(String titulo, String artista){
        Map<String, String> datos = Peticiones.map("method","track.updateNowPlaying","track",titulo,"artist",artista,"sk",SK);
        String peticion = Peticiones.request(datos);
        return peticion;
    }
}
