package com.melchor629.musicote;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static String apiUrl(String apiUrl) {
        return String.format("http://%s%s%s", MainActivity.HOST, MainActivity.BASE_URL, apiUrl);
    }

    public static ArrayList getHashMapFromUrl(String url) throws IOException {
        try {
            return (ArrayList) getJsonFromUrl(url).get("canciones");
        } catch(IllegalStateException e) {
            Log.e("Utils", "El archivo recibido no es json");
        }
        return null;
    }

    public static Map getJsonFromUrl(String url) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(getStringFromUrl(url), HashMap.class);
    }

    public static String getStringFromUrl(String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        String ret = null;

        try {
            InputStream is = new BufferedInputStream(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            byte[] buff = new byte[512];
            int length = is.read(buff);

            sb.append(new String(buff, 0, length));
            while((length = is.read(buff)) != -1) {
                sb.append(new String(buff, 0, length));
            }

            ret = sb.toString();
        } finally {
            conn.disconnect();
        }

        return ret;
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

    public static String toBase64(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }

    public static String fromBase64(String b64) {
        return new String(Base64.decode(b64, Base64.DEFAULT));
    }

    public static String urlEncode(String enc) {
        try {
            return URLEncoder.encode(enc, "UTF-8");
        } catch(Exception ignore) {}
        return null;
    }
}
