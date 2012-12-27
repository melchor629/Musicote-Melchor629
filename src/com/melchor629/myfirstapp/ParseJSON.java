package com.melchor629.myfirstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ParseJSON extends MyFirstActivity {
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    if (android.os.Build.VERSION.SDK_INT > 9) {
    	StrictMode.ThreadPolicy policy = 
    	        new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	StrictMode.setThreadPolicy(policy);
    	}
    String readTwitterFeed = readTwitterFeed();
    // Get the TableLayout
    TableLayout tl = (TableLayout) findViewById(R.id.canciones);
    try {
    	Log.i(ParseJSON.class.getName(),">>> Empezando el parse... <<<");
    	// Hashmap for ListView
        // TODO Si no funciona el sistema actual mira la siguiente linia y http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
    	// ArrayList<HashMap<String, String>> musicoteList = new ArrayList<HashMap<String, String>>();
      /*JSONArray jsonArray = new JSONArray(readTwitterFeed);
      Log.i(ParseJSON.class.getName(),
          "Number of entries " + jsonArray.length());
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);*/
    	JSONObject jsonObject = new JSONObject(readTwitterFeed);
	for(int i = 0; i < jsonObject.length(); i++){
        // Create a TableRow and give it an ID
        TableRow tr = new TableRow(this);
        tr.setId(100+i);
        tr.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        // Create a TextView to house the name of the province
        TextView labelTV = new TextView(this);
        labelTV.setId(200+i);
        labelTV.setText(jsonObject.getString("titulo"));
        labelTV.setTextColor(Color.BLACK);
        labelTV.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        tr.addView(labelTV);

        // Create a TextView to house the value of the after-tax income
        TextView valueTV = new TextView(this);
        valueTV.setId(i);
        valueTV.setText(jsonObject.getString("artista"));
        valueTV.setTextColor(Color.BLACK);
        valueTV.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        tr.addView(valueTV);

        // Add the TableRow to the TableLayout
        tl.addView(tr, new TableLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        Log.i(ParseJSON.class.getName(), jsonObject.getString("titulo"));
      }
    } catch (Exception e) {
    	//e.printStackTrace();
  		Intent intent = new Intent(this, DisplayMessageActivity.class);
  		String message = "La aplicación ha encontrado esta excepción: " + e.toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	intent.putExtra(Last_STRING, Last_String);
    	startActivity(intent);
    }
  }

  public String readTwitterFeed() {
    StringBuilder builder = new StringBuilder();
    HttpClient client = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet("http://192.168.1.128/multimedia/musicoteApi.php");
    try {
      HttpResponse response = client.execute(httpGet);
      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (statusCode == 200) {
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
        }
      } else {
        Log.e(ParseJSON.class.toString(), "Failed to download file");
      }
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return builder.toString();
  }
} 