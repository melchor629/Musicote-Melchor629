package com.melchor629.musicote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.melchor629.musicote.scrobbler.Album;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.michaelevans.colorart.library.ColorArt;

/**
 * El reproductor, en modo gráfico para que pueda el usuario controlarlo mejor
 * @author melchor9000
 */
public class ReproductorGrafico extends SherlockActivity implements Runnable, SeekBar.OnSeekBarChangeListener {

    //Importing all the layout stuff into Java code for use it easely
    private TextView tituloActual;
    private TextView artistaActual;
    private TextView albumActual;
    private TextView positionActual;
    private TextView durationActual;
    private ImageView image;
    private SeekBar playingUbication;
    private IconButton playpauseActual;

    private volatile boolean H;
    private Handler h;
    private volatile String song, album;
    private volatile boolean isSeeking = false;
    private volatile boolean doThings = false;
    private boolean button;
    private final String TAG = "Reproductor Gráfico";

    @SuppressLint ("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reproductor_grafico);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        button = getIntent().getBooleanExtra("button", false);

        //Starting layout variables
        tituloActual = (TextView) findViewById(R.id.tituloActual);
        artistaActual = (TextView) findViewById(R.id.artistaActual);
        albumActual = (TextView) findViewById(R.id.albumActual);
        playingUbication = (SeekBar) findViewById(R.id.playingUbication);
        playpauseActual = (IconButton) findViewById(R.id.playpauseActual);
        positionActual = (TextView) findViewById(R.id.currentPlayingPosition);
        durationActual = (TextView) findViewById(R.id.currentPlayingDuration);
        image = (ImageView) findViewById(R.id.AlbumGP);

        playingUbication.setOnSeekBarChangeListener(this);

        if(Reproductor.a != -1) {
            setThings();
            playpauseActual.setText("{fa-pause}");
            playpauseActual.setTag("pause");
        }
        setBackground(getResources().getDrawable(R.drawable.graphical_player));

        H = true;
        h = new Handler();
        new Thread(this, "Player GUI").start();
    }

    private void setThings() {
        if(PlaylistManager.self.get(0) != null) {
            tituloActual.setText(PlaylistManager.self.get(0).title);
            artistaActual.setText(PlaylistManager.self.get(0).artist);
            albumActual.setText(PlaylistManager.self.get(0).album);
        }
        positionActual.setText("0:00");
        durationActual.setText("0:00");

        if(image.getMeasuredWidth() < image.getMeasuredHeight())
            image.setLayoutParams(new android.widget.LinearLayout.LayoutParams(image.getMeasuredWidth(), image.getMeasuredWidth()));
        if(image.getMeasuredWidth() > image.getMeasuredHeight())
            image.setLayoutParams(new android.widget.LinearLayout.LayoutParams(image.getMeasuredHeight(), image.getMeasuredHeight()));
    }

    public void playpause(View v) {
        if(Reproductor.a != -1) {
            if(!Reproductor.paused) {
                playpauseActual.setText("{fa-play}");
                playpauseActual.setTag("play");
            } else {
                playpauseActual.setText("{fa-pause}");
                playpauseActual.setTag("pause");
            }

            Reproductor.pause();
        }
    }

    public void stop(View v) {
        if(Reproductor.a != -1) {
            PlaylistManager.self.stopPlaying();
            playpauseActual.setText("{fa-play}");
            playpauseActual.setTag("play");
        }
    }

