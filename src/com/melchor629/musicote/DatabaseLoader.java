package com.melchor629.musicote;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import java.io.File;
import java.util.ArrayList;

/**
 * Carga la base de datos del servidor y lo guarda en la base de
 * datos
 */
public class DatabaseLoader extends AsyncTask<Void, Void, Void> {
    protected DatabaseLoaderListener listener;

    @Override
    protected Void doInBackground(Void... params) {
        ArrayList list = Utils.getHashMapFromUrl("http://" + MainActivity.HOST + MainActivity.BASE_API_URL);
        ArrayList<String> artists = new ArrayList<>();

        if(list == null) {
            Toast.makeText(MainActivity.appContext, MainActivity.appContext.getString(R.string.err_server_conn), Toast.LENGTH_LONG).show();
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

                if(!artists.contains(map.get("artista"))) {
                    artists.add(map.get("artista"));
                }

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
        if(listener != null)
            listener.onLoaded();
    }

    public interface DatabaseLoaderListener {
        public void onLoaded();
    }
}
