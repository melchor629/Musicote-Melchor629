package com.melchor629.musicote.scrobbler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

/**
 * Autentificador de Last.FM para el musicote
 * @author melchor
 * @TODO <code>username</code> y <code>password</code> cojerlos de los datos de la app 
 */
public class Auth {

	private static final String APIkey   = "201a5fdd42fd8cc5577fd0646b3e8ba7";
	private static final String Secret   = "8a5b2c73afdd9f1a585754d52449f0cd";
	private static final String url      = "https://ws.audioscrobbler.com/2.0/";
	private static final String TAG      = "Scrobbler->Auth";
	private static final String username = "melchor629"; //TODO Poner menu de ajustes y cojerlo de alli
	private static final String password = "andurriales"; //TODO lo que dise arriva
	private static final String userAgent= "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17";
	
	public String sk = null;
	
	public Auth(){
		
	}
	
	public String getSK(){ //TODO Poner comentarios chulos de esos xD
		return openUrl(sign());
	}
	
	/**
	 * Crea la petición para hacer la petición de autenticación
	 * @return out Petición creada
	 */
	public String sign(){
		String texto = "api_key"+APIkey+"methodauth.getMobileSession"+"password"+password+"username"+username+""+Secret;
		byte[] textBytes = texto.getBytes();
        MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG,"Error: "+ e.toString());
		}
        md.update(textBytes);
        byte[] codigo = md.digest();
        String request = "method=auth.getMobileSession"+"&api_key="+APIkey+"&username="+username+"&password="+password+"&api_sig="+codigo.toString();
		return request;
	}
	
	/**
	 * Crea envia la petición de autenticación a la web de Last.fm
	 * @param request Petición creada a través de {@link sign}
	 * @return out String con el xml/json de la petición
	 */
	public String openUrl(String request){
		String out = null;
		InputStream is = null;
    	try {
			URL Url = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept-Charset","utf-8");
			conn.setRequestProperty("Content-type","application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			
			OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
			BufferedWriter writer = new BufferedWriter(os);
			writer.write(request);
			
			conn.connect();
			int response = conn.getResponseCode();
			if(response==200)
				is = conn.getInputStream();

			writer.close();
			conn.disconnect();
		} catch (MalformedURLException e) {
			Log.e(TAG,"Error: "+ e.toString());
		} catch (IOException e) {
			Log.e(TAG,"Error: "+ e.toString());
		}
    	Reader reader;
		try {
			reader = new InputStreamReader(is, "UTF-8");
	        char[] buffer = new char[1000];
	        reader.read(buffer);
	        out = new String(buffer);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,"Error: "+ e.toString());
		} catch (IOException e) {
			Log.e(TAG,"Error: "+ e.toString());
		}
		Log.e(TAG,out);
		return out;
	}
}
