package com.melchor629.musicote;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.melchor629.musicote.R;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
public class SingleMenuItemActivity extends SherlockActivity {

    // JSON node keys
    private static final String TAG_NAME = "titulo";
    private static final String TAG_EMAIL = "artista";
    private static final String TAG_PHONE_MOBILE = "album";
    private static final String TAG_DURACIONS = "duracion";
    private static final String TAG_FILE = "archivo";
    
    private TextView texto;
    private ProgressBar barra;
    private Handler h = new Handler();
    private boolean H = true;
    private boolean n = false;

    public static String url;
    public static String name;
    public static String cost;
    public static String description;
    public static String duracion;

    private static String archivo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_list_item);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        ActionBar ab = getSupportActionBar();
        //ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        
        android.view.animation.Interpolator on = new android.view.animation.DecelerateInterpolator();
        
        texto = (TextView) findViewById(R.id.playingNow);
        barra = (ProgressBar) findViewById(R.id.progressBar1);
        barra.setMax(1000);
        barra.setInterpolator(on);
        barra.setSecondaryProgress(500);
        
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
    	final Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
    	
    	if(Reproductor.a != -1)
    		n = true;

        new Thread(
    		new Runnable() {
    			public void run() {
    				while(H) {
    					h.post(
							new Runnable() {
								public void run() {
									if(Reproductor.a != -1) {
										if(n || (Reproductor.a != -1 && n))
											o();
										barra.setProgress((int)(Reproductor.a*10d));
										texto.setText("Reproduciendo "+Reproductor.tit+" de "+Reproductor.art);
									} else {
										if(!n) {
									    	Drawable play = getResources().getDrawable(R.drawable.ic_stat_name);
									    	ImageButton but = (ImageButton) findViewById(R.id.play);
											but.setTag("play");
								        	but.startAnimation(animAlpha);
								    		but.setImageDrawable(play);
								    		but.startAnimation(alphaAnim);
								    		n = true;
										}
										barra.setProgress((int)(Reproductor.a*10d));
										texto.setText("No reproduce nada");
									}
								}
							}
						);
    					try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
    				}
    			}
    		}
		).start();

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        name = in.getStringExtra(TAG_NAME);
        cost = in.getStringExtra(TAG_EMAIL);
        description = in.getStringExtra(TAG_PHONE_MOBILE);
        duracion = in.getStringExtra(TAG_DURACIONS);
        archivo = in.getStringExtra(TAG_FILE);
        
        //Setting the activity title
        ab.setTitle(name);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
        	case 16908332:
                Intent intenta = new Intent(this, MainActivity.class);
                intenta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intenta);
                break;
            case R.id.ajustesm:
                Intent intent = new Intent(SingleMenuItemActivity.this, Ajustes.class);
                startActivity(intent);
                break;
             case R.id.parar:
                Intent intento = new Intent(SingleMenuItemActivity.this, Reproductor.class); //TODO Cambiar esto por la actividad del reproductor UI
                stopService(intento);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * PlaySong
     * Al apretar el enlace para reproducir canción aparece un servicio dificil de manejar
     * TODO Añadir una interfaz al servicio...
     * @param v
     */
    public void PlaySong(final View v) {
    	Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
    	Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
    	Drawable pause = getResources().getDrawable(R.drawable.ic_pause);
    	Drawable play = getResources().getDrawable(R.drawable.ic_stat_name);
    	ImageButton but = (ImageButton)v.findViewById(R.id.play); Log.d("", but.getTag().toString());
    	if(but.getTag().toString().equals("play")) {
			but.startAnimation(animAlpha);
			but.setImageDrawable(pause);
			but.startAnimation(alphaAnim);
			but.setTag("pause");
			
	        url = archivo;
	        // Starting new intent
	        Intent in = new Intent(getApplicationContext(), Reproductor.class);
	        in.putExtra("titulo", name);
	        in.putExtra("artista", cost);
	        in.putExtra("archivo", url);
	        Log.i("Iniciando servicio...", "1. "+name+" 2. "+cost+" 3."+url);
	
	        startService(in);
	        
	        Reproductor.a = 0;
	        n = false;
    	}else if(but.getTag().toString().equals("pause")) {
        	but.setTag("playpause");
        	but.startAnimation(animAlpha);
    		but.setImageDrawable(play);
    		but.startAnimation(alphaAnim);
    		Reproductor.pause();
    	}else if(but.getTag().toString().equals("playpause")) {
			but.startAnimation(animAlpha);
			but.setImageDrawable(pause);
			but.startAnimation(alphaAnim);
			but.setTag("pause");
			Reproductor.pause();
    	}
    }

    /**
     * StopSong
     * Para el servicio del reproductor
     * @param v
     */
    public void StopSong(View v) {
        Intent in = new Intent(getApplicationContext(), Reproductor.class);
        stopService(in);
    }
    
    /**
     * download	
     * Descarga la canción seleccionada
     * @param v
     */
    public void download(View v){
    	try {
    		new DownloadFile().doInBackground(archivo);
    		//TODO poner un Thread + Handler para actualizar la descarga perfectamente y que no vaya atascada
    	} catch(java.lang.RuntimeException o) {
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	H = false;
    }
    
    private void o() {
    	Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
    	Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
    	Drawable pause = getResources().getDrawable(R.drawable.ic_pause);
    	ImageButton but = (ImageButton) findViewById(R.id.play);
    	but.setTag("pause");
    	but.startAnimation(animAlpha);
		but.setImageDrawable(pause);
		but.startAnimation(alphaAnim);
		n = false;
    }
    
    private class DownloadFile extends AsyncTask<String, Integer, Void> {
		@Override
        protected Void doInBackground(String... urls) {
            if(Environment.MEDIA_MOUNTED.equals("mounted")) {
	            // params comes from the execute() call: params[0] is the url
            	try {
					URL url = new URL(urls[0].replace(" ", "%20"));
					String arch = urls[0].substring(urls[0].lastIndexOf("/")+1);
					URLConnection connection = url.openConnection();
					connection.connect();
					// this will be useful so that you can show a typical 0-100% progress bar
					int fileLength = connection.getContentLength();

					// download the file
					InputStream input = new BufferedInputStream(url.openStream());
					OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()+"/"+arch);

					byte data[] = new byte[1024];
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
					    total += count;
					    // publishing the progress....
					    publishProgress((int) (total * 100 / fileLength));
					    output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
				} catch (MalformedURLException e) {
					Log.e("DM1","Error: "+ e.toString());
				} catch (FileNotFoundException e) {
					Log.e("DM2","Error: "+ e.toString());
				} catch (IOException e) {
					Log.e("DM3","Error: "+ e.toString());
				}
            }else {
            	Toast.makeText(getApplicationContext(), "No hay SD montada", Toast.LENGTH_LONG).show();
            }
			return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            return;
       }
        
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            barra.setProgress(progress[0]);
        }
    }
}
