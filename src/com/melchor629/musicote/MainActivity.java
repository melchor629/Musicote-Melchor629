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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DB_entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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

    public static String Last_String = "";
    public static volatile int response = 0;
    public static String url;
    public static Context appContext;

    private NotificationManager nm;
    private Toast tostado;
    private String oldText = "";
    private PullToRefreshLayout mPullToRefreshAttacher;

    // contacts JSONArray
    private static JSONArray contacts = null;
    private ArrayList<HashMap<String, String>> contactList;

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

        // La app prueba en busca de la dirección correcta
        WifiManager mw = (WifiManager)getSystemService(Context.WIFI_SERVICE);
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

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Deletes the notification if remains (BUG)
        NotificationManager mn = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Reproductor.a == -1)
            mn.cancel(1);
        mn.cancel(3);

        //Revisa la base de datos
        DB mDbHelper = new DB(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if(mDbHelper.ifTableExists(db, "canciones") == false || mDbHelper.ifTableExists(db, "acceso") == false) {
            db.execSQL(DB_entry.CREATE_ACCESO);
            db.execSQL(DB_entry.CREATE_CANCIONES);
            ContentValues values = new ContentValues();
            values.put("tabla", "canciones");
            values.put("fecha", System.currentTimeMillis());
            Log.e("newDB", "Dado " + db.insert("acceso", "null", values));
        }

        //Actualización de la lista
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        cursordb(db);
        if(mDbHelper.isNecesaryUpgrade(db, pref) && ParseJSON.HostTest(url) == 200)
            db.execSQL(DB_entry.DELETE_CANCIONES);

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
                new String[] {"titulo", "artista", "album"}, new int[] {
                R.id.name, R.id.email, R.id.mobile});

        try {
            setListAdapter(adapter);
        } catch (Exception e) {
            Log.e("ServerHostDetector", "Er ordenata de mershor ta apagao... " + e.toString());
            tostado = Toast.makeText(MainActivity.this, "Ningún servidor activo...", Toast.LENGTH_LONG);
            tostado.show();
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
                HashMap<String, String> datos = null;
                try {
                    datos = contactList.get(position);
                } catch (Exception e) {
                    Log.e("com.melchor629.musicote", "" + e.toString());
                    e.printStackTrace();
                }
                String name = getString(R.string.vacio);
                String cost = getString(R.string.vacio);
                String description = getString(R.string.vacio);
                String album = "-00:00";
                String archivo = "http://" + url + "/musica";
                try {
                    name = ((TextView)view.findViewById(R.id.name)).getText().toString();
                    cost = ((TextView)view.findViewById(R.id.email)).getText().toString();
                    description = ((TextView)view.findViewById(R.id.mobile)).getText().toString();
                    album = datos.get("duracion");
                    archivo = datos.get("archivo");
                } catch (Exception e) {
                    Log.e("MainActivity", e.toString());
                }

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                in.putExtra("titulo", name);
                in.putExtra("artista", cost);
                in.putExtra("album", description);
                in.putExtra("duracion", album);
                in.putExtra("archivo", archivo);

                startActivity(in);
            }
        });

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View v, final int which, long id) {
                final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        contactList.get(which).get("archivo").substring(contactList.get(which).get("archivo").lastIndexOf("/")+1));
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(((TextView) v.findViewById(R.id.name)).getText().toString())
                    .setItems(file.exists() ? R.array.song_options_array2 : R.array.song_options_array, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which2) {
                            if(which2 == 0) {
                                if(Reproductor.a == -1) {
                                    String url = contactList.get(which).get("archivo");
                                    if(file.exists()) url = file.getAbsolutePath();
                                    PlaylistManager.self.startPlaying(((TextView) v.findViewById(R.id.name)).getText().toString(), 
                                        ((TextView) v.findViewById(R.id.email)).getText().toString(),
                                        ((TextView) v.findViewById(R.id.mobile)).getText().toString(), url);
                                } else {
                                    String url = contactList.get(which).get("archivo");
                                    if(file.exists()) url = file.getAbsolutePath();
                                    PlaylistManager.self.stopPlaying();
                                    PlaylistManager.self.addSong(((TextView) v.findViewById(R.id.name)).getText().toString(),
                                            ((TextView) v.findViewById(R.id.email)).getText().toString(),
                                            ((TextView) v.findViewById(R.id.mobile)).getText().toString(), url);
                                }
                            } else if(which2 == 1) {
                                if(file.exists()) {
                                    
                                } else {
                                    
                                }
                            } else if(which2 == 2) {
                                String url = contactList.get(which).get("archivo");
                                if(file.exists()) url = file.getAbsolutePath();
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
    public void onPause() {
        super.onPause();
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
    private class JSONParseDialog extends AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> {
        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
            // Hashmap for ListView
            final ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();

            // Creating JSON Parser instance
            ParseJSON jParser = new ParseJSON();

            synchronized (this) {
                publishProgress(1);

                publishProgress(2);
                if(MainActivity.url != null) {
                    // getting JSON string from URL
                    response = ParseJSON.HostTest(MainActivity.url);

                    Log.d("MainActivity", "Response code: " + response);

                    publishProgress(25);
                    if(response == 200) {
                        JSONObject json = jParser.getJSONFromUrl("http://" + MainActivity.url + "/py/api.py");
                        publishProgress(30);
                        try {
                            // Getting Array of Songs
                            MainActivity.contacts = json.getJSONArray("canciones");
                            publishProgress(35);

                            // looping through All Songs
                            publishProgress(37);
                            DB dbHelper = new DB(getBaseContext());
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            if(!dbHelper.ifTableExists(db, "canciones"))
                                db.execSQL(DB_entry.CREATE_CANCIONES);
                            for(int i = 0; i < MainActivity.contacts.length(); i++) {
                                JSONObject c = MainActivity.contacts.getJSONObject(i);
                                ContentValues values = new ContentValues();

                                int counter = 40 + ((i * 100 / MainActivity.contacts.length()) / 2);
                                publishProgress(counter);
                                // Storing each json item in variable
                                int id = c.getInt("id");
                                String archivo = c.getString("archivo");
                                String titulo = c.getString("titulo");
                                String artista = c.getString("artista");
                                String album = c.getString("album");
                                String duracion = c.getString("duracion");

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                map.put("id", "" + id);
                                map.put("titulo", titulo);
                                map.put("artista", artista);
                                map.put("album", album);
                                map.put("archivo", "http://" + MainActivity.url + "" + archivo);
                                map.put("duracion", duracion);

                                //DB
                                values.put(DB_entry.COLUMN_NAME_ID, id);
                                values.put(DB_entry.COLUMN_NAME_ARCHIVO, archivo);
                                values.put(DB_entry.COLUMN_NAME_TITULO, titulo);
                                values.put(DB_entry.COLUMN_NAME_ARTISTA, artista);
                                values.put(DB_entry.COLUMN_NAME_ALBUM, album);
                                values.put(DB_entry.COLUMN_NAME_DURACION, duracion);

                                // adding HashList to ArrayList
                                contactList.add(map);

                                //Adding data into DB
                                db.insert(DB_entry.TABLE_CANCIONES, "null", values);
                            }
                            dbHelper.actualizarAcceso(db, "canciones", System.currentTimeMillis());
                            db.close();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("MainActivity", "Excepción encontrada: " + e.toString());
                        }
                        publishProgress(90);
                    }

                    publishProgress(95);
                    return contactList;
                } else {
                    return null;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            super.onPostExecute(result);
            nm.cancel(3);
        }
    }

    //SearchBar methods
    @Override
    public boolean onQueryTextSubmit(String query) {
        DB dbs = new DB(this);
        SQLiteDatabase db = dbs.getReadableDatabase();
        Cursor c = dbs.get(db, query);
        contactList = new ArrayList<HashMap<String, String>>();
        c.moveToFirst();
        if(c.getCount() > 0) {
            do {
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ID));
                String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_TITULO));
                String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARTISTA));
                String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ALBUM));
                String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARCHIVO));
                String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DURACION));

                // adding each child node to HashMap key => value
                map.put("id", "" + id);
                map.put("titulo", titulo);
                map.put("artista", artista);
                map.put("album", album);
                map.put("archivo", "http://" + url + "" + archivo);
                map.put("duracion", duracion);

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
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification
                .setSmallIcon(R.drawable.altavoz)
                .setContentTitle("Musicote")
                .setContentText("Actualizando base de datos...")
                .setProgress(100, 0, true)
                .setOngoing(true);
        nm.cancel(3);
        nm.notify(3, notification.build());
        mPullToRefreshAttacher.setRefreshing(true); //FIXME Comrpobar si va bien
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> asd = new JSONParseDialog().execute();
                    contactList = asd.get();
                } catch (InterruptedException e) {
                    Log.e("AsyncTask", "AsyncTask not finished: " + e.toString());
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("AsyncTask", "AsyncTask not finished: " + e.toString());
                    e.printStackTrace();
                }
                MainActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            sis();
                            mPullToRefreshAttacher.setRefreshComplete();
                        }
                    }
                );
                nm.cancel(3);
            }
        }).start();
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
                DB_entry.COLUMN_NAME_ARCHIVO
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DB_entry.COLUMN_NAME_ID + " ASC";

        Cursor c = db.query(
                DB_entry.TABLE_CANCIONES,                 // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        contactList = new ArrayList<HashMap<String, String>>();

        c.moveToFirst();
        try {
            do {
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ID));
                String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_TITULO));
                String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARTISTA));
                String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ALBUM));
                String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARCHIVO));
                String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DURACION));

                // adding each child node to HashMap key => value
                map.put("id", "" + id);
                map.put("titulo", titulo);
                map.put("artista", artista);
                map.put("album", album);
                map.put("archivo", "http://" + MainActivity.url + "" + archivo);
                map.put("duracion", duracion);

                contactList.add(map);
            } while(c.moveToNext());
        } catch (CursorIndexOutOfBoundsException e) {
            db.execSQL(DB_entry.DELETE_CANCIONES);
            Log.e("DB", "Mala integridad de la BD");
            try {
                this.finalize();
            } catch (Throwable e1) {
                Log.e("error", "Error: " + e1.toString());
            }
        }
        c.close();
        sis();
    }

    /* (non-Javadoc)
     * @see uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener#onRefreshStarted(android.view.View)
     */
    @Override
    public void onRefreshStarted(View view) {
        if(ParseJSON.HostTest(MainActivity.url) == 200) {
            //Revisa la base de datos
            DB mDbHelper = new DB(getBaseContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            if(mDbHelper.ifTableExists(db, "canciones") == false || mDbHelper.ifTableExists(db, "acceso") == false) {
                db.execSQL(DB_entry.CREATE_ACCESO);
                ContentValues values = new ContentValues();
                values.put("tabla", "canciones");
                values.put("fecha", System.currentTimeMillis());
                Log.e("newDB", "Dado " + db.insert("acceso", "null", values));
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
