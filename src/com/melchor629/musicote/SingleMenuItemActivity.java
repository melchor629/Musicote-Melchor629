package com.melchor629.musicote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.melchor629.musicote.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
	 * Al apretar el enlace para reproducir canción aparece el menú "intent"
	 * On click the link to play the song appears the "intent" menu
	 * TODO Cambiar el "intent" por un reproductor de la misma APP y que se pueda minimizar
	 * @param v
	 */
	public void PlaySong(View v) {
		InputStream is=null;
		boolean sierto=false;
		url = archivo;
		// Starting new intent
		Intent in = new Intent(getApplicationContext(), Reproductor.class);
		in.putExtra("titulo", name);
		in.putExtra("artista", cost);
		in.putExtra("archivo", url);

		startService(in);
		/*try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(archivo);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
		} catch(Exception e){
			Log.e("cache","Error al descargar el archivo: "+e.toString());
		}
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			Toast.makeText(getApplicationContext(), "La tarjeta externa no tiene permisos de escritura", Toast.LENGTH_SHORT).show();
			sierto = false;
		} else {
			Toast.makeText(getApplicationContext(), "No se ha encontrado una Tarjeta Externa", Toast.LENGTH_SHORT).show();
		}
		File cachedir = new File(Environment.getExternalStorageDirectory()+"/.musicote/");
		if(!cachedir.exists()){
			if(!cachedir.mkdirs()){
				sierto = false;
				Log.e("DirectoryCreate", "Error al crear el directorio \"/sdcard/.musicote/\"...");
			}
		}
		FileOutputStream out;
		byte buf[] = new byte[16384];
		File downloadingMediaFile = null;
		try{
			downloadingMediaFile = new File(cachedir, "musicote-temp.mp3");
			out = new FileOutputStream(downloadingMediaFile);

			do {
				int numread = is.read(buf);
				if (numread <= 0) {
					// Nothing left to read so quit
					break;
				} else {
					out.write(buf, 0, numread);
					sierto = true;
				}
			} while (true);
			is.close();
		} catch(Exception e){
			Log.e("FileIO","Error: "+e.toString());
		}
		if(sierto==true){
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW); 
			intent.setDataAndType(Uri.fromFile(downloadingMediaFile), "audio/*");
			startActivity(intent);
		}else{
			Toast.makeText(getApplicationContext(), "Usando el método malo, abriendo una URL...", Toast.LENGTH_SHORT).show();
			Log.e("PlaySong","Error al meter el archivo en caché...");
			Uri webpage = Uri.parse(archivo);
	    	Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
	    	startActivity(webIntent);
		}*/
	}
}