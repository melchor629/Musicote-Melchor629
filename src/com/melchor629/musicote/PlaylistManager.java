package com.melchor629.musicote;

import java.util.ArrayList;

/**
 * Manages platlist
 * @author melchor9000
 */
public class PlaylistManager {
    public static PlaylistManager self;
    private ArrayList<Song> playlist;
    
    private PlaylistManager() {
        playlist = new ArrayList<Song>();
    }

    public void addSong(String titulo, String artista, String urle, String album) {
        Song song = new Song(titulo, artista, album, urle);
        playlist.add(song);
    }

    public void deleteSong(int i) {
        playlist.remove(i);
    }
    
    public boolean isNextSong() {
        return playlist.size() > 0;
    }

    public class Song {
        public String title;
        public String artist;
        public String album;
        public String url;

        public Song(String title, String artist, String album, String url) {
            super();
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.url = url;
        }
        
        
    }
}
