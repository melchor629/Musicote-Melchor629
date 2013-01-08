package com.melchor629.musicote.actualizador;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.melchor629.musicote.R;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Servicio de actualizaciones, las busca y te saltan notificaciones
 * @author melchor
 *
 */
public class servicio extends Service {
	
	private static final String TAG = "Actualizador";
	
	/**
	 * Constantes para saber que version tiene, igual, superior o inferior
	 */
	public static final int EQUAL = 1;
	public static final int MINUS = 2;
	public static final int MAJUS = 3;
	

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		Log.i(TAG, "Actualizador de Musicote iniciado...");
		while(true){
			ConnectivityManager connMgr = (ConnectivityManager) 
					getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()){
				String v = null;
				int ver = 0;
				
				try {
					v = version();
					ver = compVer(v);
				} catch (IOException e) {
					Log.e(TAG,"Error: "+ e.toString());
				}
				//TODO Poner aqui lo de comprovar versión con Jelly Bean or UP
				notificar(v, ver);
			}else{
				try {
					Thread.sleep(3600000);
				} catch (InterruptedException e) {
					Log.e(TAG,"Error al descansar (WTF!?): "+ e.toString());
				}
			}
		}
	}
	
	/**
	 * Comprobador de versiones
	 * @param v
	 * @return int
	 */
	private int compVer(String v) {
		if(v.equals(getString(R.string.version)))
			return EQUAL;
		if(v.compareTo(getString(R.string.version)) < 0)
			return MINUS;
		if(v.compareTo(getString(R.string.version)) > 0)
			return MAJUS;
		return EQUAL;
	}

	/**
	 * Descarga el archivo con la versión del app
	 * @return
	 * @throws IOException
	 */
	private String version() throws IOException {
		InputStream is = null;
		URL url = new URL("https://github.com/melchor629/Musicote-Melchor629/raw/master/res/raw/version");
		String version = null;
		
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setReadTimeout(10000);
		http.setConnectTimeout(10000);
		http.setRequestMethod("GET");
		http.setDoInput(true);
		http.connect();
		
		int resp = http.getResponseCode();
		if(resp == 200){
			is = http.getInputStream();
			version = is.toString();
		}
		
		is.close();
		return version;
	}
	
	@SuppressLint("NewApi")
	private void notificar(String v, int ver) {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("¡Tienes una actualización!")
		        .setContentText("La aplicación de músicote ha encontrado una actualización de ella misma. Aprete aqui para descargarla...");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, Descarga.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(Descarga.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(123, mBuilder.build());
	}
}
