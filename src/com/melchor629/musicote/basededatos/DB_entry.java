/**
 *
 */
package com.melchor629.musicote.basededatos;

import android.provider.BaseColumns;

/**
 * A SQLite Data base schema class for the app
 *
 * @author melchor9000
 */
public abstract class DB_entry implements BaseColumns {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_MUSICOTE = "musicote.db";

    //TABLA canciones
    public static final String TABLE_CANCIONES = "canciones";
    public static final String COLUMN_CANCIONES_ID = "id";
    public static final String COLUMN_CANCIONES_TITULO = "titulo";
    public static final String COLUMN_CANCIONES_ARTISTA = "artista";
    public static final String COLUMN_CANCIONES_ALBUM = "album";
    public static final String COLUMN_CANCIONES_ARCHIVO = "archivo";
    public static final String COLUMN_CANCIONES_DURACION = "duracion";
    public static final String COLUMN_CANCIONES_DOWNLOADED = "downloaded";

    public static final String CREATE_CANCIONES = String.format("CREATE TABLE IF NOT EXISTS %s " +
            "(%s INTEGER PRIMARY KEY ASC, %s, %s, %s, %s, %s, %s)", TABLE_CANCIONES, COLUMN_CANCIONES_ID,
            COLUMN_CANCIONES_TITULO, COLUMN_CANCIONES_ARTISTA, COLUMN_CANCIONES_ALBUM, COLUMN_CANCIONES_ARCHIVO,
            COLUMN_CANCIONES_DURACION, COLUMN_CANCIONES_DOWNLOADED);
    public static final String DELETE_CANCIONES = "DROP TABLE IF EXISTS " + TABLE_CANCIONES;
    public static final String EMPTY_CANCIONES = "DELETE FROM " + TABLE_CANCIONES;

    //TABLA artistas
    public static final String TABLE_ARTISTAS = "artistas";
    public static final String COLUMN_ARTISTAS_ID = "ID";
    public static final String COLUMN_ARTISTAS_ARTISTA = "artista";

    public static final String CREATE_ARTISTAS = String.format("CREATE TABLE IF NOT EXISTS %s " +
            "(%s INTEGER PRIMARY KEY ASC, %s)", TABLE_ARTISTAS, COLUMN_ARTISTAS_ID, COLUMN_ARTISTAS_ARTISTA);
    public static final String DELETE_ARTISTAS = "DROP TABLE IF EXISTS " + TABLE_ARTISTAS;

    /** No constructor for DB */
    private DB_entry() { }
}
