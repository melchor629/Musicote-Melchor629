package com.melchor629.musicote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.melchor629.musicote.PlaylistManager.Song;
import com.melchor629.musicote.PlaylistManager.callback;
import com.melchor629.musicote.scrobbler.Auth;
import com.melchor629.musicote.scrobbler.Peticiones;
import com.melchor629.musicote.scrobbler.Scrobble;

import static com.melchor629.musicote.PlaylistManager.self;

/**
 * Reproductor del Musicote 0.1
 *
 * @author melchor629
 */
public class Reproductor extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    /** Static variable for the Media Player */
    static MediaPlayer reproductor = new MediaPlayer();
    static callback beforeEnd, onEnd;

    private coso cosa;
    private NotificationManager nm;
    private volatile boolean True = true;
    private PowerManager.WakeLock wl;
    private Song song;
    private static Reproductor me;

    public volatile static double a = -1;
    public volatile static boolean paused;

    public int onStartCommand(Intent intent, int flags, int StartID) {
        me = this;
        initMediaPlayer();
        PowerManager mgr = (PowerManager)getBaseContext().getSystemService(Context.POWER_SERVICE);
        wl = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Musicote");
        wl.acquire();
        return START_STICKY;
    }

    /**
     * When the service is ready, start playing the song
     */
    private void initMediaPlayer() {
        song = self.get(PlaylistManager.pos);
        reproductor = newPlayer();
        notification();
        if(onEnd != null && beforeEnd != null)
            self.callbacks();
    }

    /** Configura un reproductor */
    private MediaPlayer newPlayer() {
        String url = song.url;
        MediaPlayer reproductor = new MediaPlayer(); // initialize it here
        reproductor.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            url = url.replace(" ", "%20");
            if(url.startsWith("http"))
                reproductor.setDataSource(url);
            else
                reproductor.setDataSource(getApplicationContext(), Uri.parse(url));
        } catch (Exception e) {
            Log.e("Reproductor", "Error al descargar: " + url, e);
            if(e.toString().equals("(1, -1004"))
                Toast.makeText(this, "No se ha podido descargar la canción", Toast.LENGTH_LONG).show();
            stopSelf();
        }
        reproductor.setOnPreparedListener(this);
        reproductor.setOnCompletionListener(this);
        reproductor.setOnErrorListener(this);
        reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        reproductor.prepareAsync(); // prepare async to not block main thread
        return reproductor;
    }

    /** Notificación de la canción */
    private void notification() {
        int mID = 1;

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification
                .setSmallIcon(R.drawable.altavoz)
                .setContentTitle("Musicote")
                .setContentText(String.format("%s %s %s %s", getResources().getString(R.string.playing), song.title,
                        getResources().getString(R.string.playing_of), song.artist))
                .setOngoing(true);

        if(self.getPlaylist().size() > 1) {
            NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
            inbox.setBigContentTitle(String.format("%s %s %s %s",
                    getResources().getString(R.string.playing), song.title, getResources().getString(R.string.playing_of),
                    song.artist));
            for(int i = 0; i < self.getPlaylist().size() || i == 5; i++) {
                if(i == 0)
                    inbox.addLine(getResources().getString(R.string.and_after));
                else
                    inbox.addLine(String.format("%s %s %s",
                            self.getPlaylist().get(i).title, getResources().getString(R.string.playing_of),
                            self.getPlaylist().get(i).artist));
            }
            notification.setStyle(inbox);
        }

        Intent resultIntent = new Intent(this, ReproductorGrafico.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(resultPendingIntent);
        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(mID);
        nm.notify(mID, notification.build());
    }

    /** Se llama cuando el reproductor está listo */
    public void onPrepared(final MediaPlayer player) {
        final SharedPreferences pref = Ajustes.getPreferences();
        new Thread(new Runnable() {
            public void run() {
                if(pref.getBoolean("lastact", false)) {
                    Auth auth = new Auth(pref.getString("usuario", null), pref.getString("contraseña", null));
                    auth.getSK();
                    Scrobble scr = new Scrobble(song.title, song.artist);
                    scr.nowPlaying(player.getDuration()/1000);
                }
            }
        }).start();

        cosa = new coso();
        cosa.nm = nm;
        cosa.pref = pref;
        True = true;
        player.start();
        cosa.start();
    }

    /** Pause or resume the song */
    public static void pause() {
        try {
            if(reproductor.isPlaying()) {
                reproductor.pause();
                paused = true;
            } else {
                reproductor.start();
                paused = false;
            }
        } catch (IllegalStateException e) {
            Log.d("Reproductor", "Se ha invocado el pause, aunque el reproductor está cerrado");
        }
    }

    static void leNext() {
        me.onCompletion(null);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        boolean isNext = self.isNextSong();
        cosa.interrupt();
        True = false;
        song = null;
        if(onEnd != null)
            onEnd.run();
        reproductor.release();
        if(isNext)
            initMediaPlayer();
        else
            this.stopSelf();
    }

    /* (non-Javadoc)
     * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer, int, int)
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("Reproductor", "No se ha podido cargar el archivo: "+song.url);
        //Toast.makeText(getApplicationContext(), "Cannot load '" + song.title + "'", Toast.LENGTH_LONG).show();
        if(onEnd != null)
            onEnd.run();
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wl.release();
        if(cosa != null)
            cosa.interrupt();
        if(reproductor != null)
            reproductor.release();
        nm.cancelAll();
        a = -1;
        song = null;
        beforeEnd = null;
        onEnd = null;
    }

    /**
     * Class for manage the current position of the song, and send the scrobbling
     * @author melchor9000
     */
    class coso extends Thread {
        SharedPreferences pref;
        NotificationManager nm;
        public void run() {
            Looper.prepare();
            try {
                boolean o = true, u = true;

                while(True) {
                    try {
                        a = (reproductor.getCurrentPosition() / (reproductor.getDuration() / 100d));
                        if((int) a >= 50 && o) {
                            if(pref.getBoolean("lastact", false)) {
                                o = false;
                                new Thread(new Runnable() {
                                    public void run() {
                                        Scrobble scr = new Scrobble(song.title, song.artist);
                                        int e = scr.scrobble();
                                        if(e != 0)
                                            Toast.makeText(Reproductor.this, "Last.FM: " + Peticiones.errorM[e], Toast.LENGTH_LONG).show();
                                    }
                                }).start();
                            }
                        }
                        if((reproductor.getDuration() - reproductor.getCurrentPosition())/1000 == 10 && u) {
                            u = false;
                            if(beforeEnd != null)
                                beforeEnd.run();
                        }

                        Thread.sleep(100);
                    } catch (Exception e) {
                        //if(!(e instanceof IllegalStateException)) e.printStackTrace();
                        True = false;
                    }
                }
            } catch (IllegalStateException e) {
                nm.cancelAll();
                a = -1;
                this.interrupt();
                Log.d("Reproductor", "Se ha detectado que el reproductor se ha cerrado, esto tambien se cierra");
            } catch (Exception e) {
                Log.e("Reproductor", "Ha habido un error en el \"coso\": " + e.toString());
            }
        }
    }
}
