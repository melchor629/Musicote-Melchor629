package com.melchor629.musicote;

import com.melchor629.musicote.scrobbler.Auth;
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

    static MediaPlayer reproductor = new MediaPlayer();

    private String url;
    private static boolean paused;
    public static String tit;
    public static String art;
    private coso cosa;
    private NotificationManager nm;

    public static long a;

    public int onStartCommand (Intent intent, int flags, int StartID){
        Toast.makeText(this, "Reproductor de musicote abierto", Toast.LENGTH_LONG).show();
        url = intent.getStringExtra("archivo");
        tit = intent.getStringExtra("titulo");
        art = intent.getStringExtra("artista");
        initMediaPlayer(url, tit, art);
        return START_STICKY;
    }

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
    
    public static void pause(){
    	if(reproductor.isPlaying()){
			reproductor.pause();
			paused = true;
    	}else{
    		reproductor.start();
    		paused = false;
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
       Toast.makeText(this, "Reproductor de musicote cerrado", Toast.LENGTH_LONG).show();
    }

    class coso extends Thread {
        public void run(MediaPlayer player, SharedPreferences pref, NotificationManager nm){
            player.start();
            try{
                for(int i = 0; ((player.isPlaying() || paused) ? true : false) & i < player.getDuration(); i+=1000){
                    try{
                        if((int)(i/1000) == (int)((player.getDuration()/1000)/2)){
                               if(pref.getBoolean("lastact", false) == true){
                                   Scrobble scr = new Scrobble(tit, art);
                                   scr.scrobble();
                               }
                        }
                        a = (long)(i/(long)(player.getDuration()/100)); Log.d("Reproductor", "a = "+a+"-"+i+"-"+player.getDuration());
                    	if(paused)
                    		i-=1000;
                        Thread.sleep(1000);
                    }catch (Exception e){
                        Log.e("Reproductor", "No se sabe porqué pero se ha cerrado...");
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
