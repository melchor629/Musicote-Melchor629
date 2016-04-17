package com.melchor629.musicote.basededatos;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.melchor629.musicote.MainActivity;

import java.util.ArrayList;

public class DBArrayList<E> extends ArrayList<LinkedTreeMap<String, String>> {
    SQLiteDatabase db;
    Cursor c;

    public DBArrayList() {
        DB mDbHelper = new DB(MainActivity.appContext);
        db = mDbHelper.getReadableDatabase();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        db.close();
    }

    @Override
    public int size() {
        return getCursor().getCount();
    }

    @Override
    public LinkedTreeMap<String, String> get(int pos) {
        LinkedTreeMap<String, String> obj = new LinkedTreeMap<>();

        Cursor c = getCursor();

        c.moveToPosition(pos);
        try {
            long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ID));
            String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_TITULO));
            String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARTISTA));
            String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ALBUM));
            String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARCHIVO));
            String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DURACION));
            String downloaded = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DOWNLOADED));

            obj.put("id", "" + id);
            obj.put("titulo", titulo);
            obj.put("artista", artista);
            obj.put("album", album);
            obj.put("archivo", archivo);
            obj.put("duracion", duracion);
            obj.put("downloaded", downloaded.equalsIgnoreCase("true") ? "{fa-mobile}" : "{fa-cloud}");
        } catch(IllegalArgumentException e) {
            Log.e(this.getClass().getCanonicalName(), "Error", e);
        }

        return obj;
    }

    private Cursor getCursor() {
        if(c != null) return c;
        String[] projection = {
                DB_entry.COLUMN_CANCIONES_ID,
                DB_entry.COLUMN_CANCIONES_TITULO,
                DB_entry.COLUMN_CANCIONES_ARTISTA,
                DB_entry.COLUMN_CANCIONES_ALBUM,
                DB_entry.COLUMN_CANCIONES_DURACION,
                DB_entry.COLUMN_CANCIONES_ARCHIVO,
                DB_entry.COLUMN_CANCIONES_DOWNLOADED
        };

        String sortOrder = DB_entry.COLUMN_CANCIONES_ID + " ASC";

        c =  db.query(
                DB_entry.TABLE_CANCIONES, // The table to query
                projection,               // The columns to return
                null,                     // The columns for the WHERE clause
                null,                     // The values for the WHERE clause
                null,                     // don't group the rows
                null,                     // don't filter by row groups
                sortOrder                 // The sort order
        );
        return c;
    }
}
