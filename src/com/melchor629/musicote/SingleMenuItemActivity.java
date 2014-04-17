package com.melchor629.musicote;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.IconButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

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
 * @author Melchor
 */
public class SingleMenuItemActivity extends SherlockActivity {

    // JSON node keys
    private static final String TAG_NAME = "titulo";
    private static final String TAG_EMAIL = "artista";
    private static final String TAG_PHONE_MOBILE = "album";
    private static final String TAG_DURACIONS = "duracion";
    private static final String TAG_FILE = "archivo";

    private volatile int progress;
    private boolean isDownloaded = false;

    private String title;
    private String artist;
    private String album;
    private String duracion;
    private String archivo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_list_item);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // getting intent data
        Intent in = getIntent();
        title = in.getStringExtra(TAG_NAME);
        artist = in.getStringExtra(TAG_EMAIL);
        album = in.getStringExtra(TAG_PHONE_MOBILE);
        duracion = in.getStringExtra(TAG_DURACIONS);
        archivo = in.getStringExtra(TAG_FILE);
        isDownloaded = in.getBooleanExtra("downloaded", false);
        //If the Activity started from a crash, close the activity, avoiding another crash, and open the Main Activity
        if(title == null || artist == null || album == null || duracion == null || archivo == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        //Setting the activity title
        ab.setTitle(title);
        getWindow().getDecorView().setBackgroundColor(android.graphics.Color.rgb(50, 207, 102));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Displaying all values on the screen
        TextView lblName = (TextView)findViewById(R.id.name_label);
        TextView lblCost = (TextView)findViewById(R.id.email_label);
        TextView lblDesc = (TextView)findViewById(R.id.mobile_label);
        TextView lblDura = (TextView)findViewById(R.id.duracionS);
        //TextView lblArch = (TextView) findViewById(R.id.Play); Guardado por si acaso

        lblName.setText(title);
        lblCost.setText(artist);
        lblDesc.setText(album);
        lblDura.setText(duracion);
        //TODO Poner una carátula de álbum

        if(isDownloaded)
            ((IconButton) findViewById(R.id.stopActual)).setText("{fa-trash-o}");

        findViewById(R.id.stopActual).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isDownloaded)
                    Toast.makeText(getApplicationContext(), getString(R.string.download_info), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.delete_info), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        PlaylistManager.Song song = PlaylistManager.self.get(PlaylistManager.pos);
        if(song != null && title.equals(song.title) && artist.equals(song.artist))
            o();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.ajustesm).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_cogs)
                        .color(Color.WHITE)
                        .actionBarSize());
        menu.findItem(R.id.parar).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_music)
                        .color(Color.WHITE)
                        .actionBarSize()
        );
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
     * @param v view from android
     */
    public void PlaySong(final View v) {
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
        IconButton but = (IconButton) v.findViewById(R.id.play);
        if(but.getTag().toString().equals("play")) {
            but.startAnimation(animAlpha);
            but.setText("{fa-pause}");
            but.startAnimation(alphaAnim);
            but.setTag("pause");

            if(Reproductor.a != -1)
                PlaylistManager.self.stopPlaying();
            PlaylistManager.self.startPlaying(title, artist, album, archivo);
        } else if(but.getTag().toString().equals("pause")) {
            but.setTag("playpause");
            but.startAnimation(animAlpha);
            but.setText("{fa-play}");
            but.startAnimation(alphaAnim);
            Reproductor.pause();
        } else if(but.getTag().toString().equals("playpause")) {
            but.startAnimation(animAlpha);
            but.setText("{fa-pause}");
            but.startAnimation(alphaAnim);
            but.setTag("pause");
            Reproductor.pause();
        }
    }

    /**
     * StopSong
     * Para el servicio del reproductor
     *
     * @param v view from android
     */
    public void StopSong(View v) {
        IconButton but = (IconButton) v;
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
        PlaylistManager.self.stopPlaying();
        but.startAnimation(animAlpha);
        but.setText("{fa-play}");
        but.startAnimation(alphaAnim);
        but.setTag("play");
        Reproductor.pause();
    }

    /**
     * addToPlaylist
     * Añade una canción a la lista de reproducción
     *
     * @param v view from android
     */
    public void addToPlaylist(View v) {
        String url = archivo;
        if(isDownloaded)
            url = "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() +
                "/" + archivo.substring(archivo.lastIndexOf("/")+1);
        PlaylistManager.self.addSong(title, artist, album, url);
        Toast.makeText(this, title + " " + this.getResources().getString(R.string.added_to_playlist), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * TODO Pasar esto a un servicio a parte para poder descargar cosas everywhere
     * Descarga la canción seleccionada
     *
     * @param v view from android
     */
    public void download(View v) {
        if(!isDownloaded) { //Only if it isn't downloaded
            final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplication());
            mBuilder.setContentTitle("Descargando " + title + " de " + artist)
                    .setContentText("Descargando musicote...")
                    .setSmallIcon(R.drawable.altavoz)
                    .setOngoing(true)
                    .setProgress(100, 0, false);
            mNotifyManager.cancel(2);
            mNotifyManager.notify(2, mBuilder.build());
            final IconButton m = ((IconButton) findViewById(R.id.stopActual));
    
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Log.d("SingleMenuItem", "Descargando " + archivo + "...");
                        if(Environment.MEDIA_MOUNTED.equals("mounted")) {
                            //Create music directory if doesn't exist
                            if(!Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).isDirectory())
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
                                new Handler().post(new Runnable() {@Override public void run() {m.setText("{fa-trash-o}");}});
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
                            ((IconButton) findViewById(R.id.stopActual)).setText("{fa-download}");
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

    private void o() {
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.from_alpha);
        IconButton but = (IconButton)findViewById(R.id.play);
        but.setTag("pause");
        but.startAnimation(animAlpha);
        but.setText("{fa-pause}");
        but.startAnimation(alphaAnim);
    }
}
