package com.melchor629.musicote;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.melchor629.musicote.scrobbler.Album;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

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
    private volatile String album;
    private volatile boolean isSeeking = false;
    private volatile boolean doThings = false;
    private boolean button;
    private volatile int backColor = Color.BLACK;
    private final String TAG = "Reproductor Gráfico";

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
        getWindow().getDecorView().setBackgroundColor(Color.rgb(39, 180, 231));

        H = true;
        h = new Handler();
        new Thread(this, "Player GUI").start();
    }

    private void setThings() {
        if(PlaylistManager.self.get(0) != null) {
            tituloActual.setText(PlaylistManager.self.get(PlaylistManager.pos).title);
            artistaActual.setText(PlaylistManager.self.get(PlaylistManager.pos).artist);
            albumActual.setText(PlaylistManager.self.get(PlaylistManager.pos).album);
            tituloActual.setSelected(true);
            artistaActual.setSelected(true);
            albumActual.setSelected(true);
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
            PlaylistManager.self.nextSong();
        } else {
            Toast.makeText(this, "No hay siguiente canción", Toast.LENGTH_LONG).show();
        }
    }

    private void animateAlbumArt(final Bitmap bmp, final boolean type) {
        final int time = 300;
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(image, "alpha", type?0:1, type?1:0)
        );
        set.setDuration(time).start();
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

    private void animateBackground(final int color) {
        new Thread(new Runnable() {
            public void run() {
                final View back = findViewById(R.id.activityReproductorGrafico);
                int duration = 300, time = 0;
                int cr = Color.red(color), cg = Color.green(color), cb = Color.blue(color);
                int br = Color.red(backColor), bg = Color.green(backColor), bb = Color.blue(backColor);
                while(duration >= time) {
                    float linear = (float) time / (float) duration;
                    final int r = br + (int) ((cr-br) * linear);
                    final int g = bg + (int) ((cg-bg) * linear);
                    final int b = bb + (int) ((cb-bb) * linear);
                    final int rgb = Color.rgb(r, g, b);
                    h.post(new Runnable() {
                        public void run() {
                            back.setBackgroundColor(rgb);
                        }
                    });
                    time += 1000/30;
                    try {Thread.sleep(1000/30);}catch(Exception e){}
                }
                backColor = color;
            }
        }, "BackgroundAnimate").start();
    }

    @Override
    public void run() {
        Looper.prepare();
        String song = "";
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
                        if(PlaylistManager.self.get(PlaylistManager.pos) != null && Reproductor.a != -1) {
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
                int p = PlaylistManager.pos;
                PlaylistManager.Song a = PlaylistManager.self.get(p);
                if(a != null && song != null && !song.equals(a.title)) {
                    String alb = PlaylistManager.self.get(p).album;
                    if(alb != null && !alb.isEmpty() && !alb.equals(album))
                        new Background().execute(p);
                    song = PlaylistManager.self.get(p).title;
                    doThings = true;
                } else if(PlaylistManager.self.get(p) == null && song != null) {
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
                            animateBackground(Color.BLACK);
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
            album = PlaylistManager.self.get(PlaylistManager.pos).album;
            final Object[] d = new Object[2];
            if(((ImageView) findViewById(R.id.AlbumGP)).getDrawable() != null)
                h.post(new Runnable() {
                   public void run() {
                       animateAlbumArt(null, false);
                       animateBackground(Color.BLACK);
                       tituloActual.setTextColor(Color.WHITE);
                       artistaActual.setTextColor(Color.WHITE);
                       albumActual.setTextColor(Color.WHITE);
                       positionActual.setTextColor(Color.WHITE);
                       durationActual.setTextColor(Color.WHITE);
                   }
                });

            Album album = new Album(PlaylistManager.self.get(PlaylistManager.pos).artist,
                    PlaylistManager.self.get(PlaylistManager.pos).album);
            int num = 4, width = getWindow().getDecorView().getWidth();
            if(width >= 600) num = 5;
            if(width < 300) num = 3;
            String albumart = album.getInfo(num);
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

            h.post(new Runnable() {
                @Override
                public void run() {
                    animateAlbumArt((Bitmap) d[0], true);
                    if(d[1] != null) {
                        ColorArt colors = (ColorArt) d[1];
                        tituloActual.setTextColor(colors.getPrimaryColor());
                        artistaActual.setTextColor(colors.getSecondaryColor());
                        albumActual.setTextColor(colors.getDetailColor());
                        positionActual.setTextColor(colors.getDetailColor());
                        durationActual.setTextColor(colors.getDetailColor());
                        //findViewById(R.id.activityReproductorGrafico).setBackgroundColor(colors.getBackgroundColor());
                        animateBackground(colors.getBackgroundColor());
                    }
                }
            });
            return null;
        }
    }
}