package com.melchor629.musicote;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;

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
import java.util.ArrayList;
import java.util.HashMap;

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
public class Utils {

    /**
     * JSONObject
     * Descarga y carga un JSON a JSONObject
     * Download & load a JSON to JSONObetct
     *
     * @param url Url with a JSON object
     * @return JSONObject jObj
     */
    public static JSONObject getJSONFromUrl(String url) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        InputStream is = null;
        String json = "{}";
        JSONObject jObj = null;
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
                    is, "UTF8"), 8192);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
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

    public static ArrayList getHashMapFromUrl(String url) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HashMap map = new HashMap();
        try {
            long time = System.currentTimeMillis();
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream is = httpEntity.getContent();

            //BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8192);
            StringBuilder sb = new StringBuilder();
            byte[] buff = new byte[256];
            int length = is.read(buff);
            sb.append(new String(buff, 0, length));
            while((length = is.read(buff)) != -1) {
                sb.append(new String(buff, 0, length));
            }
            is.close();
            Log.d("MainActivity", String.format("JSON Downloaded in %dms", System.currentTimeMillis() - time));

            time = System.currentTimeMillis();
            Gson gson = new Gson();
            String s = sb.toString();
            map = gson.fromJson(s, map.getClass());
            Log.d("MainActivity", String.format("JSON parsed in %dms", System.currentTimeMillis() - time));
            return (ArrayList) map.get("canciones");
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HostTest
     * Sirve para comprobar si está encendido el PC
     * Nothing to see here...
     *
     * @param host HOST IP
     * @return int response
     */
    public static int HostTest(String host) {
        int response = 0;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL urlhttp = new URL("http://" + host);
            HttpURLConnection http = (HttpURLConnection) urlhttp.openConnection();
            http.setReadTimeout(1000);
            response = http.getResponseCode();
            http.disconnect();
        } catch (Exception e) {
            Log.e("Comprobando", "Excepción HTTPURL: " + e.toString() + " " + host);
        }

        return response;
    }

    public static String getUrl(String archivo) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                archivo.substring(archivo.lastIndexOf("/")+1));
        if(file.exists())
            return file.getAbsolutePath();
        return String.format("http://%s%s",MainActivity.url, archivo);
    }

    public static boolean isDownloaded(String archivo) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                archivo.substring(archivo.lastIndexOf("/")+1));
        return file.exists();
    }
}