    public void next(View v) {
        if(PlaylistManager.self.isNextSong()) {
            Reproductor.reproductor.seekTo(Reproductor.reproductor.getDuration() - 1);
        } else {
            Toast.makeText(this, "No hay siguiente canción", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint ("NewApi")
    @SuppressWarnings ("deprecation")
    private void setBackground(Drawable d) {
        if(d == null)
            getWindow().getDecorView().setBackgroundResource(R.drawable.graphical_player);
        else {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                getWindow().getDecorView().setBackground(d);
            else
                getWindow().getDecorView().setBackgroundDrawable(d);
        }
    }

    private void animateAlbumArt(final Bitmap bmp, final boolean type) {
        int id = type ? R.anim.fade_in : R.anim.fade_out;
        final int time = 300;
        Animation a = AnimationUtils.loadAnimation(getApplicationContext(), id);
        a.setDuration(time);
        image.startAnimation(a);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(type ? 1 : time);
                    h.post(new Runnable() {
                        public void run() {
                            if(image != null)
                                image.setImageBitmap(bmp);
                            }
                        });
                } catch(InterruptedException e) {
                    Log.e(TAG, "", e);
                }
            }
        }).start();
    }

    @Override
    public void run() {
        Looper.prepare();
        song = "";
        while(H) {
            h.post(
                new Runnable() {
                    @Override
                    public void run() {
                        playingUbication.setMax(1000);
                        if(!isSeeking)
                            playingUbication.setProgress((int) (Reproductor.a * 10d));
                        if(doThings)
                            setThings();
                        //Time stuff
                        if(PlaylistManager.self.get(0) != null && Reproductor.a != -1) {
                            int duration = Reproductor.reproductor.getDuration() / 1000;
                            int position = Reproductor.reproductor.getCurrentPosition() / 1000;
                            int minutes = position / 60;
                            float seconds = ((position / 60f) - (float) minutes) * 60f;
                            positionActual.setText(minutes + ":" + (seconds < 10 ? "0" : "") + (int) seconds);
                            minutes = duration / 60;
                            seconds = ((duration / 60f) - (float) minutes) * 60f;
                            durationActual.setText(minutes + ":" + (seconds < 10 ? "0" : "") + (int) seconds);
                        }
                    }
                }
            );

            synchronized (this) {
                if(PlaylistManager.self.get(0) != null && !song.equals(PlaylistManager.self.get(0).title)) {
                    if(PlaylistManager.self.get(0).album.isEmpty() && PlaylistManager.self.get(0).album != null)
                        new Background().execute(0);
                    song = PlaylistManager.self.get(0).title;
                    doThings = true;
                } else if(PlaylistManager.self.get(0) == null && song != null) {
                    song = null;
                    h.post(new Runnable() {
                        public void run() {
                            tituloActual.setText("");
                            artistaActual.setText("");
                            albumActual.setText("");
                            positionActual.setText("0:00");
                            durationActual.setText("0:00");
                            positionActual.setTextColor(Color.WHITE);
                            durationActual.setTextColor(Color.WHITE);
                            animateAlbumArt(null, false);
                        }
                    });
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error: " + e.toString());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        H = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_reproductor_grafico, menu);

        menu.findItem(R.id.settings).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_cogs)
                        .color(Color.WHITE)
                        .actionBarSize());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                if(button)
                    finish();
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.settings:
                Intent intent = new Intent(this, Ajustes.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
     */
    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
        if(fromUser && Reproductor.a != -1) {
            float posicion = (progress / 1000f) * (float)Reproductor.reproductor.getDuration();
            if((progress / 1000f) >= 0.98f)
                Reproductor.reproductor.seekTo(Math.round(posicion) - 100);
            else
                Reproductor.reproductor.seekTo(Math.round(posicion));
            Reproductor.reproductor.pause();
        }
    }

    /* (non-Javadoc)
     * @see android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android.widget.SeekBar)
     */
    @Override
    public void onStartTrackingTouch(SeekBar bar) {
        isSeeking = true;
    }

    /* (non-Javadoc)
     * @see android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android.widget.SeekBar)
     */
    @Override
    public void onStopTrackingTouch(SeekBar bar) {
        if(!Reproductor.paused)
            Reproductor.reproductor.start();
        isSeeking = false;
    }

    private class Background extends AsyncTask<Integer, Void, Void> {
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Void doInBackground(Integer... params) {
            if(album != null && album.equals(PlaylistManager.self.get(0).album))
                return null;

            album = PlaylistManager.self.get(0).album;Log.d(TAG, "Album diferentes...");
            final Object[] d = new Object[2];
            if(((ImageView) findViewById(R.id.AlbumGP)).getDrawable() != null)
                h.post(new Runnable() {
                   public void run() {
                       animateAlbumArt(null, false);
                   }
                });

            if(PlaylistManager.self.get(0).album != null && PlaylistManager.self.get(0).album.length() != 0) {
                Album album = new Album(PlaylistManager.self.get(0).artist, PlaylistManager.self.get(0).album);
                String albumart = album.getInfo();
                InputStream is;
                try {
                    //Download the image content
                    is = (InputStream) new URL(albumart).getContent();
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 1;
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opt);
                    ColorArt art = new ColorArt(bmp);
                    d[0] = bmp;
                    d[1] = art;
                } catch(IOException e) {
                    Log.wtf(TAG, "Error al descargar/procesar el álbum.", e);
                }
            }

            h.post(new Runnable() {
                @Override
                public void run() {
                    animateAlbumArt((Bitmap) d[0], true);
                    //((ImageView) findViewById(R.id.AlbumGP)).setImageBitmap((Bitmap) d[0]);
                    if(d[1] != null) {
                        ColorArt colors = (ColorArt) d[1];
                        tituloActual.setTextColor(colors.getPrimaryColor());
                        artistaActual.setTextColor(colors.getSecondaryColor());
                        albumActual.setTextColor(colors.getDetailColor());
                        positionActual.setTextColor(colors.getDetailColor());
                        durationActual.setTextColor(colors.getDetailColor());
                        if(findViewById(R.id.simplifiedTitle) != null)
                            ((TextView) findViewById(R.id.simplifiedTitle)).setTextColor(colors.getSecondaryColor());
                        if(findViewById(R.id.simplifiedArtist) != null)
                            ((TextView) findViewById(R.id.simplifiedArtist)).setTextColor(colors.getDetailColor());
                    }
                }
            });
            return null;
        }

    }
}