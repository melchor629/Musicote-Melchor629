package com.melchor629.musicote;

import java.util.ArrayList;

import android.content.Intent;
import android.util.Log;

/**
 * Manages playlist and the player cycle
 * @author melchor9000
 */
public class PlaylistManager {
    /* Current position of the playlist */
    public static int pos;
    public final static PlaylistManager self = new PlaylistManager();
    private final ArrayList<Song> playlist;
    private boolean adelante = true;
    
    private PlaylistManager() {
        playlist = new ArrayList<>();
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
        return playlist.size() > (pos+1);
    }

    /**
     * @return Tell if there is a previous song or not
     */
    public boolean isPreviousSong() {
        return pos > 0;
    }

    public void nextSong() {
        if(Reproductor.a == -1) return;
        adelante = true;
        Reproductor.reproductor.stop();
        Reproductor.reproductor.release();
        Reproductor.leNext();
    }

    public void previousSong() {
        if(Reproductor.a != -1) {
            adelante = false;
            Reproductor.reproductor.stop();
            Reproductor.reproductor.release();
            Reproductor.leNext();
        }
    }

    /**
     * Get the song in the position of the playlist
     * @param i Position
     * @return Song info
     */
    public Song get(int i) {
        if(playlist.size() > i)
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
            this.url = Utils.getUrl(url);
        }
    }

    public interface callback {
        void run();
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
                //if(!isNextSong()) return;
            }
        };
        Reproductor.onEnd = new callback() {
            public boolean runed = false;
            @Override
            public void run() {
                if(runed) return;
                Log.i("PlaylistManager", "onEnd called");
                Reproductor.a = -1;
                if(!isNextSong()) {
                    pos = 0;
                    playlist.clear();
                    return;
                }
                if(adelante) pos++;
                else {
                    pos--;
                    adelante = true;
                }
                runed = true;
            }
        };
    }
}
