package com.melchor629.musicote;

import com.melchor629.myfirstapp.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MyFirstActivity.EXTRA_MESSAGE);
        String last_str = intent.getStringExtra(MyFirstActivity.Last_STRING);
        String mensaje = getString(R.string.infor)+"\n"+message+"\n"+last_str;

        // Create the text view
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setText(mensaje);

        setContentView(textView);
    }
}
