package com.melchor629.musicote.scrobbler;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

/**
 * Autentificador de Last.FM para el musicote
 * @author melchor
 * @TODO <code>username</code> y <code>password</code> cojerlos de los datos de la app 
 */
public class Auth {

	private static final String TAG      = "Scrobbler->Auth";
	private static final String username = "melchor629"; //TODO Poner menu de ajustes y cojerlo de alli
	private static final String password = "andurriales"; //TODO lo que dise arriva
	
	public Auth(){
		
	}
	
	/**
	 * Hace todo lo que tiene dentro este java y te saca el SK de la sesión
	 * @return 
	 */
	public String getSK(){
		Log.e(TAG, "Last.FM sesión iniciada");
		return AuthParser(Peticiones.HTTPSpost(sign()));
	}
	
	/**
	 * Crea la petición para hacer la petición de autenticación
	 * @return out Petición creada
	 */
	private static String sign(){
		Map<String, String> datos = Peticiones.map("method","auth.getMobileSession","username",username,"password", password);
		String request = Peticiones.request(datos);
		return request;
	}
	
	/**
	 * Obtiene el código de la sesión
	 * @param json
	 * @return
	 */
	private static String AuthParser(String json){
		String SK = null;
		try {
			JSONObject autho = new JSONObject(json);
			JSONArray auth = autho.getJSONArray("session");
			String[] sk = new String[auth.length()];
			
			for(int i=0; i<auth.length(); i++){
				JSONObject obj = auth.getJSONObject(i);
				
				String key = obj.getString("key");
				
				sk[i] = key;
			}
			SK = sk[1];
		} catch (Exception e) {
			Log.e(TAG,"Error: "+ e.toString());
		}
		return SK;
	}
}
