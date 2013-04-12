package com.melchor629.musicote.scrobbler;

import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

/**
 * Autentificador de Last.FM para el musicote
 * @author melchor
 * @TODO <code>username</code> y <code>password</code> cojerlos de los datos de la app
 */
public class Auth {

	/**
	 * TAG for the Log Android system
	 */
    private static final String TAG = "Scrobbler->Auth";
    
    /**
     * Username to authenticate, always starting as <b>null</b>
     */
    private static String username     = null;
    
    /**
     * Password to authenticate, always starting as <b>null</b>
     */
    private static String password     = null;

    /**
     * Public SK code
     */
    public static String SK = null;

    /**
     * Constructor for Auth
     * @param user <i>Username to authenticate</i>
     * @param pass <i>Password for the username</i>
     */
    public Auth(String user, String pass){
        Log.d(TAG, "Llamado");
        if(user != null && pass != null){
            username = user;
            password = pass;
        }
    }

    /**
     * Hace todo lo que tiene dentro este java y te saca el SK de la sesión
     * @return SK el código de sesión
     */
    public String getSK(){
        Log.d(TAG, "Last.FM sesión iniciada");
        if(SK==null) {
            SK = AuthParser(Peticiones.HTTPSpost(sign()));
            Log.d("Scrobbler->Auth", "Renovando SK");
        }
        return SK;
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
            JSONObject auth = autho.getJSONObject("session");
            String[] sk = new String[auth.length()];

            for(int i=0; i<auth.length(); i++){
                JSONObject obj = auth;

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
