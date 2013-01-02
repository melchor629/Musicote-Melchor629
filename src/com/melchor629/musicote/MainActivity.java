package com.melchor629.musicote;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.melchor629.musicote.R;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;
import android.app.ProgressDialog;

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
 * Actividad principal de la App
 * TODO añadir la función de Inicio de sesión
 * @author melchor
 *
 */
public class MainActivity extends ListActivity{

	public final static String EXTRA_MESSAGE = "com.melchor629.musicote.MESSAGE";
	public final static String Last_STRING = "asdasda";

	public static String Last_String = "";
	public static int response = 0;
	public static String url;
	
	private ProgressDialog progressDialog;  

	// contacts JSONArray
	JSONArray contacts = null;

	TextView mTextView; // Member variable for text view in the layout
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Set the user interface layout for this Activity
        // The layout file is defined in the project res/layout/main.xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            Last_String = savedInstanceState.getString(Last_STRING);
        }

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();

		AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> asd = new JSONParseDialog().execute();
		try {
			contactList = asd.get();
		} catch (InterruptedException e) {
			Log.e("AsyncTask","AsyncTask not finished: "+e.toString());
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.e("AsyncTask","AsyncTask not finished: "+e.toString());
			e.printStackTrace();
		}
		/**
		 * Updating parsed JSON data into ListView
		 * */
		ListAdapter adapter = new SimpleAdapter(this, contactList,
				R.layout.list_item,
				new String[] { "titulo", "artista", "album" }, new int[] {
					R.id.name, R.id.email, R.id.mobile });

		setListAdapter(adapter);

		// selecting single ListView item
		ListView lv = getListView();

		// Launching new screen on Selecting Single ListItem
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			// getting values from selected ListItem
				JSONObject tolcoño = null;
				try{
					tolcoño = contacts.getJSONObject(position);
				}catch(Exception e){
					Log.e("com.melchor629.musicote", "138<<"+e.toString()); e.printStackTrace();
				}
				String name = getString(R.string.vacio);
				String cost = getString(R.string.vacio);
				String description = getString(R.string.vacio);
				String album = "-00:00";
				String archivo = "http://"+url+"/multimedia/escucha.php";
				try{
					name = ((TextView) view.findViewById(R.id.name)).getText().toString();
					cost = ((TextView) view.findViewById(R.id.email)).getText().toString();
					description = ((TextView) view.findViewById(R.id.mobile)).getText().toString();
					album = tolcoño.getString("duracion");
					archivo = tolcoño.getString("archivo");
				} catch (Exception e){
					Log.e("com.melchor629.musicote", e.toString());
				}

				// Starting new intent
				Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
				in.putExtra("titulo", name);
				in.putExtra("artista", cost);
				in.putExtra("album", description);
				in.putExtra("duracion", album);
				in.putExtra("archivo", "http://"+url+"/"+archivo);
				startActivity(in);
			}
		});
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        android.os.Debug.stopMethodTracing();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    @Override
    public void finish() {
    	super.finish(); //Always call the superclass method first
    }

    @Override
    public void onStop() {
    	super.onStop();
    	ContentValues values = new ContentValues();
    	values.put(Last_STRING, Last_String);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** Called when the user selects the Send button **/
    public void sendMessage(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	intent.putExtra(Last_STRING, Last_String);
    	startActivity(intent);
    }

    /** Called when the user selects the Send Random button **/
    public void sendMessageRandom(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
		Random rand = new Random();
		int num = rand.nextInt(5-0)+1;
    	String randText = "Error al enviar texto random...";
		switch (num) {
			case 1:
				randText = "Una tortuga empieza con 5 metros de ventaja y el humano nunca alcanzará a la tortuga. ¿Por qué? Preguntaselo a la hdp de Filosofia...";
				break;
			case 2:
				randText = "Los dinosarios d'Albert...";
				break;
			case 3:
				randText = "If you love me, want let me know...";
				break;
			case 4:
				randText = "Musicote: The 2nd Law de Muse";
				break;
			case 5:
				randText = "Cutre Application by Melchor629...";
				break;
			default:
				randText = "El número que ha tret el generador"+ rand +" es incorrect, cagon putes...";
		}
    	String message = randText;
    	intent.putExtra(EXTRA_MESSAGE, message);
    	intent.putExtra(Last_STRING, Last_String);
    	startActivity(intent);
    }
    // Intento de guardar lo ultimo enviado al otro .class
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString(Last_STRING, EXTRA_MESSAGE);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * Clase AsyncTask para descargar el JSON y parsearlo y no desesperar a la peña
     * @author melchor
     *
     */
    private class JSONParseDialog extends AsyncTask<Void, Integer, ArrayList<HashMap<String,String>>> {

    	public String url;
    	public int response;
    	public JSONArray contacts;

		protected void onPreExecute()
		{
			//Create a new progress dialog
			progressDialog = new ProgressDialog(MainActivity.this);
			//Set the progress dialog to display a horizontal progress bar
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			//Set the dialog title to 'Loading...'
			progressDialog.setTitle("Musicote en camino...");
			//Set the dialog message to 'Loading application View, please wait...'
			progressDialog.setMessage("Cargando datos del servidor.\nEspere...");
			//This dialog can't be canceled by pressing the back key
			progressDialog.setCancelable(false);
			//This dialog isn't indeterminate
			progressDialog.setIndeterminate(false);
			//The maximum number of items is 100
			progressDialog.setMax(100);
			//Set the current progress to zero
			progressDialog.setProgress(0);
			//Display the progress dialog
			progressDialog.show();
		}

    	protected ArrayList<HashMap<String, String>> doInBackground(Void... params){
    		// Hashmap for ListView
            final ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();

            // Creating JSON Parser instance
            ParseJSON jParser = new ParseJSON();

            synchronized (this){
            	// La app prueba en busca de la dirección correcta
            	if(jParser.HostTest("192.168.1.128",80)){
            		url = "192.168.1.128";
            	}else if(jParser.HostTest("reinoslokos.no-ip.org",80)){
            		url = "reinoslokos.no-ip.org";
            	}else if(jParser.HostTest("melchor629.no-ip.org",80)){
            		url = "melchor629.no-ip.org";
            	}
            	publishProgress(10);
            	if(url!=null){
            		// getting JSON string from URL
            		try{
            			URL urlhttp = new URL("http://"+url+"/multimedia/musicoteApi.php");
            			HttpURLConnection http = (HttpURLConnection) urlhttp.openConnection();
            			response = http.getResponseCode();
            		} catch(Exception e){
            			Log.e("com.melchor629.musicote", "Excepción HTTPURL: "+e.toString());
            		}
            		publishProgress(25);
            		if(response==200){
            			JSONObject json = jParser.getJSONFromUrl("http://"+url+"/multimedia/musicoteApi.php");
            			publishProgress(30);
            			try {
            				// Getting Array of Songs
            				contacts = json.getJSONArray("canciones");
            				publishProgress(40);

            				// looping through All Songs
            				for(int i = 0; i < contacts.length(); i++){
            					JSONObject c = contacts.getJSONObject(i);
            				
            					int counter = 40 + ((i/contacts.length())/2);
            					publishProgress(counter);
            					// Storing each json item in variable
            					String id = c.getString("id");
            					String archivo = c.getString("archivo");
            					String titulo = c.getString("titulo");
            					String artista = c.getString("artista");
            					String album = c.getString("album");
            					String duracion = c.getString("duracion");

            					// creating new HashMap
            					HashMap<String, String> map = new HashMap<String, String>();

            					// adding each child node to HashMap key => value
            					map.put("id", id);
            					map.put("titulo", titulo);
            					map.put("artista", artista);
            					map.put("album", album);
            					map.put("archivo", "http://"+url+"/"+archivo);
            					map.put("duracion", duracion);

            					// adding HashList to ArrayList
            					contactList.add(map);
            				}
            			} catch (JSONException e) {
            				e.printStackTrace();
            				Log.i("com.melchor629.musicote","Excepción encontrada: "+e.toString());
            			}
            			publishProgress(90);
        			}

        			publishProgress(100);
        			return contactList;
            	}else{
            		Log.i("ServerHostDetector","Er ordenata de mershor ta apagao...");
            		Toast.makeText(getApplicationContext(), "Ningún servidor activo...", Toast.LENGTH_SHORT).show();
            		return null;
            	}
        	}
        }
    	
    	protected void onProgressUpdate(Integer... progress){
    		progressDialog.setProgress(progress[0]);
    	}
    	
    	protected void onPostExecute(ArrayList<HashMap<String, String>> result){
    		super.onPostExecute(result);
    		progressDialog.dismiss();
    	}
    }
    
}
