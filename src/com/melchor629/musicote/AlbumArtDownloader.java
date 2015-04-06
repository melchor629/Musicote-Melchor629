package com.melchor629.musicote;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.melchor629.musicote.scrobbler.Album;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Descarga la carátula del álbum si es posible
 * TODO solo pedir last.fm si no esta en la base de datos
 */
public class AlbumArtDownloader extends AsyncTask<PlaylistManager.Song, Integer, Bitmap> {
    protected AlbumArtDownloaderListener listener;

    @Override
    protected Bitmap doInBackground(PlaylistManager.Song... params) {
        //Looper.prepare();

        Album album = new Album(params[0].artist, params[0].album);
        String url = album.getInfo(5);
        Bitmap bmp = null;
        try {
            bmp = Picasso.with(MainActivity.appContext).load(url).get();
        } catch(IOException e) {
            Log.e(this.getClass().getName(), "No se ha podido descargar la carátula", e);
        }
        return bmp;
    }

    @Override
    protected void onPreExecute() {
        if(listener != null) {
            listener.onPreExecute();
        }
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        if(listener != null) {
            listener.onProgressUpdate(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        if(listener != null) {
            listener.onPostExecute(bitmap);
        }
    }

    public AlbumArtDownloader setListener(AlbumArtDownloaderListener listener) {
        this.listener = listener;
        return this;
    }

    public interface AlbumArtDownloaderListener {
        void onPreExecute();
        void onProgressUpdate(int progress);
        void onPostExecute(Bitmap bitmap);
    }
}
