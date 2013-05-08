/**
 * 
 */
package com.melchor629.musicote;

import android.provider.BaseColumns;

/**
 * A SQLite Data base class for the app
 * @author melchor9000
 *
 */
public abstract class DB implements BaseColumns {
	
	public static final int    DATABASE_VERSION = 1;
	public static final String DATABASE_MUSICOTE = "musicote.db";
	public static final String TABLE_CANCIONES = "canciones";
	public static final String COLUMN_NAME_ID = "id";
	public static final String COLUMN_NAME_TITULO = "titulo";
	public static final String COLUMN_NAME_ARTISTA = "artista";
	public static final String COLUMN_NAME_ALBUM = "album";
	public static final String COLUMN_NAME_ARCHIVO = "archivo";
	public static final String COLUMN_NAME_DURACION = "duracion";
	
	private final String CREATE_CANCIONES = "CREATE TABLE " + TABLE_CANCIONES + " IF NOT EXISTS (" + COLUMN_NAME_ID + " INTEGER PRIMARY KEY ASC, "
			+ COLUMN_NAME_TITULO +", " + COLUMN_NAME_ARTISTA + ", " + COLUMN_NAME_ALBUM + ", " + COLUMN_NAME_ARCHIVO + ", "
			+ COLUMN_NAME_DURACION + ");";
	private final String DELETE_CANCIONES = "DROP TABLE IF EXISTS " + TABLE_CANCIONES;

	/**
	 * No constructor for DB
	 */
	private DB() { }

}
