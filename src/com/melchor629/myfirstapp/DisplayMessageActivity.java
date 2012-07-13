package com.melchor629.myfirstapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

@SuppressLint("ParserError")
public class DisplayMessageActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MyFirstActivity.EXTRA_MESSAGE);
        
        // Create the text view
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        setContentView(textView);
        
        /**TextView info = new TextView(this);
        info.setTextSize(20);
        CharSequence infor = "Escribe lo que quieras, apreta el botón y aparecerá el mismo texto en grande";
		info.setText(infor);
		
        setContentView(info);**/
    }
}
