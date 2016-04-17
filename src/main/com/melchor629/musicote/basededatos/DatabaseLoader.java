package com.melchor629.musicote.basededatos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.melchor629.musicote.MainActivity;
import com.melchor629.musicote.R;
import com.melchor629.musicote.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Carga la base de datos del servidor y lo guarda en la base de
 * datos
 */
public class DatabaseLoader extends AsyncTask<Void, Void, Void> {
    protected DatabaseLoaderListener listener;
    private String error = null;

    @Override
    protected Void doInBackground(Void... params) {
        ArrayList list;

        try {
            list = Utils.getHashMapFromUrl("http://" + MainActivity.HOST + MainActivity.BASE_API_URL);
        } catch(IOException e) {
            error = MainActivity.appContext.getString(R.string.err_server_conn);
            return null;
        }
        TreeSet<String> artists = new TreeSet<>();

        if(list == null) {
            error = MainActivity.appContext.getString(R.string.err_server_conn);
            return null;
        }
        try {
            DB dbHelper = new DB(MainActivity.appContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if(!dbHelper.ifTableExists(db, "canciones"))
                db.execSQL(DB_entry.CREATE_CANCIONES);
            else {
                db.execSQL(DB_entry.EMPTY_CANCIONES);
                db.execSQL("VACUUM");
            }
            long time = System.currentTimeMillis();

            for(Object obj : list) {
                LinkedTreeMap<String, String> map = (LinkedTreeMap<String, String>) obj;
                ContentValues values = new ContentValues();

                //Test if the file is downloaded
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        (map.get("archivo")).substring((map.get("archivo")).lastIndexOf("/") + 1));
                map.put("downloaded", file.exists() ? "{fa-mobile}" : "{fa-cloud}");
                //TODO Tarea que compruebe los archivos guardados comparando con los ID3Tags

                //Putting vaules to be added in DB
                values.put(DB_entry.COLUMN_CANCIONES_ID, map.get("id"));
                values.put(DB_entry.COLUMN_CANCIONES_ARCHIVO, map.get("archivo"));
                values.put(DB_entry.COLUMN_CANCIONES_TITULO, map.get("titulo"));
                values.put(DB_entry.COLUMN_CANCIONES_ARTISTA, map.get("artista"));
                values.put(DB_entry.COLUMN_CANCIONES_ALBUM, map.get("album"));
                values.put(DB_entry.COLUMN_CANCIONES_DURACION, map.get("duracion"));
                values.put(DB_entry.COLUMN_CANCIONES_DOWNLOADED, ""+file.exists());

                artists.add(map.get("artista"));

                //Adding data into DB
                db.insert(DB_entry.TABLE_CANCIONES, "null", values);
            }

            for(String artista : artists) {
                ContentValues values = new ContentValues();
                values.put(DB_entry.COLUMN_ARTISTAS_ARTISTA, artista);
                db.insert(DB_entry.TABLE_ARTISTAS, "null", values);
            }

            db.close();
            Log.d(this.getClass().getName(), String.format("Saved to DB in %dms", System.currentTimeMillis() - time));
        } catch(Exception e) {
            e.printStackTrace();
            Log.i(this.getClass().getName(), "Excepción encontrada: " + e.toString());
        }

        return null;
    }

    public DatabaseLoader setListener(DatabaseLoaderListener l) {
        listener = l;
        return this;
    }

    @Override
    protected void onPostExecute(Void na) {
        if(error != null) {
            Toast.makeText(MainActivity.appContext, error, Toast.LENGTH_LONG).show();
        } else if(listener != null)
            listener.onLoaded();
    }

    /**
     * Obtains a List representation of the song table from the Database
     * @param db an SQLite connexion to database
     * @return List with songs
     */
    public static ArrayList<SongRow> getSongsMap(SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DB_entry.COLUMN_CANCIONES_ID,
                DB_entry.COLUMN_CANCIONES_TITULO,
                DB_entry.COLUMN_CANCIONES_ARTISTA,
                DB_entry.COLUMN_CANCIONES_ALBUM,
                DB_entry.COLUMN_CANCIONES_DURACION,
                DB_entry.COLUMN_CANCIONES_ARCHIVO,
                DB_entry.COLUMN_CANCIONES_DOWNLOADED
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DB_entry.COLUMN_CANCIONES_ID + " ASC";

        Cursor c = db.query(
                DB_entry.TABLE_CANCIONES, // The table to query
                projection,               // The columns to return
                null,                     // The columns for the WHERE clause
                null,                     // The values for the WHERE clause
                null,                     // don't group the rows
                null,                     // don't filter by row groups
                sortOrder                 // The sort order
        );
        ArrayList<SongRow> songList = new ArrayList<>();

        c.moveToFirst();
        try {
            do {
                songList.add(new SongRow(c));
            } while(c.moveToNext());
        } catch (CursorIndexOutOfBoundsException e) {
            db.execSQL(DB_entry.DELETE_CANCIONES);
            Log.e("DB", "Mala integridad de la BD");
        }
        c.close();
        return songList;
    }

    public interface DatabaseLoaderListener {
        void onLoaded();
    }
}
