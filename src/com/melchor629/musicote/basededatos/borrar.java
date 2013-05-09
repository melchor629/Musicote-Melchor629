package com.melchor629.musicote.basededatos;

import com.melchor629.musicote.R;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

/**
 * 
 * @author melchor9000
 *
 */
public class borrar extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bd_borrar);
	}
	
	public void yes(View v) {
		DB dbh = new DB(getBaseContext());
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.execSQL(DB_entry.DELETE_CANCIONES);
		db.execSQL(DB_entry.CREATE_CANCIONES);
		dbh.ultimo_acceso(db, "canciones");
		db.close();
		finish();
	}
	
	public void no(View v) {
		finish();
	}
}
