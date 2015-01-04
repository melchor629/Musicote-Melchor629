package com.melchor629.musicote;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.gson.internal.LinkedTreeMap;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import java.io.File;
import java.util.ArrayList;

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
public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener, DatabaseLoader.DatabaseLoaderListener {

    public static String HOST, BASE_API_URL = ":8000/json", BASE_URL = "/musica";
    public static Context appContext;

    private String oldText = "";
    private SwipeRefreshLayout swipeRefreshLayout;

    static ArrayList<LinkedTreeMap<String, String>> songList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Set the user interface layout for this Activity
        // The layout file is defined in the project res/layout/main.xml file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        appContext = getApplicationContext();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark, R.color.colorGrey600);
        final ListView list = (ListView) findViewById(android.R.id.list);
        list.setOnScrollListener(new ListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if(list != null && list.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = list.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = list.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });
        getActionBar().setIcon(R.drawable.ic_launcher);
        getActionBar().setDisplayUseLogoEnabled(true);

        // La app prueba en busca de la dirección correcta
        WifiManager mw = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = mw.getConnectionInfo();
        String SSID = wi.getSSID();
        Log.i("MainActivity", "Wifi conectado: " + SSID + " " + (SSID != null ? SSID.equals("Madrigal") : ""));
        if(SSID == null) {
            SSID = "";
            Toast.makeText(this, "No está usando WIFI, se recomienda utilizar la app con WIFI", Toast.LENGTH_LONG).show();
        }
        if(SSID.equals("Madrigal") || SSID.contains("Madrigal") || System.getProperty("os.version").equals("3.4.67+")) {
            MainActivity.HOST = "192.168.1.133";
        } else {
            MainActivity.HOST = "reinoslokos.no-ip.org";
        }
        Log.i("MainActivity", "HOST: " + HOST);

        //Deletes the notification if remains (BUG)
        NotificationManager mn = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Reproductor.a == -1)
            mn.cancel(1);
        mn.cancel(3);

        //Revisa la base de datos
        DB mDbHelper = new DB(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //Actualización de la lista
        if(mDbHelper.isNecesaryUpgrade(db) && Utils.HostTest(HOST) == 200)
            async();
        else
            cursordb(db);

        db.close();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void sis() {
        if(songList == null) {
            Log.e("MainActivity", "songList viene vacío...");
            return;
        }
        /**
         * Updating parsed JSON data into ListView
         * */
        ListAdapter adapter = new SimpleAdapter(this, songList,
                R.layout.list_item,
                new String[] { "titulo",  "artista",  "album",     "downloaded" },
                new int[] {    R.id.name, R.id.email, R.id.mobile, R.id.mainStatusSong });

        setListAdapter(adapter);

        // selecting single ListView item
        ListView lv = getListView();
        lv.setFastScrollEnabled(true);

        // Launching new screen on Selecting Single ListItem
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                LinkedTreeMap<String, String> datos = songList.get(position);

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                in.putExtra("obj", datos);

                startActivity(in);
            }
        });

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View v, final int which, long id) {
                final boolean isDownloaded = Utils.isDownloaded(songList.get(which).get("archivo"));
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(((TextView) v.findViewById(R.id.name)).getText().toString())
                    .setItems(isDownloaded ? R.array.song_options_array2 : R.array.song_options_array, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which2) {
                            LinkedTreeMap<String, String> obj = songList.get(which);
                            String url = obj.get("archivo");
                            if(which2 == 0) {
                                if(Reproductor.a == -1) {
                                    PlaylistManager.self.startPlaying(obj.get("titulo"),
                                        obj.get("artista"), obj.get("album"), url);
                                } else {
                                    PlaylistManager.self.stopPlaying();
                                    PlaylistManager.self.addSong(obj.get("titulo"),
                                            obj.get("artista"), obj.get("album"), url);
                                }
                            } else if(which2 == 1) {
                                if(!isDownloaded) {
                                    Intent inte = new Intent(getApplicationContext(), DownloadManager.class);
                                    inte.putExtra("file", url);
                                    inte.putExtra("id", which);
                                    startService(inte);
                                } else {
                                    if(new File(Utils.getUrl(url)).delete()) {
                                        Utils.setFileAsDownloaded(which, false);
                                        Toast.makeText(MainActivity.this, getString(R.string.done_delete), Toast.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(MainActivity.this, getString(R.string.err_delete), Toast.LENGTH_LONG).show();
                                }
                            } else if(which2 == 2) {
                                PlaylistManager.self.addSong(obj.get("titulo"),
                                        obj.get("artista"), obj.get("album"), url);
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
        SearchView searchView = new SearchView(getActionBar().getThemedContext());
        searchView.setQueryHint(getResources().getString(R.string.menu_search));
        searchView.setOnQueryTextListener(this);

        menu.add("Search")
                .setIcon(new IconDrawable(this, Iconify.IconValue.fa_search)
                        .color(Color.WHITE)
                        .actionBarSize())//android.R.drawable.ic_menu_search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        getMenuInflater().inflate(R.menu.main, menu);

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

    @Override
    public void onLoaded() {
        DB mDbHelper = new DB(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        cursordb(db);
        db.close();
        swipeRefreshLayout.setRefreshing(false);
    }

    //SearchBar methods
    @Override
    public boolean onQueryTextSubmit(String query) {
        DB dbs = new DB(this);
        SQLiteDatabase db = dbs.getReadableDatabase();
        Cursor c = dbs.get(db, query);
        songList.clear();
        c.moveToFirst();
        if(c.getCount() > 0) {
            do {
                // creating new HashMap
                LinkedTreeMap<String, String> map = new LinkedTreeMap<>();

                long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ID));
                String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_TITULO));
                String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARTISTA));
                String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ALBUM));
                String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARCHIVO));
                String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DURACION));
                String down = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DOWNLOADED));
                boolean downloaded = down.equalsIgnoreCase("true");

                // adding each child node to HashMap key => value
                map.put("id", "" + id);
                map.put("titulo", titulo);
                map.put("artista", artista);
                map.put("album", album);
                map.put("archivo", archivo);
                map.put("duracion", duracion);
                map.put("downloaded", downloaded ? "{fa-mobile}" : "{fa-cloud}"); //TODO

                songList.add(map);
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

    /** Carga los datos desde el servidor */
    private void async() {
        swipeRefreshLayout.setRefreshing(true);
        new DatabaseLoader().setListener(this).execute();
    }

    /** Carga desde la base de datos */
    private void cursordb(SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DB_entry.COLUMN_CANCIONES_ID,
                DB_entry.COLUMN_CANCIONES_TITULO,
                DB_entry.COLUMN_CANCIONES_ARTISTA,
                DB_entry.COLUMN_CANCIONES_ALBUM,
                DB_entry.COLUMN_CANCIONES_DURACION,
                DB_entry.COLUMN_CANCIONES_ARCHIVO,
                DB_entry.COLUMN_CANCIONES_DOWNLOADED
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DB_entry.COLUMN_CANCIONES_ID + " ASC";

        Cursor c = db.query(
                DB_entry.TABLE_CANCIONES, // The table to query
                projection,               // The columns to return
                null,                     // The columns for the WHERE clause
                null,                     // The values for the WHERE clause
                null,                     // don't group the rows
                null,                     // don't filter by row groups
                sortOrder                 // The sort order
        );
        if(songList != null)
            songList.clear();
        else
            songList = new ArrayList<>();

        c.moveToFirst();
        try {
            do {
                // creating new HashMap
                LinkedTreeMap<String, String> map = new LinkedTreeMap<>();

                long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ID));
                String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_TITULO));
                String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARTISTA));
                String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ALBUM));
                String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_ARCHIVO));
                String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DURACION));
                String downloaded = "false";//c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_CANCIONES_DOWNLOADED));

                // adding each child node to HashMap key => value
                map.put("id", "" + id);
                map.put("titulo", titulo);
                map.put("artista", artista);
                map.put("album", album);
                map.put("archivo", archivo);
                map.put("duracion", duracion);
                map.put("downloaded", downloaded.equalsIgnoreCase("true") ? "{fa-mobile}" : "{fa-cloud}"); //TODO

                songList.add(map);
            } while(c.moveToNext());
        } catch (CursorIndexOutOfBoundsException e) {
            db.execSQL(DB_entry.DELETE_CANCIONES);
            Log.e("DB", "Mala integridad de la BD");
        }
        c.close();
        sis();
    }

    /**
     * Ejecutado cuando el usuario completa la acción de actualizar
     */
    @Override
    public void onRefresh() {
        if(Utils.HostTest(MainActivity.HOST) == 200) {
            //Revisa la base de datos
            async();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this, "No se ha podido conectar con el servidor", Toast.LENGTH_LONG).show();
        }
    }
}
