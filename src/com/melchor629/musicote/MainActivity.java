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

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.melchor629.musicote.basededatos.DB;
import com.melchor629.musicote.basededatos.DatabaseLoader;
import com.melchor629.musicote.basededatos.SongRow;

import java.io.File;
import java.util.ArrayList;

/**
 * Actividad principal de la App, hace muchas cosas
 * @author melchor9000
 */
public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener, DatabaseLoader.DatabaseLoaderListener {

    public static String HOST, BASE_API_URL = "/musicote/json", BASE_URL = "/musica";
    public static Context appContext;

    private String oldText = "";
    private SwipeRefreshLayout swipeRefreshLayout;

    static ArrayList<SongRow> songList;

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
            Toast.makeText(this, getString(R.string.no_wifi), Toast.LENGTH_LONG).show();
        }
        if(SSID.equals("Madrigal") || SSID.contains("Madrigal") || System.getProperty("os.version").equals("3.4.67+")) {
            MainActivity.HOST = "192.168.1.133";
        } else {
            MainActivity.HOST = "95.17.216.65";//"reinoslokos.no-ip.org";
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
                SongRow datos = songList.get(position);

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                in.putExtra("obj", datos.toString());

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
                            SongRow obj = songList.get(which);
                            String url = obj.getArchivo();
                            if(which2 == 0) {
                                if(Reproductor.a == -1) {
                                    PlaylistManager.self.startPlaying(obj.getTitulo(),
                                        obj.getArtista(), obj.getAlbum(), url);
                                } else {
                                    PlaylistManager.self.stopPlaying();
                                    PlaylistManager.self.addSong(obj.getTitulo(),
                                            obj.getArtista(), obj.getAlbum(), url);
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
                                PlaylistManager.self.addSong(obj.getTitulo(),
                                        obj.getArtista(), obj.getAlbum(), url);
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
        //Create the search view
        SearchView searchView = new SearchView(getActionBar().getThemedContext());
        searchView.setQueryHint(getResources().getString(R.string.menu_search));
        searchView.setOnQueryTextListener(this);

        menu.add("Search")
                .setIcon(new IconDrawable(this, Iconify.IconValue.fa_search)
                        .color(Color.WHITE)
                        .actionBarSize())
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
                songList.add(new SongRow(c));
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
        if(songList == null)
            songList = new ArrayList<>();
        else
            songList.clear();
        songList.addAll(DatabaseLoader.getSongsMap(db));
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
            Toast.makeText(MainActivity.this, getString(R.string.err_server_conn), Toast.LENGTH_LONG).show();
        }
    }
}
