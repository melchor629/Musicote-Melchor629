package com.melchor629.musicote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.MenuItem;

/**
 * Actividad de preferencias
 * También es la actividad para los ajustes
 *
 * @author melchor
 */
public class Ajustes extends Activity {

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AjustesFragment())
                .commit();

        getActionBar().setIcon(R.drawable.ic_launcher);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.appContext);
    }

    public static class AjustesFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstancedState) {
            super.onCreate(savedInstancedState);
            addPreferencesFromResource(R.xml.ajustes);
        }
    }
}
