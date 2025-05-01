package fit24.duy.musicplayer.service;

import fit24.duy.musicplayer.dto.MediaTypeResponse;
import fit24.duy.musicplayer.entity.Song;
import fit24.duy.musicplayer.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

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

    public List<MediaTypeResponse> getSongsByMediaType(Long mediaTypeId) {
        List<Song> songs = songRepository.findByMediaTypeId(mediaTypeId);
        return songs.stream()
                .map(song -> new MediaTypeResponse(
                        song.getId(),
                        song.getTitle(),
                        song.getCoverImage(),
                        song.getFilePath(),
                        song.getDuration(),
                        song.getPlayCount()
                ))
                .collect(Collectors.toList());
    }
}
