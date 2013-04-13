package com.melchor629.musicote;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.melchor629.musicote.R;

import android.content.pm.ActivityInfo;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends ListActivity {

    public final static String EXTRA_MESSAGE = "com.melchor629.musicote.MESSAGE";
    public final static String Last_STRING = "asdasda";

    public static String Last_String = "";
    public static int response = 0;
    public static String url;

    private ProgressDialog progressDialog;
    private Toast tostado;

    // contacts JSONArray
    private static JSONArray contacts = null;
    private ArrayList<HashMap<String, String>> contactList = null;

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
        
        //Create a new progress dialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Musicote en camino...");
        progressDialog.setMessage("Cargando datos del servidor, espere...");
        progressDialog.setCancelable(false);
        if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
	        progressDialog.setIndeterminate(false);
	        progressDialog.setMax(100);
	        progressDialog.setProgress(0);
        } else {
        	progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
        
        new Thread(new Runnable(){
			@Override
			public void run() {
				Looper.prepare();
		        try {
		        	AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> asd = new JSONParseDialog().execute();
		            contactList = asd.get();
		        } catch (InterruptedException e) {
		            Log.e("AsyncTask","AsyncTask not finished: "+e.toString()); 
		            e.printStackTrace();
		        } catch (Exception e) {
		            Log.e("AsyncTask","AsyncTask not finished: "+e.toString());
		            e.printStackTrace();
		        } 
		        MainActivity.this.runOnUiThread(
	        		new Runnable(){
	        			@Override public void run(){
	        				sis();
	        				try {
        						this.finalize();
	        				} catch (Throwable e) {
	        					Log.e(MainActivity.EXTRA_MESSAGE, "Error: "+ e.toString());
	        				}
        				}
	                }
        		);
		        try {
		        	progressDialog.dismiss();
					this.finalize();
				} catch (Throwable e) {
					Log.e("UIUpdate" ,"Error: "+ e.toString());
				}
			}}).start();
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    private void sis(){
        if(contactList == null){
        	Log.e("MainActivity", "contactList viene vacío...");
        	return;
        }
        /**
         * Updating parsed JSON data into ListView
         * */
        ListAdapter adapter = new SimpleAdapter(this, contactList,
                R.layout.list_item,
                new String[] { "titulo", "artista", "album" }, new int[] {
                    R.id.name, R.id.email, R.id.mobile });

        try {
            setListAdapter(adapter);
        }catch (Exception e){
            Log.e("ServerHostDetector","Er ordenata de mershor ta apagao... "+e.toString());
            tostado = Toast.makeText(MainActivity.this, "Ningún servidor activo...", Toast.LENGTH_LONG);
            tostado.show();
        }

        // selecting single ListView item
        ListView lv = getListView();

        // Launching new screen on Selecting Single ListItem
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
			public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                // getting values from selected ListItem
                JSONObject datos = null;
                try{
                    datos = contacts.getJSONObject(position);
                }catch(Exception e){
                    Log.e("com.melchor629.musicote", "121<<"+e.toString()); e.printStackTrace();
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
                    album = datos.getString("duracion");
                    archivo = datos.getString("archivo");
                } catch (Exception e){
                    Log.e("com.melchor629.musicote", e.toString());
                }

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                in.putExtra("titulo", name);
                in.putExtra("artista", cost);
                in.putExtra("album", description);
                in.putExtra("duracion", album);
                in.putExtra("archivo", "http://"+url+""+archivo);

                startActivity(in);
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.ajustesm:
                Intent intent = new Intent(MainActivity.this, Ajustes.class);
                startActivity(intent);
                break;
            case R.id.parar:
                Intent intento = new Intent(MainActivity.this, Reproductor.class);
                stopService(intento);
            default:
                return super.onOptionsItemSelected(item);
        }
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
    @Override
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

        public int response;

        @Override
		protected ArrayList<HashMap<String, String>> doInBackground(Void... params){
            // Hashmap for ListView
            final ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();

            // Creating JSON Parser instance
            ParseJSON jParser = new ParseJSON();

            synchronized (this){
                publishProgress(1);
                // La app prueba en busca de la dirección correcta
                if(jParser.HostTest("192.168.1.133",80)){
                    MainActivity.url = "192.168.1.133";publishProgress(5);
                }else if(jParser.HostTest("reinoslokos.no-ip.org",80)){
                    MainActivity.url = "reinoslokos.no-ip.org";
                }
                publishProgress(10);
                if(MainActivity.url!=null){
                    // getting JSON string from URL
                    try{
                        URL urlhttp = new URL("http://"+MainActivity.url+"/cgi-bin/archivos.py");
                        publishProgress(13);
                        HttpURLConnection http = (HttpURLConnection) urlhttp.openConnection();
                        publishProgress(17);
                        response = http.getResponseCode();
                        publishProgress(21);
                    } catch(Exception e){
                        Log.e("Comprobando", "Excepción HTTPURL: "+e.toString());
                    }
                    publishProgress(25);
                    if(response==200){
                        JSONObject json = jParser.getJSONFromUrl("http://"+MainActivity.url+"/cgi-bin/archivos.py");
                        publishProgress(30);
                        try {
                            // Getting Array of Songs
                            MainActivity.contacts = json.getJSONArray("canciones");
                            publishProgress(35);

                            // looping through All Songs
                            publishProgress(37);
                            for(int i = 0; i < MainActivity.contacts.length(); i++){
                                JSONObject c = MainActivity.contacts.getJSONObject(i);

                                int counter = 40 + ((i*100/MainActivity.contacts.length())/2);
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
                                map.put("archivo", "http://"+MainActivity.url+""+archivo);
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

                    publishProgress(95);
                    return contactList;
                }else{
                    return null;
                }
            }
        }

        @Override
		protected void onProgressUpdate(Integer... progress){
    		progressDialog.setProgress(progress[0]);
        }

        @Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result){
            super.onPostExecute(result);
            try{
                //Thread.sleep(1000);
                publishProgress(99);
            } catch (Exception e) {}
        }
    }

}
