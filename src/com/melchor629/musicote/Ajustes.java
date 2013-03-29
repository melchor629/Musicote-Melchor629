package com.melchor629.musicote;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Actividad de preferencias
 * También es la actividad para los ajustes
 * @author melchor
 *
 */
public class Ajustes extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstancedState){
        super.onCreate(savedInstancedState);

        addPreferencesFromResource(R.xml.ajustes);
    }
}
