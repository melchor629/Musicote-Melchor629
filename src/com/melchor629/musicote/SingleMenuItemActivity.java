package com.melchor629.musicote;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.special.ResideMenu.menu.ResideMenu;
import com.special.ResideMenu.menu.ResideMenuItem;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
 *
 * @author Melchor
 */
public class SingleMenuItemActivity extends SherlockActivity implements OnClickListener {

    // JSON node keys
    private static final String TAG_NAME = "titulo";
    private static final String TAG_EMAIL = "artista";
    private static final String TAG_PHONE_MOBILE = "album";
    private static final String TAG_DURACIONS = "duracion";
    private static final String TAG_FILE = "archivo";

    private TextView texto;
    private ProgressBar barra;
    public volatile int progress;
    private Handler h = new Handler();
    private boolean H = true;
    private boolean n = false;
    private boolean e = true;
    private boolean isDownloaded = false;
    private ResideMenu resideMenu;

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
        ab.setDisplayHomeAsUpEnabled(true);

        //ResideMenu
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.blurred_background);
        resideMenu.attachToActivity(this);
        String titles[] = { "Home", "Settings", "Playlist", "About" };
        int icon[] = { R.drawable.ic_home, R.drawable.cogwheels, R.drawable.playlist, R.drawable.ic_launcher };

        for (int i = 0; i < titles.length; i++){
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            item.setOnClickListener(this);
            item.setTag(i);
            resideMenu.addMenuItem(item);
        }

        android.view.animation.Interpolator on = new android.view.animation.DecelerateInterpolator();

        texto = (TextView)findViewById(R.id.playingNow);
        barra = (ProgressBar)findViewById(R.id.progressBar1);
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
                            if(e) {
                                h.post(
                                    new Runnable() {
                                        public void run() {
                                            if(Reproductor.a != -1) {
                                                if(n || (Reproductor.a != -1 && n))
                                                    o();
                                                barra.setProgress((int)(Reproductor.a * 10d));
                                                texto.setText(getResources().getString(R.string.playing) + " " + Reproductor.tit + " " + getResources().getString(R.string.playing_of) + " " + Reproductor.art);
                                            } else {
                                                if(!n) {
                                                    Drawable play = getResources().getDrawable(R.drawable.ic_stat_name);
                                                    ImageButton but = (ImageButton)findViewById(R.id.play);
                                                    but.setTag("play");
                                                    but.startAnimation(animAlpha);
                                                    but.setImageDrawable(play);
                                                    but.startAnimation(alphaAnim);
                                                    n = true;
                                                }
                                                barra.setProgress((int)(Reproductor.a * 10d));
                                                texto.setText(getResources().getString(R.string.playing_no));
                                            }
                                        }
                                    }
                                );
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) { }
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
        //If the Activity started from a crash, close the activity, avoiding another crash, and open the Main Activity
        if(name == null || cost == null || description == null || duracion == null || archivo == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        //Setting the activity title
        ab.setTitle(name);

        // Displaying all values on the screen
        TextView lblName = (TextView)findViewById(R.id.name_label);
        TextView lblCost = (TextView)findViewById(R.id.email_label);
        TextView lblDesc = (TextView)findViewById(R.id.mobile_label);
        TextView lblDura = (TextView)findViewById(R.id.duracionS);
        //TextView lblArch = (TextView) findViewById(R.id.Play); Guardado por si acaso

        lblName.setText(name);
        lblCost.setText(cost);
        lblDesc.setText(description);
        lblDura.setText(duracion);
        //TODO Poner una carátula de álbum

        //Comoprueba si está descargada la canción
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() + "/" + archivo.substring(archivo.lastIndexOf("/")+1));
        isDownloaded = file.exists();
        if(isDownloaded)
            ((ImageButton) findViewById(R.id.stopActual)).setImageResource(R.drawable.delete);
        ((ImageButton) findViewById(R.id.stopActual)).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isDownloaded)
                    Toast.makeText(getApplicationContext(), getString(R.string.download_info), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.delete_info), Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case R.id.ajustesm:
                Intent intent = new Intent(SingleMenuItemActivity.this, Ajustes.class);
                startActivity(intent);
                break;
            case R.id.parar:
                Intent intento = new Intent(SingleMenuItemActivity.this, ReproductorGrafico.class);
                intento.putExtra("button", true);
                startActivity(intento);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * PlaySong
     * Al apretar el enlace para reproducir canción aparece un servicio dificil de manejar
     * @param v
     */
    public void PlaySong(final View v) {
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
        Drawable pause = getResources().getDrawable(R.drawable.ic_pause);
        Drawable play = getResources().getDrawable(R.drawable.ic_stat_name);
        ImageButton but = (ImageButton)v.findViewById(R.id.play);
        if(but.getTag().toString().equals("play")) {
            but.startAnimation(animAlpha);
            but.setImageDrawable(pause);
            but.startAnimation(alphaAnim);
            but.setTag("pause");

            url = archivo;
            String archivo = SingleMenuItemActivity.archivo.substring(SingleMenuItemActivity.archivo.lastIndexOf("/")+1);

            if(isDownloaded)
                url = "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() + "/" + archivo;

            // Starting new intent
            Intent in = new Intent(getApplicationContext(), Reproductor.class);
            in.putExtra("titulo", name);
            in.putExtra("artista", cost);
            in.putExtra("archivo", url);
            in.putExtra("album", description);

            startService(in);

            Reproductor.a = 0;
            n = false;
        } else if(but.getTag().toString().equals("pause")) {
            but.setTag("playpause");
            but.startAnimation(animAlpha);
            but.setImageDrawable(play);
            but.startAnimation(alphaAnim);
            Reproductor.pause();
        } else if(but.getTag().toString().equals("playpause")) {
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
     *
     * @param v
     */
    public void StopSong(View v) {
        Intent in = new Intent(getApplicationContext(), Reproductor.class);
        stopService(in);
    }

    /**
     * addToPlaylist
     * Añade una canción a la lista de reproducción
     *
     * @param v
     */
    public void addToPlaylist(View v) {
        String url = archivo;
        if(isDownloaded)
            url = "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() +
                "/" + archivo.substring(archivo.lastIndexOf("/")+1);
        Reproductor.addSong(name, cost, url, description);
        Toast.makeText(this, name + " " + this.getResources().getString(R.string.added_to_playlist), Toast.LENGTH_LONG).show();
    }

    /**
     * download
     * Descarga la canción seleccionada
     *
     * @param v
     */
    public void download(View v) {
        if(!isDownloaded) { //Only if it isn't downloaded
            final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplication());
            mBuilder.setContentTitle("Descargando " + name + " de " + cost)
                    .setContentText("Descargando musicote...")
                    .setSmallIcon(R.drawable.download)
                    .setOngoing(true)
                    .setProgress(100, 0, false);
            mNotifyManager.cancel(2);
            mNotifyManager.notify(2, mBuilder.build());
            final ImageButton m = ((ImageButton) findViewById(R.id.stopActual));
    
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Log.d("SingleMenuItem", "Descargando " + archivo + "...");
                        if(Environment.MEDIA_MOUNTED.equals("mounted")) {
                            //Create music directory if doesn't exist
                            if(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).isDirectory())
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).mkdirs();
                            try {
                                URL url = new URL(archivo.replace(" ", "%20"));
                                String arch = archivo.substring(archivo.lastIndexOf("/") + 1);
                                URLConnection connection = url.openConnection();
                                connection.connect();
                                // this will be useful so that you can show a typical 0-100% progress bar
                                int fileLength = connection.getContentLength();
    
                                // download the file
                                InputStream input = new BufferedInputStream(url.openStream());
                                File songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), arch);
                                OutputStream output = new FileOutputStream(songFile);
    
                                byte data[] = new byte[1024];
                                long total = 0;
                                int count;
                                while((count = input.read(data)) != -1) {
                                    total += count;
                                    progress = (int)(total * 100 / fileLength);
                                    output.write(data, 0, count);
                                }
    
                                output.flush();
                                output.close();
                                input.close();

                                isDownloaded = true;
                                new Handler().post(new Runnable() {@Override public void run() {m.setImageResource(R.drawable.delete);}});
                                Toast.makeText(getApplicationContext(), "Descarga lista", Toast.LENGTH_LONG).show();
                                Log.d("SingleMenuItem", "Arhivo " + archivo + " descargado completamente");
                            } catch (MalformedURLException e) {
                                progress = -1;
                                Log.e("SingleMenuItem", "Error: " + e.toString());
                            } catch (FileNotFoundException e) {
                                progress = -1;
                                Log.e("SingleMenuItem", "Error: " + e.toString());
                            } catch (IOException e) {
                                progress = -1;
                                Log.e("SingleMenuItem", "Error: " + e.toString());
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "No hay SD montada", Toast.LENGTH_LONG).show();
                        }
                        try {
                            this.finalize();
                        } catch (Throwable e) {
                            Log.e("SingleMenuItem", "Error: " + e.toString());
                        }
                    }
                }
            ).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    while(progress < 100 && progress != -1) {
                        // publishing the progress...
                        mBuilder.setProgress(100, progress, false);
                        mNotifyManager.notify(2, mBuilder.build());
    
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e("SingleMenuItem", "Error: " + e.toString());
                        }
                    }

                    if(progress != -1) {
                        mBuilder.setContentText("Descarga completa")
                                // Removes the progress bar
                                .setProgress(0, 0, false)
                                //Removes fixed notify, ensure to be deleted by user
                                .setOngoing(false);
                        mNotifyManager.notify(2, mBuilder.build());
                    } else if(progress == -1){
                        mBuilder.setContentText("Error al descargar")
                                // Removes the progress bar
                                .setProgress(0, 0, false)
                                //Removes fixed notify, ensure to be deleted by user
                                .setOngoing(false);
                        mNotifyManager.notify(2, mBuilder.build());
                    }
                }
            }).start();
        } else {
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() + "/" + archivo.replace("http://" + MainActivity.url + "/musica/", ""));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.sure_delete)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(file.delete()) {
                            ((ImageButton) findViewById(R.id.stopActual)).setImageResource(R.drawable.download);
                            Toast.makeText(getApplicationContext(), getString(R.string.done_delete), Toast.LENGTH_LONG).show();
                            isDownloaded = false;
                        } else
                            Toast.makeText(getApplicationContext(), getString(R.string.err_delete), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        H = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        e = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        e = true;
    }

    private void o() {
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
        Drawable pause = getResources().getDrawable(R.drawable.ic_pause);
        ImageButton but = (ImageButton)findViewById(R.id.play);
        but.setTag("pause");
        but.startAnimation(animAlpha);
        but.setImageDrawable(pause);
        but.startAnimation(alphaAnim);
        n = false;
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        ResideMenuItem item = (ResideMenuItem) view;
        int which = (Integer) item.getTag();
        switch(which) {
            case 0:
                finish();
            case 1:
                Intent intent = new Intent(this, Ajustes.class);
                startActivity(intent);
                resideMenu.closeMenu();
                break;
            case 2:
                Intent intento = new Intent(this, ReproductorGrafico.class);
                intento.putExtra("button", true);
                startActivity(intento);
                resideMenu.closeMenu();
                break;
            case 3:
                resideMenu.closeMenu();
                break;
        }
    }
}
