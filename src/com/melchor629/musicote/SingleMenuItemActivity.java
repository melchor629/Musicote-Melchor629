package com.melchor629.musicote;

import com.melchor629.musicote.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
        android.view.animation.Interpolator on = new android.view.animation.DecelerateInterpolator();
        
        texto = (TextView) findViewById(R.id.playingNow);
        barra = (ProgressBar) findViewById(R.id.progressBar1);
        barra.setMax(100);
        barra.setInterpolator(on);
        barra.setSecondaryProgress(50);
        
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
    	final Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
    	
    	if(Reproductor.a != -1)
    		H = true;

        new Thread(
    		new Runnable() {
    			public void run() {
    				while(H) {
    					h.post(
							new Runnable() {
								public void run() {
									if(Reproductor.a != -1) {
										if(n || (Reproductor.a != -1 && !n))
											o();
										barra.setProgress((int)Reproductor.a);
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
										barra.setProgress((int)Reproductor.a);
										texto.setText("No reproduce nada");
									}
								}
							}
						);
    					try {
							Thread.sleep(1000);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
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
    	Uri location = Uri.parse(archivo);
    	Intent dow = new Intent(Intent.ACTION_VIEW, location);
    	startActivity(dow);
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
}
