package com.melchor629.musicote;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

    public static ArrayList getHashMapFromUrl(String url) {
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
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
                archivo.substring(archivo.lastIndexOf("/") + 1));
        if(file.exists())
            return file.getAbsolutePath();
        return String.format("http://%s%s%s", MainActivity.HOST, MainActivity.BASE_URL, archivo);
    }

    public static boolean isDownloaded(String archivo) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                archivo.substring(archivo.lastIndexOf("/") + 1));
        return file.exists();
    }

    public static void setFileAsDownloaded(int pos, boolean a) {
        DB db = new DB(MainActivity.appContext);
        SQLiteDatabase d = db.getWritableDatabase();
        if(d == null) return;
        d.execSQL(String.format("UPDATE %s SET %s=\"%b\" WHERE id = \"%s\"",
                DB_entry.TABLE_CANCIONES, DB_entry.COLUMN_CANCIONES_DOWNLOADED,
                a, pos));
        d.close();
        MainActivity.songList.get(pos).put("downloaded", a ? "{fa-mobile}" : "{fa-cloud}");
    }
}
