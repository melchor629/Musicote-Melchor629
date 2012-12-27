package com.melchor629.myfirstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
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
    	Log.i("com.melchor629.myfirstapp","Parseador JSON iniciado...");
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
 
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
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
    
    public boolean HostTest(String host, int port) {
        boolean toReturn = false;

        try {
            Socket client1 = new Socket();
            client1.setSoTimeout(10000);
            client1.bind(new InetSocketAddress(host, port));
            InputStream stream = client1.getInputStream();
            byte[] response = new byte[4096];
            int bytes = 0;
            String serverReturnString = null;
            bytes = stream.read(response, 0, response.length);
            serverReturnString = String.valueOf(bytes);
            System.out.println("TestAvailablility: serverReturnString = {0} " + serverReturnString);
            /*if (serverReturnString.toLowerCase().startsWith(responseStartsWith.toLowerCase()))
                toReturn = true;*/
        } catch (Exception ex) // SocketException for connect, IOException for
        {
            System.out.println("TestAvailable - Could not connect to VNC server.  Exception info: ");
            ex.printStackTrace();
        	Log.i("com.melchor629.myfirstactivity", "Error al comprobar el host: "+host+":"+port);
        }

        return toReturn;
    }
}