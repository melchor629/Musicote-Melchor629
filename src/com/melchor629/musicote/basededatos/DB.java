package com.melchor629.musicote.basededatos;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A data base class for the app.<br>
 * For acces in, type: <br>&nbsp;&nbsp;<code>DB mDbHelper = new DB(getContext());</code><br>
 * Once accessed in, you can put, read, delete & update information, see how: <br><br>
 * <b>Put information:</b><br><code>
 * // Gets the data repository in write mode<br>
 * SQLiteDatabase db = mDbHelper.getWritableDatabase();<br>
 * <br>
 * // Create a new map of values, where column names are the keys<br>
 * ContentValues values = new ContentValues();<br>
 * values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID, id);<br>
 * values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);<br>
 * values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT, content);<br>
 * <br>
 * // Insert the new row, returning the primary key value of the new row<br>
 * long newRowId;<br>
 * newRowId = db.insert(<br>
 * DB_entry.FeedEntry.TABLE_NAME,<br>
 * DB_entry.FeedEntry.COLUMN_NAME_NULLABLE,<br>
 * values);<br></code><br>
 * <b>Read information:</b><br>
 * <code>
 * SQLiteDatabase db = mDbHelper.getReadableDatabase();<br>
 * <br>
 * // Define a projection that specifies which columns from the database<br>
 * // you will actually use after this query.<br>
 * String[] projection = {<br>
 * DB_entry.FeedEntry._ID,<br>
 * DB_entry.FeedEntry.COLUMN_NAME_TITLE,<br>
 * DB_entry.FeedEntry.COLUMN_NAME_UPDATED,<br>
 * ...<br>
 * };<br>
 * <br>
 * // How you want the results sorted in the resulting Cursor<br>
 * String sortOrder =<br>
 * DB_entry.FeedEntry.COLUMN_NAME_UPDATED + " DESC";<br>
 * <br>
 * Cursor c = db.query(<br>
 * DB_entry.FeedEntry.TABLE_NAME,  // The table to query<br>
 * projection,                               // The columns to return<br>
 * selection,                                // The columns for the WHERE clause<br>
 * selectionArgs,                            // The values for the WHERE clause<br>
 * null,                                     // don't group the rows<br>
 * null,                                     // don't filter by row groups<br>
 * sortOrder                                 // The sort order<br>
 * );</code><br><br>
 * <b>Delete information:</b><br>
 * <code>
 * // Define 'where' part of query.<br>
 * String selection = DB_entry.FeedEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";<br>
 * // Specify arguments in placeholder order.<br>
 * String[] selectionArgs = { String.valueOf(rowId) };<br>
 * // Issue SQL statement.<br>
 * db.delete(table_name, selection, selectionArgs);</code><br><br>
 * <b>Update information;</b><br>
 * <code>
 * SQLiteDatabase db = mDbHelper.getReadableDatabase();<br>
 * <br>
 * // New value for one column<br>
 * ContentValues values = new ContentValues();<br>
 * values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);<br>
 * <br>
 * // Which row to update, based on the ID<br>
 * String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";<br>
 * String[] selectionArgs = { String.valueOf(rowId) };<br>
 * <br>
 * int count = db.update(<br>
 * DB_entry.FeedEntry.TABLE_NAME,<br>
 * values,<br>
 * selection,<br>
 * selectionArgs);</code>
 *
 * @author melchor9000
 */
public class DB extends SQLiteOpenHelper {

    public DB(Context context) {
        super(context, DB_entry.DATABASE_MUSICOTE, null, DB_entry.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        //db.execSQL(DB_entry.CREATE_CANCIONES); se hace en MainActivity
        db.execSQL(DB_entry.CREATE_ACCESO);
        ContentValues values = new ContentValues();
        values.put("tabla", "canciones");
        values.put("fecha", System.currentTimeMillis());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DB_entry.DELETE_CANCIONES);
        db.execSQL(DB_entry.DELETE_ACCESO);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean ifTableExists(SQLiteDatabase db, String tableName) {
        if(tableName == null || db == null || !db.isOpen())
            return false;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if(!cursor.moveToFirst() || cursor.getCount() == 0)
            return false;
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public void actualizarAcceso(SQLiteDatabase db, String tabla, long time) {
        db.execSQL(DB_entry.CREATE_ACCESO);
        ContentValues values = new ContentValues();
        values.put("fecha", time);
        db.update("acceso", values, "tabla = ?", new String[] {tabla});
    }

    public long obtenerAcceso(SQLiteDatabase db, String tabla) {
        Cursor c = db.query("acceso", new String[] {"fecha"}, "tabla = ?", new String[] {tabla}, null, null, null);
        c.moveToFirst();
        return c.getLong(c.getColumnIndexOrThrow("fecha"));
    }

    public boolean isNecesaryUpgrade(SQLiteDatabase db, SharedPreferences pref) {
        long ultimo = obtenerAcceso(db, "canciones");
        long ahora = System.currentTimeMillis();
        long diff = Long.parseLong(pref.getString("updateTime", "15"), 10) * 60l * 1000l;
        Log.d("DB", "Is necesary upgrade table contents? "
                + (((ahora - ultimo) > diff) ? "True" : "False, will be in " + ((ahora - ultimo) / 60l / 1000l)) + " of " + diff / 60l / 1000l + " minutes.");
        return (ahora - ultimo) > diff;
    }

    public Cursor get(SQLiteDatabase db, String query) {
        String sortOrder = DB_entry.COLUMN_NAME_ID + " ASC";
        String which = DB_entry.COLUMN_NAME_TITULO + " LIKE ? OR " + DB_entry.COLUMN_NAME_ARTISTA + " LIKE ? OR " + DB_entry.COLUMN_NAME_ALBUM + " LIKE ?";
        String[] where = {"%" + query + "%", "%" + query + "%", "%" + query + "%"};

        return db.query(
                DB_entry.TABLE_CANCIONES,                 // The table to query
                null,                                     // The columns to return
                which,                                    // The columns for the WHERE clause
                where,                                    // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }
}
