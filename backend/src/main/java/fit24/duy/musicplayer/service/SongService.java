package fit24.duy.musicplayer.service;

import fit24.duy.musicplayer.entity.Song;

import java.util.List;

public interface SongService {
    List<Song> getRecentlyPlayedSongs();
    List<Song> getRecommendedSongs();
    Song getSongById(Long id);
    List<Song> searchSongsByTitle(String title);
    Song saveSong(Song song);
    void deleteSong(Long id);
}
