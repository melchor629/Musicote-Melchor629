package com.melchor629.musicote;

import android.os.StrictMode;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
 *
 * @author melchor
 */
public class ParseJSON {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public ParseJSON() {
        Log.i("com.melchor629.musicote", "Parseador JSON iniciado...");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /**
     * JSONObject
     * Descarga y carga un JSON a JSONObject
     * Download & load a JSON to JSONObetct
     *
     * @param url
     * @return JSONObject jObj
     */
    public JSONObject getJSONFromUrl(String url) {
        if(json.equals("")) {
            Log.e("", "Se ase");
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
                Log.e("com.melchor629.musicote", "UnsupportedEncodingException " + e.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.e("com.melchor629.musicote", "ClientProtocolException " + e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("com.melchor629.musicote", "IOException " + e.toString());
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "UTF8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
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
     *
     * @param host
     * @return int response
     */
    public static int HostTest(String host) {
        int response = 0;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL urlhttp = new URL("http://" + host + "/");
            HttpURLConnection http = (HttpURLConnection) urlhttp.openConnection();
            http.setReadTimeout(1000);
            response = http.getResponseCode();
        } catch (Exception e) {
            Log.e("Comprobando", "Excepción HTTPURL: " + e.toString());
        }

        return response;
    }
}
