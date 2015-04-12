package com.melchor629.musicote;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;
import com.melchor629.musicote.scrobbler.Album;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Descarga la carátula del álbum si es posible
 */
public class AlbumArtDownloader extends AsyncTask<PlaylistManager.Song, Integer, Bitmap> {
    protected AlbumArtDownloaderListener listener;
    private Handler handler;
    private DB db;

    public AlbumArtDownloader() {
        super();
        handler = new Handler();
        db = new DB(MainActivity.appContext);
    }

    @Override
    protected Bitmap doInBackground(PlaylistManager.Song... params) {
        Album album = new Album(params[0].artist, params[0].album);
        SQLiteDatabase sqlite = db.getWritableDatabase();
        String url;
        Bitmap bmp = null;

        Cursor c = sqlite.query(DB_entry.TABLE_CARATULAS,
                new String[]{DB_entry.COLUMN_CARATULAS_URL},
                DB_entry.COLUMN_CARATULAS_ALBUM + " = \"" + params[0].album + "\"" +
                " AND " + DB_entry.COLUMN_ARTISTAS_ARTISTA + " = \"" + params[0].artist + "\"",
                null,
                null,
                null,
                null);
        if(c.moveToFirst())
            url = c.getString(c.getColumnIndex(DB_entry.COLUMN_CARATULAS_URL));
        else {
            url = album.getInfo(5);
            ContentValues cv = new ContentValues();
            cv.put(DB_entry.COLUMN_CARATULAS_ALBUM, params[0].album);
            cv.put(DB_entry.COLUMN_CARATULAS_ARTISTA, params[0].artist);
            cv.put(DB_entry.COLUMN_CARATULAS_URL, url);
            sqlite.insert(DB_entry.TABLE_CARATULAS, "null", cv);
        }
        c.close();

        try {
            bmp = Picasso.with(MainActivity.appContext).load(url).get();
        } catch(IOException e) {
            Log.e(this.getClass().getName(), "No se ha podido descargar la carátula", e);
        } finally {
            sqlite.close();
        }
        return bmp;
    }

    @Override
    protected void onPreExecute() {
        if(listener != null) {
            handler.post(new Runnable() {
                public void run() {
                    listener.onPreExecute();
                }
            });
        }
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        if(listener != null) {
            handler.post(new Runnable() {
                public void run() {
                    listener.onProgressUpdate(progress[0]);
                }
            });
        }
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        if(listener != null) {
            //handler.post(new Runnable() {
            //    public void run() {
                    listener.onPostExecute(bitmap);
            //    }
            //});
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
