package com.melchor629.musicote.scrobbler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

/**
 * Hace las peticiones al servicio REST de Last.FM
 * @author melchor
 *
 */
public class Peticiones {
	
	public static final String APIkey   = "201a5fdd42fd8cc5577fd0646b3e8ba7";
	public static final String url      = "https://ws.audioscrobbler.com/2.0/";
	
	private static final String TAG		= "Scrobbler->Peticiones";
	private static final String Secret   = "8a5b2c73afdd9f1a585754d52449f0cd";
	
	/**
	 * Envia una petición a Last.FM por HTTPS y POST
	 * @param request Petición creada a través de {@link sign}
	 * @return out String con el xml/json de la petición
	 */
	public static String HTTPSpost(String request){
		String out = null;
		InputStream is = null;
		HttpsURLConnection conn = null;
    	try {
    		request += "&format=json";
			java.net.URL Url = new URL(null, url);
			trustAllHosts();
			HttpsURLConnection.setFollowRedirects(true);
			conn = (HttpsURLConnection) Url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept-Charset","utf-8");
			conn.setRequestProperty("Content-type","application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			DataOutputStream os = new DataOutputStream(conn.getOutputStream());
			os.writeBytes(request);
			os.flush();
			os.close();
			
			Log.e(TAG, "os = "+conn.getOutputStream().toString());
			
			conn.connect();
			int response = conn.getResponseCode();
			Log.e(TAG, "Respuesta de Last.FM: "+response);
			if(response==200){
				is = conn.getInputStream();
			}

			if(is != null){
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				out = "";
				while((line = reader.readLine()) != null){
					out += "\n"+line;
				}
				reader.close();
			}else{
				Log.e(TAG,"Last.FM: is a venido vacio (null)");
				out = "nulo";
			}
		} catch (MalformedURLException e) {
			Log.e(TAG,"Error autenticando: "+ e.toString());
		} catch (IOException e) {
			Log.e(TAG,"Error autenticando: "+ e.toString());
		} finally {
			if(conn != null){
				conn.disconnect();
			}
		}
		
		Log.e(TAG,out);
		return out;
	}
	
	/**
	 * Crea la <i>api_sig</i> y el texto a enviar 
	 * @param Map<String,String> s
	 * @return String
	 */
	public static String request(Map<String,String> s){
		TreeMap <String, String> params = new TreeMap<String, String>(s);
		params.put("api_key", APIkey);
		StringBuilder b = new StringBuilder(100);
		for (Entry<String, String> entry : params.entrySet()) {
			b.append(entry.getKey());
			b.append(entry.getValue());
		}
		b.append(Secret);
		String c = b.toString();
		Log.e("", c);
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG,"Error: "+ e.toString());
		}
		byte[] bytes = null;
		try {
			bytes = md.digest(c.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,"Error: "+ e.toString());
		}
		StringBuilder d = new StringBuilder(32);
		for (byte aByte : bytes) {
			String hex = Integer.toHexString((int) aByte & 0xFF);
			if (hex.length() == 1)
				d.append('0');
			d.append(hex);
		}
		
		params.put("api_sig", d.toString());
		StringBuilder builder = new StringBuilder(200);
		for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			builder.append(entry.getKey());
			builder.append('=');
			try {
				builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG,"Error: "+ e.toString());
			}
			if (it.hasNext())
				builder.append('&');
		}
		return builder.toString();
	}
	
	public static Map<String, String> map(String... strings) {
		if (strings.length % 2 != 0)
			throw new IllegalArgumentException("strings.length % 2 != 0");
		Map<String, String> mp = new HashMap<String, String>();
		for (int i = 0; i < strings.length; i += 2) {
			mp.put(strings[i], strings[i + 1]);
		}
		return mp;
	}
	
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
			}
			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
