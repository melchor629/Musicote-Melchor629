package com.melchor629.musicote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.melchor629.musicote.PlaylistManager.Song;
import com.melchor629.musicote.scrobbler.Album;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.michaelevans.colorart.library.ColorArt;

/**
 * El reproductor, en modo gráfico para que pueda el usuario controlarlo mejor
 * @author melchor9000
 */
public class ReproductorGrafico extends SherlockListActivity implements Runnable, SeekBar.OnSeekBarChangeListener {

    //Importing all the layout stuff into Java code for use it easely
    private TextView tituloActual;
    private TextView artistaActual;
    private TextView albumActual;
    private TextView positionActual;
    private TextView durationActual;
    private SeekBar playingUbication;
    private ImageButton playpauseActual;
    private ListView playlist;
    private ActionBar ab;

    private volatile boolean H;
    private Handler h;
    private volatile String song;
    private volatile boolean isSeeking = false;
    private volatile int width, height;
    private volatile boolean doThings = false;
    private boolean button;
    private final String TAG = "Reproductor Gráfico";
    private Bitmap fondo, background;

    @SuppressLint ("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            //If possible Hardware accelerated
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        setContentView(R.layout.activity_reproductor_grafico);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        button = getIntent().getBooleanExtra("button", false);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 1;
        fondo = BitmapFactory.decodeResource(getResources(), R.drawable.graphical_player_frame, opt);
        opt = null;

        //Starting layout variables
        tituloActual = (TextView) findViewById(R.id.tituloActual);
        artistaActual = (TextView) findViewById(R.id.artistaActual);
        albumActual = (TextView) findViewById(R.id.albumActual);
        playingUbication = (SeekBar) findViewById(R.id.playingUbication);
        playpauseActual = (ImageButton) findViewById(R.id.playpauseActual);
        positionActual = (TextView) findViewById(R.id.currentPlayingPosition);
        durationActual = (TextView) findViewById(R.id.currentPlayingDuration);
        playlist = getListView();

        playingUbication.setOnSeekBarChangeListener(this);

        if(Reproductor.a != -1) {
            setThings();
            playpauseActual.setImageResource(R.drawable.ic_pause);
            playpauseActual.setTag("pause");
        }
        setBackground(getResources().getDrawable(R.drawable.graphical_player));

        H = true;
        h = new Handler();
        new Thread(this, "Player GUI").start();
    }

    private void setThings() {
        tituloActual.setText(PlaylistManager.self.get(0).title);
        artistaActual.setText(PlaylistManager.self.get(0).artist);
        albumActual.setText(PlaylistManager.self.get(0).album);
        positionActual.setText("0:00");
        durationActual.setText("0:00");
        ArrayList<HashMap<String, String>> toPlaylistView = new ArrayList<HashMap<String, String>>();
        ArrayList<Song> playlist = PlaylistManager.self.getPlaylist();
        if(playlist != null) {
            for(int i = 1; i < playlist.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("titulo", playlist.get(i).title);
                map.put("artista", playlist.get(i).artist);
                toPlaylistView.add(map);
            }

            ListAdapter adapter = new SimpleAdapter(this, toPlaylistView, R.layout.simplified_list_item,
                    new String[] {"titulo", "artista"},
                    new int[] {R.id.simplifiedTitle, R.id.simplifiedArtist});

            setListAdapter(adapter);

            this.playlist.setLongClickable(true);
            this.playlist.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    PlaylistManager.self.deleteSong(arg2 + 1);
                    return false;
                }
            });
            doThings = false;
        }

        width = findViewById(R.id.activityReproductorGrafico).getMeasuredWidth();
        height = findViewById(R.id.activityReproductorGrafico).getMeasuredHeight();
        if(background == null && width > 0 && height > 0)
            background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    public void playpause(View v) {
        if(Reproductor.a != -1) {
            if(!Reproductor.paused) {
                playpauseActual.setImageResource(R.drawable.ic_stat_name);
                playpauseActual.setTag("play");
            } else {
                playpauseActual.setImageResource(R.drawable.ic_pause);
                playpauseActual.setTag("pause");
            }

            Reproductor.pause();
        }
    }

    public void stop(View v) {
        if(Reproductor.a != -1) {
            PlaylistManager.self.stopPlaying();
            playpauseActual.setImageResource(R.drawable.ic_stat_name);
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

    private Object[] compoundDrawable(int width, int height) {
        if(PlaylistManager.self.get(0).album != null && PlaylistManager.self.get(0).album.length() != 0) {
            Album album = new Album(PlaylistManager.self.get(0).artist, PlaylistManager.self.get(0).album);
            String albumart = album.getInfo();
            InputStream is;
            try {
                //Download the image content
                is = (InputStream) new URL(albumart).getContent();
                //Make Bitmaps from resources & the downloaded image
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                if(fondo == null)
                    fondo = BitmapFactory.decodeResource(getResources(), R.drawable.graphical_player_frame);
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = 1;
                Bitmap albumdraw = BitmapFactory.decodeStream(is, null, opt);
                opt = null;
                //Create the compound image and its canvas
                if(background == null)
                    background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(background);

                //Paint on canvas
                canvas.drawBitmap(albumdraw, new Rect(0, 0, albumdraw.getWidth(), albumdraw.getHeight()),
                        new Rect(0, height/6, width, width + (height/12)), paint); //La mitad mas un tercio | la mitad mas un tercio entre la mitad
                //Obatin colors before return, because of background black color replacement
                ColorArt colorart = new ColorArt(albumdraw);
                //and here black is replaced by the background color given by ColorArt
                paint.setColorFilter(new LightingColorFilter(Color.BLACK, colorart.getBackgroundColor()));
                canvas.drawBitmap(fondo, new Rect(0, 0, fondo.getWidth(), fondo.getHeight()), new Rect(0, 0, width, height), paint);

                //clear before return
                is.close(); is = null;
                album = null;
                paint = null;
                canvas = null;
                albumdraw.recycle();
                albumdraw = null;
                return new Object[] {new BitmapDrawable(getResources(), background).getCurrent(), colorart};
            } catch (MalformedURLException e) {
                Log.e(TAG,"Error en el enlace: "+ e.toString() + "(" + albumart + ")");
            } catch (IOException e) {
                Log.e(TAG,"Error: "+ e.toString());
            }
        }
        return new Object[] {null, null};
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
                        if(doThings || (width == 0 && height == 0))
                            setThings();
                        //Time stuff
                        if(PlaylistManager.self.get(0).title != null && Reproductor.a != -1 && Reproductor.reproductor.isPlaying()) {
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
                if(PlaylistManager.self.get(0).title != null && width != 0 && height != 0 && !song.equals(PlaylistManager.self.get(0).title)) {
                    if(PlaylistManager.self.get(0).album != "" && PlaylistManager.self.get(0).album != null)
                        new Background().execute(width, height);
                    song = PlaylistManager.self.get(0).title;
                    doThings = true;
                } else if(PlaylistManager.self.get(0).title == null && song != null) {
                    song = null;
                    h.post(new Runnable() {
                        public void run() {
                            tituloActual.setText("");
                            artistaActual.setText("");
                            albumActual.setText("");
                            positionActual.setText("0:00");
                            durationActual.setText("0:00");
                            setBackground(getResources().getDrawable(R.drawable.graphical_player));
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
            if((float)(progress / 1000f) >= 0.98f)
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
            synchronized (this) {
                final Object[] d = compoundDrawable(params[0], params[1]);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        setBackground((Drawable) d[0]);
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
            }
            return null;
        }

    }
}