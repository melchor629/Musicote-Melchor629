package com.melchor629.myfirstapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
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
import android.app.ListActivity;

public class MyFirstActivity extends ListActivity {

	public final static String EXTRA_MESSAGE = "com.melchor629.myfirstapp.MESSAGE";
	public final static String Last_STRING = "asdasda";

	public static String Last_String = "";
	
	// url to make request
	private static String url = "http://192.168.1.128/multimedia/musicoteApi.php";
	 
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
 
        // Creating JSON Parser instance
        ParseJSON jParser = new ParseJSON();
 
        // getting JSON string from URL
        JSONObject json = jParser.getJSONFromUrl(url);
 
        try {
            // Getting Array of Songs
            contacts = json.getJSONArray("canciones");
 
            // looping through All Songs
            for(int i = 0; i < contacts.length(); i++){
                JSONObject c = contacts.getJSONObject(i);
 
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
 
                // adding HashList to ArrayList
                contactList.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("com.melchor629.myfirstapp","Excepción encontrada: "+e.toString());
        }
 
        /**
         * Updating parsed JSON data into ListView
         * */
        ListAdapter adapter = new SimpleAdapter(this, contactList,
                R.layout.list_item,
                new String[] { "id", "titulo", "artista" }, new int[] {
                        R.id.name, R.id.email, R.id.mobile });
 
        setListAdapter(adapter);
 
        // selecting single ListView item
        ListView lv = getListView();
 
        // Launching new screen on Selecting Single ListItem
        lv.setOnItemClickListener(new OnItemClickListener() {
 
            //@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                String cost = ((TextView) view.findViewById(R.id.email)).getText().toString();
                String description = ((TextView) view.findViewById(R.id.mobile)).getText().toString();
 
                // Starting new intent
                Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                in.putExtra("titulo", name);
                in.putExtra("artista", cost);
                in.putExtra("album", description);
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
}
