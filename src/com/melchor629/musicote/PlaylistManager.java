package com.melchor629.musicote;

import java.util.ArrayList;

import android.content.Intent;
import android.util.Log;

/**
 * Manages playlist and the player cicle
 * @author melchor9000
 */
public class PlaylistManager {
    public final static PlaylistManager self = new PlaylistManager();
    private ArrayList<Song> playlist;
    
    private PlaylistManager() {
        playlist = new ArrayList<Song>();
    }

    /**
     * Add a song to the playlist
     * @param titulo Title of the song
     * @param artista Artist of the song
     * @param album Album of the song
     * @param urle URL to the file
     */
    public void addSong(String titulo, String artista, String album, String urle) {
        Song song = new Song(titulo, artista, album, urle);
        playlist.add(song);
    }

    /**
     * Delete a song of the playlist with the given Position
     * @param i Position in the playlist [0,...]
     */
    public void deleteSong(int i) {
        playlist.remove(i);
    }

    /**
     * @return Tell if there is another song or not
     */
    public boolean isNextSong() {
        return playlist.size() > 0;
    }

    /**
     * Get the song in the position of the playlist
     * @param i Position
     * @return Song info
     */
    public Song get(int i) {
        if(isNextSong())
            return playlist.get(i);
        return null;
    }

    /**
     * Start playing with this song. Don't use it if is something playing,
     * firstly use {@link #stopPlaying()}.
     * @param titulo Title of the song
     * @param artista Artist of the song
     * @param album Album of the song
     * @param urle URL to the file
     */
    public void startPlaying(String titulo, String artista, String album, String urle) {
        playlist.clear();
        addSong(titulo, artista, album, urle);
        intent(true);
    }

    /**
     * Stops playing
     */
    public void stopPlaying() {
        MainActivity.appContext.stopService(new Intent(MainActivity.appContext, Reproductor.class));
        playlist.clear();
    }

    /**
     * Class that describes needed information about a song
     * @author melchor9000
     */
    public class Song {
        public final String title;
        public final String artist;
        public final String album;
        public final String url;

        public Song(String title, String artist, String album, String url) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.url = url;
        }
    }

    public interface callback {
        public void run();
    }

    ArrayList<Song> getPlaylist() {
        return playlist;
    }
    
    private void intent(boolean b1) {
        Intent intent = new Intent(MainActivity.appContext, Reproductor.class);
        intent.putExtra("autostart", b1);
        MainActivity.appContext.startService(intent);
        callbacks();
    }

    void callbacks() {
        Reproductor.beforeEnd = new callback() {
            @Override
            public void run() {
                Log.i("PlaylistManager", "beforeEnd called");
                if(!isNextSong()) return;
                //intent(false);
            }
        };
        Reproductor.onEnd = new callback() {
            public boolean runned = false;
            @Override
            public void run() {
                if(runned) return;
                Log.i("PlaylistManager", "onEnd called");
                deleteSong(0);
                Reproductor.a = -1;
                if(!isNextSong()) return;
                //Reproductor.start();
                runned = true;
            }
        };
    }
}
