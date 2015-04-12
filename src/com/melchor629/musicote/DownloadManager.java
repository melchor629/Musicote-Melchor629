package com.melchor629.musicote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
    private final static File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    private NotificationManager nm;
    private NotificationCompat.Builder notif;
    private int progress;
    private static int mID = 2;
    private int i;

    @Override
    public int onStartCommand(Intent intent, int flags, int StartID) {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notif = new NotificationCompat.Builder(this);
        i = intent.getIntExtra("id", 0);
        String file = intent.getStringExtra("file");
        file = String.format("http://%s%s%s", MainActivity.HOST, MainActivity.BASE_URL, file);
        if(file == null) stopSelf(); //Cerrar servicio si no tenemos FILE
        dir.mkdir();
        if(isSDMounted()) {
            if(isDownloaded(file)) {
                Toast.makeText(getApplicationContext(), getString(R.string.had_been_downloaded), Toast.LENGTH_LONG).show();
                Utils.setFileAsDownloaded(i, true);
            } else {
                notification(getString(R.string.downloading), file, true);
                downloadFile(file);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_external_storage), Toast.LENGTH_SHORT).show();
            notification(getString(R.string.err_download), getString(R.string.no_external_storage), false);
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
        notif
                .setSmallIcon(R.drawable.altavoz) //TODO Cambiar
                .setContentTitle(title)
                .setContentText(body)
                .setOngoing(ongoing)
                .setAutoCancel(true)
                .setProgress(0, 0, false);
        Notification n = notif.build();
        nm.notify(mID, n);
        return n;
    }

    private void notifprog(String title, String body, int progress) {
        notif
                .setSmallIcon(R.drawable.altavoz) //TODO Cambiar
                .setContentTitle(title)
                .setContentText(body)
                .setProgress(100, progress, progress == 0)
                ;//.setOngoing(true);
        nm.notify(mID, notif.build());
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
                            notifprog(getString(R.string.downloading), file, progress);
                            try { Thread.sleep(1000); } catch(Exception e) {loop = false;}
                            if(progress == -1) loop = false;
                        }
                    }
                }, "DownloadManager Notification");
                th.start();

                File songFile;
                String arch = file.substring(file.lastIndexOf("/") + 1);
                songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), arch);
                try {
                    URL url = new URL(file.replace(" ", "%20"));
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
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
                    notification(getString(R.string.done_downloading), songFile.getAbsolutePath(), false);

                    Utils.setFileAsDownloaded(i, true);
                    Log.d("DownloadManager", "Archivo " + songFile.getAbsolutePath() + " descargado completamente");
                } catch(FileNotFoundException e) {
                    progress = -1;
                    Log.e("DownloadManager", "Error: " + e.toString());
                    notification(getString(R.string.err_download), getString(R.string.err_download_web), false);
                    Utils.setFileAsDownloaded(i, false);
                    songFile.delete();
                } catch(IOException e) {
                    progress = -1;
                    Log.e("DownloadManager", "Error: " + e.toString());
                    notification(getString(R.string.err_download), getString(R.string.err_download_io), false);
                    Utils.setFileAsDownloaded(i, false);
                    songFile.delete();
                } catch(Exception e) {
                    progress = -1;
                    Log.e("DownloadManager", "Error: " + e.toString());
                    notification(getString(R.string.err_download), getString(R.string.err_download_intern), false);
                    Utils.setFileAsDownloaded(i, false);
                    songFile.delete();
                }
            }
        }, "DownloadManager Downloader").start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
