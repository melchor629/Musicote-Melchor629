package com.melchor629.musicote.actualizador;

import com.melchor629.musicote.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Descarga el apk para instalarlo después
 * @author melchor
 *
 */
public class Descarga extends Activity {

	@Override
	public void onCreate(Bundle sis){
		Uri webpage = Uri.parse("http://reinoslokos.no-ip.org/com.melchor629.musicote.apk");
    	Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
    	startActivity(webIntent);
    	setContentView(R.layout.descarga);
	}
}
