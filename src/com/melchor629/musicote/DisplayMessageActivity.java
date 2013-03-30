package com.melchor629.musicote;

import com.melchor629.musicote.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String last_str = intent.getStringExtra(MainActivity.Last_STRING);
        SharedPreferences get = PreferenceManager.getDefaultSharedPreferences(this);
        String mensaje = getString(R.string.infor)+"\n"+message+"\n"+last_str+"\n"+get.getString("usuario", "null");
  
        // Create the text view
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setText(mensaje);
  
        setContentView(textView);
    }
}
