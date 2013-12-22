package com.melchor629.musicote.scrobbler;

import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Hace las peticiones al servicio REST de Last.FM
 *
 * @author melchor
 */
public class Peticiones {

    /** API key from <link>Last.FM</link> */
    public static final String APIkey = "201a5fdd42fd8cc5577fd0646b3e8ba7";

    /** Url for the https API */
    public static final String url = "https://ws.audioscrobbler.com/2.0/";

    /** Url for the http API */
    public static final String uRl = "http://ws.audioscrobbler.com/2.0/";

    /** Array with all descriptions for every error that could give Last.FM */
    public static String[] errorM = {
            "Correcto", "Unknown Error", "Invalid service - This service does not exist", "Invalid Method - No method with that name in this package",
            "Authentication Failed - You do not have permissions to access the service", "Invalid format - This service doesn't exist in that format",
            "Invalid parameters - Your request is missing a required parameter", "Invalid resource specified", "Operation failed - Something else went wrong",
            "Invalid session key - Please re-authenticate", "Invalid API key - You must be granted a valid key by last.fm",
            "Service Offline - This service is temporarily offline. Try again later.", "", "Invalid method signature supplied", "", "",
            "The service is temporarily unavailable, please try again.", "", "", "", "", "", "", "", "", "",
            "Suspended API key - Access for your account has been suspended, please contact Last.fm", "", "",
            "Rate limit exceeded - Your IP has made too many requests in a short period"
    };
    /** Tag for the Logging Android system */
    private static final String TAG = "Scrobbler->Peticiones";

    /** Secret API key */
    private static final String Secret = "8a5b2c73afdd9f1a585754d52449f0cd";

    /**
     * Envia una petición a Last.FM por HTTPS y POST
     *
     * @param request Petición creada a través de {@link sign}
     * @return out String con el xml/json de la petición
     */
    public static String HTTPSpost(HashMap<String, String> params) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StringBuilder builder = new StringBuilder(200);
        for(Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, String> entry = it.next();
            builder.append(entry.getKey());
            builder.append('=');
            try {
                builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Error: " + e.toString());
            }
            if(it.hasNext())
                builder.append('&');
        }
        String request = builder.toString();

        String out = null;
        InputStream is = null;
        HttpsURLConnection conn = null;
        try {
            java.net.URL Url = new URL(null, url);
            trustAllHosts();

            HttpsURLConnection.setFollowRedirects(true);
            conn = (HttpsURLConnection) Url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(request);
            os.flush();
            os.close();

            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "Respuesta de Last.FM: " + response);
            if(response == 200) {
                is = conn.getInputStream();
            }

            if(is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                out = "";
                while((line = reader.readLine()) != null) {
                    out += "\n" + line;
                }
                reader.close();
            } else {
                Log.e(TAG, "Last.FM: is a venido vacio (null)");
                out = "nulo";
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error autenticando: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error autenticando: " + e.toString());
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }

        return out;
    }

    /**
     * Envia una petición a Last.FM por HTTP y POST. Usa en todo caso:<br>
     * <code>
     * AsyncHttpClient client = new AsyncHttpClient();<br>
     * client.post(uRl, new RequestParams(request), new AsyncHttpResponseHandler() {<br>
     * Override<br>
     * public void onSuccess(String response) {<br>
     * Log.d(TAG, response);<br>
     * }<br>
     * });</code>
     *
     * @param request Petición creada a través de {@link sign}
     * @return out String con el xml/json de la petición
     */
    @Deprecated
    public static String HTTPpost(final HashMap<String, String> params) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StringBuilder builder = new StringBuilder(200);
        for(Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, String> entry = it.next();
            builder.append(entry.getKey());
            builder.append('=');
            try {
                builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Error: " + e.toString());
            }
            if(it.hasNext())
                builder.append('&');
        }
        String request = builder.toString();

        String out = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            request += "&format=json";
            java.net.URL Url = new URL(null, url);
            trustAllHosts();
            HttpURLConnection.setFollowRedirects(true);
            conn = (HttpURLConnection) Url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(request);
            os.flush();
            os.close();

            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "Respuesta de Last.FM: " + response);
            if(response == 200) {
                is = conn.getInputStream();
            }

            if(is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                out = "";
                while((line = reader.readLine()) != null) {
                    out += "\n" + line;
                }
                reader.close();
            } else {
                Log.e(TAG, "Last.FM: is a venido vacio (null)");
                out = "nulo";
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error autenticando: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error autenticando: " + e.toString());
            e.printStackTrace();
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }

        return out;
    }

    /**
     * Crea la <i>api_sig</i> y el texto a enviar. Tienes que ponerlo del en un <code>Map&lt;String,
     * String></code> con todo lo necesario menos la api_key y el secret (<b>IMPORTANTE</b> SK no
     * incluido)
     *
     * @param s A map with the data
     * @return String the String with all the Data
     */
    public static HashMap<String, String> request(Map<String, String> s) {
        TreeMap<String, String> params = new TreeMap<String, String>(s);
        params.put("api_key", APIkey);
        StringBuilder b = new StringBuilder(100);
        for(Entry<String, String> entry : params.entrySet()) {
            b.append(entry.getKey());
            b.append(entry.getValue());
        }
        b.append(Secret);
        String c = b.toString();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error: " + e.toString());
        }
        byte[] bytes = null;
        try {
            bytes = md.digest(c.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error: " + e.toString());
        }
        StringBuilder d = new StringBuilder(32);
        for(byte aByte : bytes) {
            String hex = Integer.toHexString((int)aByte & 0xFF);
            if(hex.length() == 1)
                d.append('0');
            d.append(hex);
        }

        params.put("api_sig", d.toString());
        params.put("format", "json");
        //StringBuilder builder = new StringBuilder(200);
        HashMap<String, String> p = new HashMap<String, String>();
        for(Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, String> entry = it.next();
            /*builder.append(entry.getKey());
            builder.append('=');
            try {
                builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.toString() + " (" + entry.getKey() + "-" + entry.getValue() + ")");
            }
            if(it.hasNext())
                builder.append('&');*/
            p.put(entry.getKey(), entry.getValue());
        }
        //return builder.toString();
        return p;
    }

    /**
     * Procesa el mensaje enviado de Last.FM en busca de errores, y si los tiene saldrá en el resultado
     *
     * @param request <i>Respuesta de Last.FM</i>
     * @return out <i>Código de error</i>
     */
    public static int error(String request) {
        int out = 0;
        JSONObject jObj;
        try {
            jObj = getJSONObject(request);
            if(jObj.has("error"))
                out = jObj.getInt("error");
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Procesa el mensaje enviado de Last.FM a un objeto JSON para después poderlo manejar
     *
     * @param request <i>Repuesta de Last.FM</i>
     * @return JSONObject <i>El JSONObject</i>
     * @throws JSONException <i>Si no se ha podido converitir</i>
     */
    public static JSONObject getJSONObject(String request) throws JSONException {
        return new JSONObject(request);
    }

    public static Map<String, String> map(String... strings) {
        if(strings.length % 2 != 0)
            throw new IllegalArgumentException("strings.length % 2 != 0");
        Map<String, String> mp = new HashMap<String, String>();
        for(int i = 0; i < strings.length; i += 2) {
            mp.put(strings[i], strings[i + 1]);
        }
        return mp;
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
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
            }
        };

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
