package com.melchor629.musicote;

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

/**
 * Musicote App
 * Melchor629 2012
 *
 *    Copyright 2012 Melchor629
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
**/
/**
 * Una clase que descarga un JSON y lo prepara para poderse usar
 * @author melchor
 * 
 */
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
     * @return JSONObject jObj
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
            Log.e("com.melchor629.musicote", "UnsupportedEncodingException "+e.toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e("com.melchor629.musicote", "ClientProtocolException "+e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("com.melchor629.musicote", "IOException "+e.toString());
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
     * @return boolean i
     */
    public boolean HostTest(String host, int port) {
        boolean i = false;

        try {
            Socket connection = new Socket(host, port);
            if(connection.isConnected() == true){
                Log.e("com.melchor629.musicote","Conexión... conosco a tu padre ("+host+")");
                i = true;
            }else{
                Log.e("com.melchor629.musicote","Con esta dirección "+host+":"+port+" no hay na");
                i = false;
            }

        } catch (Exception ex) // SocketException for connect, IOException for
        {
            ex.printStackTrace();
            Log.e("com.melchor629.musicote", "Error al comprobar el host: "+host+":"+port+" | "+ex.toString());
        }

        return i;
    }
}