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
	
	public static final String TABLE_CANCIONES = "canciones";
	public static final String COLUMN_NAME_ID = "id";
	public static final String COLUMN_NAME_TITULO = "titulo";
	public static final String COLUMN_NAME_ARTISTA = "artista";
	public static final String COLUMN_NAME_ALBUM = "album";
	public static final String COLUMN_NAME_ARCHIVO = "archivo";
	public static final String COLUMN_NAME_DURACION = "duracion";

	/**
	 * No constructor for DB
	 */
	private DB() { }
	
}
