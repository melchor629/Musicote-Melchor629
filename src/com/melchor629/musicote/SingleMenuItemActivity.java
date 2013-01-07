package com.melchor629.musicote;

import com.melchor629.musicote.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
 * Crea la actividad de cuando seleccionas una cancion, SOLO UNA
 * @author Melchor
 */
public class SingleMenuItemActivity extends Activity {

	// JSON node keys
	private static final String TAG_NAME = "titulo";
	private static final String TAG_EMAIL = "artista";
	private static final String TAG_PHONE_MOBILE = "album";
	private static final String TAG_DURACIONS = "duracion";
	private static final String TAG_FILE = "archivo";
	
	public static String url;
	public static String name;
	public static String cost;

	private static String archivo;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_list_item);

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        name = in.getStringExtra(TAG_NAME);
        cost = in.getStringExtra(TAG_EMAIL);
        String description = in.getStringExtra(TAG_PHONE_MOBILE);
        String duracion = in.getStringExtra(TAG_DURACIONS);
        archivo = in.getStringExtra(TAG_FILE);

        // Displaying all values on the screen
        TextView lblName = (TextView) findViewById(R.id.name_label);
        TextView lblCost = (TextView) findViewById(R.id.email_label);
        TextView lblDesc = (TextView) findViewById(R.id.mobile_label);
        TextView lblDura = (TextView) findViewById(R.id.duracionS);
        //TextView lblArch = (TextView) findViewById(R.id.Play); Guardado por si acaso

        lblName.setText(name);
        lblCost.setText(cost);
        lblDesc.setText(description);
        lblDura.setText(duracion);
        //TODO Poner una carátula de álbum

        //TODO Hacer que el titulo de la actividad cambia dependiendo de la canción
    }

	/**
	 * PlaySong
	 * Al apretar el enlace para reproducir canción aparece un servicio dificil de manejar
	 * TODO Añadir una interfaz al servicio...
	 * @param v
	 */
	public void PlaySong(View v) {
		url = archivo;
		// Starting new intent
		Intent in = new Intent(getApplicationContext(), Reproductor.class);
		in.putExtra("titulo", name);
		in.putExtra("artista", cost);
		in.putExtra("archivo", url); Log.i("Iniciando servicio...", "1. "+name+" 2. "+cost+" 3."+url);

		startService(in);
	}
}