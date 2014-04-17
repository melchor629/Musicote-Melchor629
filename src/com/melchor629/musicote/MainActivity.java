package com.melchor629.musicote;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.gson.internal.LinkedTreeMap;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Musicote App
 * Melchor629 2013
 *
 *    Copyright 2013 Melchor629
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/

/**
 * Actividad principal de la App, hace muchas cosas<br><i>tag:^(?!.*(EGL_emulation|dalvik)).*$</i>
 * @author melchor
 */
public class MainActivity extends SherlockListActivity implements SearchView.OnQueryTextListener,
        uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener {

    public static String url;
    public static Context appContext;

    private String oldText = "";
    private PullToRefreshLayout mPullToRefreshAttacher;

    private static ArrayList<LinkedTreeMap<String, String>> contactList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Set the user interface layout for this Activity
        // The layout file is defined in the project res/layout/main.xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        appContext = getApplicationContext();
        mPullToRefreshAttacher = (PullToRefreshLayout) findViewById(R.id.mainLayout);
        ActionBarPullToRefresh.from(this)
            .theseChildrenArePullable(android.R.id.list)
            .listener(this)
            .setup(mPullToRefreshAttacher);
        getWindow().getDecorView().setBackgroundColor(android.graphics.Color.rgb(50, 178, 207));

        // La app prueba en busca de la dirección correcta
        WifiManager mw = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = mw.getConnectionInfo();
        String SSID = wi.getSSID();
        Log.i("MainActivity", "Wifi conectado: " + SSID + " " + (SSID != null ? SSID.equals("Madrigal") : ""));
        if(SSID == null) {
            SSID = "";
            Toast.makeText(this, "No está usando WIFI, se recomienda utilizar la app con WIFI", Toast.LENGTH_LONG).show();
        }
        if(SSID.equals("Madrigal") || SSID.contains("Madrigal") || System.getProperty("os.version").equals("3.4.0-gd853d22")) {
            MainActivity.url = "192.168.1.133";
        } else {
            MainActivity.url = "reinoslokos.no-ip.org";
        }
        Log.i("MainActivity", "url: " + url);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Deletes the notification if remains (BUG)
        NotificationManager mn = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Reproductor.a == -1)
            mn.cancel(1);
        mn.cancel(3);

        //Revisa la base de datos
        DB mDbHelper = new DB(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if(!mDbHelper.ifTableExists(db, "canciones") || !mDbHelper.ifTableExists(db, "acceso")) {
            db.execSQL(DB_entry.CREATE_ACCESO);
            db.execSQL(DB_entry.CREATE_CANCIONES);
            ContentValues values = new ContentValues();
            values.put("tabla", "canciones");
            values.put("fecha", System.currentTimeMillis());
        }

        //Actualización de la lista
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(mDbHelper.isNecesaryUpgrade(db, pref) && Utils.HostTest(url) == 200)
            db.execSQL(DB_entry.DELETE_CANCIONES);
        else
            cursordb(db);

        if(!mDbHelper.ifTableExists(db, "canciones"))
            async();

        db.close();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void sis() {
        if(contactList == null) {
            Log.e("MainActivity", "contactList viene vacío...");
            return;
        }
        /**
         * Updating parsed JSON data into ListView
         * */
        ListAdapter adapter = new SimpleAdapter(this, contactList,
                R.layout.list_item,
                new String[] {"titulo", "artista", "album", "downloaded"}, new int[] {
                R.id.name, R.id.email, R.id.mobile, R.id.mainStatusSong});

        try {
            setListAdapter(adapter);
        } catch (Exception e) {
            Log.e("ServerHostDetector", "Er ordenata de mershor ta apagao... " + e.toString());
            Toast.makeText(MainActivity.this, "Ningún servidor activo...", Toast.LENGTH_LONG).show();
        }

        // selecting single ListView item
        ListView lv = getListView();
        lv.setFastScrollEnabled(true);

        // Launching new screen on Selecting Single ListItem
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                LinkedTreeMap<String, String> datos = contactList.get(position);
                String title = getString(R.string.vacio);
                String artist = getString(R.string.vacio);
                String album = getString(R.string.vacio);
                String descr = "-00:00";
                String archivo = "http://" + url + "/musica";
                try {
                    title = ((TextView)view.findViewById(R.id.name)).getText().toString();
                    artist = ((TextView)view.findViewById(R.id.email)).getText().toString();
                    album = ((TextView)view.findViewById(R.id.mobile)).getText().toString();
                    descr = datos.get("duracion");
                    archivo = datos.get("archivo");
                } catch (Exception e) {
                    Log.e("MainActivity", e.toString());
                }

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                in.putExtra("titulo", title);
                in.putExtra("artista", artist);
                in.putExtra("album", album);
                in.putExtra("duracion", descr);
                in.putExtra("archivo", Utils.getUrl(archivo));
                in.putExtra("downloaded", datos.get("downloaded").equals("{fa-mobile}"));

                startActivity(in);
            }
        });

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View v, final int which, long id) {
                final boolean isDownloaded = Utils.isDownloaded(contactList.get(which).get("archivo"));
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(((TextView) v.findViewById(R.id.name)).getText().toString())
                    .setItems(isDownloaded ? R.array.song_options_array2 : R.array.song_options_array,
                            new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which2) {
                            String url = Utils.getUrl(contactList.get(which).get("archivo"));
                            if(which2 == 0) {
                                if(Reproductor.a == -1) {
                                    PlaylistManager.self.startPlaying(((TextView) v.findViewById(R.id.name)).getText().toString(), 
                                        ((TextView) v.findViewById(R.id.email)).getText().toString(),
                                        ((TextView) v.findViewById(R.id.mobile)).getText().toString(), url);
                                } else {
                                    PlaylistManager.self.stopPlaying();
                                    PlaylistManager.self.addSong(((TextView) v.findViewById(R.id.name)).getText().toString(),
                                            ((TextView) v.findViewById(R.id.email)).getText().toString(),
                                            ((TextView) v.findViewById(R.id.mobile)).getText().toString(), url);
                                }
                            } else if(which2 == 1) {
                                if(isDownloaded) {
                                    //TODO Servicio que descarge archivos
                                } else {
                                    
                                }
                            } else if(which2 == 2) {
                                PlaylistManager.self.addSong(((TextView) v.findViewById(R.id.name)).getText().toString(),
                                        ((TextView) v.findViewById(R.id.email)).getText().toString(),
                                        ((TextView) v.findViewById(R.id.mobile)).getText().toString(), url);
                            }
                        }
                    })
                    .create().show();
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO mirar porque solo salen dos iconos
        //Create the search view
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getResources().getString(R.string.menu_search));
        searchView.setOnQueryTextListener(this);

        menu.add("Search")
                .setIcon(R.drawable.abs__ic_search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        getSupportMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.ajustesm).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_cogs)
                .color(Color.WHITE)
                .actionBarSize());
        menu.findItem(R.id.parar).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_music)
                .color(Color.WHITE)
                .actionBarSize()
        );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ajustesm:
                Intent intent = new Intent(MainActivity.this, Ajustes.class);
                startActivity(intent);
                break;
            case R.id.parar:
                Intent intento = new Intent(MainActivity.this, ReproductorGrafico.class);
                intento.putExtra("button", true);
                startActivity(intento);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Clase AsyncTask para descargar el JSON y parsearlo y no desesperar a la peña
     *
     * @author melchor
     */
    private class JSONParseDialog extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            MainActivity.contactList = Utils.getHashMapFromUrl("http://" + MainActivity.url + "/py/api.py");
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    sis(); //Show something to the user
                }
            });

            try {
                // looping through All Songs
                DB dbHelper = new DB(getBaseContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if(!dbHelper.ifTableExists(db, "canciones"))
                    db.execSQL(DB_entry.CREATE_CANCIONES);
                long time = System.currentTimeMillis();

                for(Object obj : MainActivity.contactList) {
                    LinkedTreeMap map = (LinkedTreeMap) obj;
                    ContentValues values = new ContentValues();

                    //Test if the file is downloaded
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                            ((String) map.get("archivo")).substring(((String) map.get("archivo")).lastIndexOf("/") + 1));
                    map.put("downloaded", file.exists() ? "{fa-mobile}" : "{fa-cloud}"); //TODO

                    //Putting vaules to be added in DB
                    values.put(DB_entry.COLUMN_NAME_ID, (String) map.get("id"));
                    values.put(DB_entry.COLUMN_NAME_ARCHIVO, (String) map.get("archivo"));
                    values.put(DB_entry.COLUMN_NAME_TITULO, (String) map.get("titulo"));
                    values.put(DB_entry.COLUMN_NAME_ARTISTA, (String) map.get("artista"));
                    values.put(DB_entry.COLUMN_NAME_ALBUM, (String) map.get("album"));
                    values.put(DB_entry.COLUMN_NAME_DURACION, (String) map.get("duracion"));
                    values.put(DB_entry.COLUMN_NAME_DOWNLOADED, ""+file.exists());
                    //Adding data into DB
                    db.insert(DB_entry.TABLE_CANCIONES, "null", values);
                }
                dbHelper.actualizarAcceso(db, "canciones", System.currentTimeMillis());
                db.close();
                Log.d("MainActivity", String.format("Saved to DB in %dms", System.currentTimeMillis() - time));

                //Update with some changes and OK
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        sis();
                        if(mPullToRefreshAttacher.isRefreshing())
                            mPullToRefreshAttacher.setRefreshComplete();
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
                Log.i("MainActivity", "Excepción encontrada: " + e.toString());
            }
            return null;
        }
    }

    //SearchBar methods
    @Override
    public boolean onQueryTextSubmit(String query) {
        DB dbs = new DB(this);
        SQLiteDatabase db = dbs.getReadableDatabase();
        Cursor c = dbs.get(db, query);
        contactList.clear();
        c.moveToFirst();
        if(c.getCount() > 0) {
            do {
                // creating new HashMap
                LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();

                long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ID));
                String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_TITULO));
                String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARTISTA));
                String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ALBUM));
                String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARCHIVO));
                String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DURACION));
                String down = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DOWNLOADED));
                boolean downloaded = down.equalsIgnoreCase("true");

                // adding each child node to HashMap key => value
                map.put("id", "" + id);
                map.put("titulo", titulo);
                map.put("artista", artista);
                map.put("album", album);
                map.put("archivo", archivo);
                map.put("duracion", duracion);
                map.put("downloaded", downloaded ? "{fa-mobile}" : "{fa-cloud}"); //TODO

                contactList.add(map);
            } while(c.moveToNext());
        }
        sis();
        c.close();
        db.close();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.length() == 0 && oldText.length() > 0) {
            SQLiteDatabase db = new DB(MainActivity.this).getReadableDatabase();
            cursordb(db);
            db.close();
        } else {
            onQueryTextSubmit(newText);
        }
        oldText = newText;
        return false;
    }

    /** Si tiene que hacer la descarga */
    private void async() {
        if(Build.VERSION.SDK_INT > 10)
            mPullToRefreshAttacher.setRefreshing(true); //FIXME In 2.3.x android versions this crashes the app
        new JSONParseDialog().execute();
    }

    /** Si solo carga desde la base de datos */
    private void cursordb(SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DB_entry.COLUMN_NAME_ID,
                DB_entry.COLUMN_NAME_TITULO,
                DB_entry.COLUMN_NAME_ARTISTA,
                DB_entry.COLUMN_NAME_ALBUM,
                DB_entry.COLUMN_NAME_DURACION,
                DB_entry.COLUMN_NAME_ARCHIVO,
                DB_entry.COLUMN_NAME_DOWNLOADED
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DB_entry.COLUMN_NAME_ID + " ASC";

        Cursor c = db.query(
                DB_entry.TABLE_CANCIONES, // The table to query
                projection,               // The columns to return
                null,                     // The columns for the WHERE clause
                null,                     // The values for the WHERE clause
                null,                     // don't group the rows
                null,                     // don't filter by row groups
                sortOrder                 // The sort order
        );
        if(contactList != null)
            contactList.clear();
        else
            contactList = new ArrayList<LinkedTreeMap<String, String>>();

        c.moveToFirst();
        try {
            do {
                // creating new HashMap
                LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();

                long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ID));
                String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_TITULO));
                String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARTISTA));
                String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ALBUM));
                String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARCHIVO));
                String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DURACION));
                String downloaded = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DOWNLOADED));

                // adding each child node to HashMap key => value
                map.put("id", "" + id);
                map.put("titulo", titulo);
                map.put("artista", artista);
                map.put("album", album);
                map.put("archivo", archivo);
                map.put("duracion", duracion);
                map.put("downloaded", downloaded.equalsIgnoreCase("true") ? "{fa-mobile}" : "{fa-cloud}"); //TODO

                contactList.add(map);
            } while(c.moveToNext());
        } catch (CursorIndexOutOfBoundsException e) {
            db.execSQL(DB_entry.DELETE_CANCIONES);
            Log.e("DB", "Mala integridad de la BD");
        }
        c.close();
        sis();
    }

    /* (non-Javadoc)
     * @see uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener#onRefreshStarted(android.view.View)
     */
    @Override
    public void onRefreshStarted(View view) {
        if(Utils.HostTest(MainActivity.url) == 200) {
            //Revisa la base de datos
            DB mDbHelper = new DB(getBaseContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            if(!mDbHelper.ifTableExists(db, "canciones") || !mDbHelper.ifTableExists(db, "acceso")) {
                db.execSQL(DB_entry.CREATE_ACCESO);
                ContentValues values = new ContentValues();
                values.put("tabla", "canciones");
                values.put("fecha", System.currentTimeMillis());
            }
            db.execSQL(DB_entry.DELETE_CANCIONES);

            db.close();
            async();
        } else {
            mPullToRefreshAttacher.setRefreshComplete();
            Toast.makeText(MainActivity.this, "No se ha podido conectar con el servidor", Toast.LENGTH_LONG).show();
        }
    }
}
