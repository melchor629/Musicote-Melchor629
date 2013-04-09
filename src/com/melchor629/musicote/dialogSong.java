/**
 * 
 */
package com.melchor629.musicote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Una actividad que muestra unas opciones para una canción a secas.
 * @author melchor9000
 *
 */
public class dialogSong extends Activity {
    // JSON node keys
    private static final String TAG_NAME = "titulo";
    private static final String TAG_EMAIL = "artista";
    private static final String TAG_PHONE_MOBILE = "album";
    private static final String TAG_DURACIONS = "duracion";
    private static final String TAG_FILE = "archivo";
    
    public static String url;
    public static String name;
    public static String cost;
    public static String description;
    public static String duracion;
    public static String archivo;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        name = in.getStringExtra(TAG_NAME);
        cost = in.getStringExtra(TAG_EMAIL);
        description = in.getStringExtra(TAG_PHONE_MOBILE);
        duracion = in.getStringExtra(TAG_DURACIONS);
        archivo = in.getStringExtra(TAG_FILE);
        
        
    }

}
