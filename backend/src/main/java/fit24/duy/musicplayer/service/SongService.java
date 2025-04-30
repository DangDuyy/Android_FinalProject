package fit24.duy.musicplayer.service;

import fit24.duy.musicplayer.entity.Song;
import java.util.List;

public interface SongService {
    // Các phương thức cũ
    List<Song> getRecentlyPlayedSongs();
    List<Song> getRecommendedSongs();
    Song getSongById(Long id);
    List<Song> searchSongsByTitle(String title);
    Song saveSong(Song song);
    void deleteSong(Long id);
    
    // Các phương thức mới
    List<Song> getAllSongs();
    Song createSong(Song song);
    Song updateSong(Song song);
    List<Song> getSongsByArtist(Long artistId);
    List<Song> getSongsByAlbum(Long albumId);
}
