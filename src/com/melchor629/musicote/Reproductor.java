package com.melchor629.musicote;

import com.melchor629.musicote.scrobbler.Auth;
import com.melchor629.musicote.scrobbler.Peticiones;
import com.melchor629.musicote.scrobbler.Scrobble;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * Reproductor del Musicote 0.1
 * TODO Mejorar con nuevas cosas el servicio inlcuyendo una interfaz gráfica y Last.FM
 * TODO Cambiar el icono por uno mejor y con tamaños para que el Lint no se queje xD
 * @author melchor
 */
public class Reproductor extends Service implements MediaPlayer.OnPreparedListener {

    /**
     * Static variable for the Media Player
     */
    static MediaPlayer reproductor = new MediaPlayer();

    private String url;
    private static boolean paused;
    public static String tit;
    public static String art;
    private coso cosa;
    private NotificationManager nm;

    public static long a = -1;

    public int onStartCommand (Intent intent, int flags, int StartID){
        Toast.makeText(this, "Reproductor de musicote abierto", Toast.LENGTH_LONG).show();
        url = intent.getStringExtra("archivo");
        tit = intent.getStringExtra("titulo");
        art = intent.getStringExtra("artista");
        initMediaPlayer(url, tit, art);
        return START_STICKY;
    }

    /**
     * When the service is ready, start playing the song
     * @param url <i>URL for the file</i>
     * @param titulo <i>Title of the song</i>
     * @param artista <i>Song' Artist</i>
     */
    public void initMediaPlayer(String url, String titulo, String artista){
        reproductor = new MediaPlayer(); // initialize it here
        reproductor.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try { Log.e("Retraso mental", url.replace(" ", "%20"));
            reproductor.setDataSource(url.replace(" ", "%20"));
        } catch (Exception e) {
            Log.e("Reproductor.Descarga","Error: "+ e.toString());
            if(e.toString().equals("(1, -1004"))
            	Toast.makeText(this, "No se ha podido descargar la canción", Toast.LENGTH_LONG).show();
            Intent in = new Intent(getApplicationContext(), Reproductor.class);
            stopService(in);
        }
        reproductor.setOnPreparedListener(this);
        reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        reproductor.prepareAsync(); // prepare async to not block main thread
        int mID = 1;

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.altavoz)
                .setContentTitle("Musicote")
                .setContentText("Reproduciendo "+titulo+" de "+artista); //TODO poner que sea fijo

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(resultPendingIntent);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mID, notification.build());
    }

    /**
     * Se llama cuando el reproductor está listo
     */
    public void onPrepared(final MediaPlayer player) {
        //player.start();

        SharedPreferences get = PreferenceManager.getDefaultSharedPreferences(this);
        if(get.getBoolean("lastact", false)==true){
            Auth auth = new Auth(get.getString("usuario", null), get.getString("contraseña", null));
            auth.getSK();
            Scrobble scr = new Scrobble(tit, art);
            scr.nowPlaying();
        }else{
            Log.d("Scrobbler", "Nada de Scrobblings...");
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        new Thread(new Runnable(){@Override public void run(){ cosa = new coso();
        cosa.run(player, pref, nm);
        }}).start();
    }
    
    /**
     * Pause or resume the song
     */
    public static void pause(){
        try{
            if(reproductor.isPlaying()){
                reproductor.pause();
                paused = true;
            }else{
                reproductor.start();
                paused = false;
            }
         }catch(IllegalStateException e){
            Log.d("Reproductor Pause", "Se ha invocado el pause, aunque el reproductor está cerrado");
        }
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
	   if(!cosa.getState().toString().equals("NEW"))
		   cosa.interrupt();
       if (reproductor != null)
           reproductor.release();
       nm.cancelAll();
       a = -1;
       Toast.makeText(this, "Reproductor de musicote cerrado", Toast.LENGTH_LONG).show();
    }

    /**
     * Class for manage the current position of the song, and send the scrobbling
     * 
     * @author melchor9000
     *
     */
    class coso extends Thread {
        public void run(MediaPlayer player, SharedPreferences pref, NotificationManager nm){
            player.start();
            Looper.prepare();
            try{
            	boolean o = true;
                while(((player.isPlaying() || paused) ? true : false) & player.getCurrentPosition() < player.getDuration()){
                    try{
                        a = (long)(player.getCurrentPosition()/(long)(player.getDuration()/100));
                        if(a==50 && o){
                            if(pref.getBoolean("lastact", false) == true){
                            	o = false;
                                Scrobble scr = new Scrobble(tit, art);
                                int e = scr.scrobble();
                                if(e != 0)
                                	Toast.makeText(Reproductor.this, "Last.FM: "+Peticiones.errorM[e], Toast.LENGTH_LONG).show();
                            	Log.d("Reproductor->Scrobbler", "Error: "+e+"\nMessage: "+Peticiones.errorM[e]);
                            }
                        }
                        //Log.d("Reproductor", "a = "+a+"-"+player.getCurrentPosition()+"-"+player.getDuration());
                        Thread.sleep(1000);
                    }catch (Exception e){
                        Log.e("Reproductor", "No se sabe porqué pero se ha cerrado...\n"+e.toString());
                    }
                }

                player.stop(); Log.d("FOR", "Se tendria que serrar...");
                nm.cancelAll();
                a = -1;
                Intent in = new Intent(getApplicationContext(), Reproductor.class);
                stopService(in);
            }catch (IllegalStateException e){
                nm.cancelAll();
                a = -1;
                this.interrupt();
                Log.d("Reproductor", "Se ha detectado que el reproductor se ha cerrado, esto tambien se cierra");
            }catch (Exception e){
                Log.e("Reproductor", "Ha habido un error en el \"coso\": "+e.toString());
            }
        }
    }
}
