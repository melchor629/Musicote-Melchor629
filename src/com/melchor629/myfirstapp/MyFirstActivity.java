package com.melchor629.myfirstapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MyFirstActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.melchor629.myfirstapp.MESSAGE";
	public final static String Last_STRING = "asdasda";
	
	public static String Last_String = "";
	
	TextView mTextView; // Member variable for text view in the layout
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Set the user interface layout for this Activity
        // The layout file is defined in the project res/layout/main.xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Initialize member TextView so we can manipulate it later
        //mTextView = (TextView) findViewById(R.id.text_message);
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            Last_String = savedInstanceState.getString(Last_STRING);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /** Called when the user selects the Send button **/
    public void sendMessage(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.editText1);
    	String message = editText.getText().toString();
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
