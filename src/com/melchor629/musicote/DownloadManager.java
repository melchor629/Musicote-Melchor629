package com.melchor629.musicote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadManager extends Service {
    private String file;
    private final static File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    private NotificationManager nm;
    private int progress;

    public DownloadManager() { }

    @Override
    public int onStartCommand(Intent intent, int flags, int StartID) {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        file = intent.getStringExtra("file");
        dir.mkdir();
        if(isSDMounted()) {
            if(isDownloaded(file)) {
                Toast.makeText(getApplicationContext(), "El arshivo a sio descagao", Toast.LENGTH_LONG).show();
            } else {
                startForeground(4, notification("Descargando musicote...", file, true));
                downloadFile(file);
            }
        } else {
            Toast.makeText(getApplicationContext(), "No hay SD conectada", Toast.LENGTH_SHORT).show();
            notification("No hay SD", "Para descargar el archivo, se requiere que haya una SD montada", false);
        }
        return START_STICKY;
    }

    public boolean isDownloaded(String file) {
        return new File(dir, file.substring(file.lastIndexOf('/'))).exists()
                || file.startsWith(dir.getAbsolutePath());
    }

    public boolean isSDMounted() {
        return Environment.MEDIA_MOUNTED.equals("mounted");
    }

    public Notification notification(String title, String body, boolean ongoing) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification
                //.setSmallIcon() TODO
                .setContentTitle(title)
                .setContentText(body)
                .setOngoing(ongoing);
        nm.cancel(4 + (ongoing ? 0 : 1));
        Notification n = notification.build();
        nm.notify(4 + (ongoing ? 0 : 1), n);
        return n;
    }

    private void notifprog(String title, String body, int progress) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification
                //.setSmallIcon() TODO
                .setContentTitle(title)
                .setContentText(body)
                .setProgress(100, progress, progress == 0)
                .setOngoing(true);
        nm.cancel(4);
        nm.notify(4, notification.build());
    }

    public void downloadFile(final String file) {
        progress = 0;

        new Thread(new Runnable() {
            public void run() {
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        boolean loop = true;
                        while(loop) {
                            notifprog("Descargando musicote...", file, progress);
                            try { Thread.sleep(1000); } catch(Exception e) {loop = false;}
                        }
                    }
                }, "DownloadManager Notification");
                th.start();

                try {
                    URL url = new URL(file.replace(" ", "%20"));
                    String arch = file.substring(file.lastIndexOf("/") + 1);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
                    File songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), arch);
                    OutputStream output = new FileOutputStream(songFile);

                    byte data[] = new byte[8192];
                    long total = 0;
                    int count;
                    while((count = input.read(data)) != -1) {
                        total += count;
                        progress = (int) (total * 100 / fileLength);
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();

                    Looper.prepare();
                    th.interrupt();
                    notification("Archivo descargado", songFile.getAbsolutePath(), false);
                    changeSQL(file, true);
                    Log.d("DownloadManager", "Archivo " + songFile.getAbsolutePath() + " descargado completamente");
                } catch(MalformedURLException e) {
                    progress = -1;
                    Log.e("DownloadManager", "Error: " + e.toString());
                } catch(FileNotFoundException e) {
                    progress = -1;
                    Log.e("DownloadManager", "Error: " + e.toString());
                } catch(IOException e) {
                    progress = -1;
                    Log.e("DownloadManager", "Error: " + e.toString());
                }
            }
        }, "DownloadManager Downloader").start();
    }

    private void changeSQL(String bef, boolean a) {
        bef = bef.replace("http://" + MainActivity.url, "");
        DB db = new DB(getApplicationContext());
        SQLiteDatabase d = db.getWritableDatabase();
        if(d == null) return;
        d.execSQL(String.format("UPDATE %s SET %s=\"%b\" WHERE archivo = \"%s\"", DB_entry.TABLE_CANCIONES,
                DB_entry.COLUMN_NAME_DOWNLOADED, a, bef));
        d.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
