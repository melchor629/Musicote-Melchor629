package com.melchor629.myfirstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;
import android.util.Log;

public class ParseJSON {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public ParseJSON() {
    	Log.i("com.melchor629.musicote","Parseador JSON iniciado...");
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /**
     * JSONObject
     * Descarga y carga un JSON a JSONObject
     * Download & load a JSON to JSONObetct
     * @param url
     * @return
     */
    public JSONObject getJSONFromUrl(String url) {

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("com.melchor629.myfirstactivity", "UnsupportedEncodingException "+e.toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e("com.melchor629.myfirstactivity", "ClientProtocolException "+e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("com.melchor629.myfirstactivity", "IOException "+e.toString());
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
    /**
     * HostTest
     * Sirve para comprobar si está encendido el PC
     * Nothing to see here...
     * @param host
     * @param port
     * @return
     */
    public boolean HostTest(String host, int port) {
        boolean i = false;

        try {
        	Socket connection = new Socket(host, port);
        	if(connection.isConnected() == true){
        		Log.e("com.melchor629.myfirstactivity","Conexión... conosco a tu padre ("+host+")");
        		i = true;
        	}else{
        		Log.e("com.melchor629.myfirstactivity","Con esta dirección "+host+":"+port+" no hay na");
        		i = false;
        	}

        } catch (Exception ex) // SocketException for connect, IOException for
        {
            ex.printStackTrace();
        	Log.e("com.melchor629.myfirstactivity", "Error al comprobar el host: "+host+":"+port+" | "+ex.toString());
        }

        return i;
    }
}
