package fit24.duy.musicplayer.service.impl;

import fit24.duy.musicplayer.entity.PlayHistory;
import fit24.duy.musicplayer.entity.Song;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.repository.PlayHistoryRepository;
import fit24.duy.musicplayer.repository.SongRepository;
import fit24.duy.musicplayer.repository.UserRepository;
import fit24.duy.musicplayer.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SongServiceImpl implements SongService {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlayHistoryRepository playHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Song> getRecentlyPlayedSongs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<PlayHistory> recentHistory = playHistoryRepository.findTop5ByUserOrderByPlayedAtDesc(user);
                return recentHistory.stream()
                        .map(PlayHistory::getContent)
                        .filter(Song.class::isInstance) // More type-safe filtering
                        .map(Song.class::cast)         // Safe casting after filtering
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    @Override
    public List<Song> getRecommendedSongs() {
        // TODO: Implement recommendation logic
        return songRepository.findAll().subList(0, Math.min(5, songRepository.findAll().size()));
    }

    @Override
    public Song getSongById(Long id) {
        Optional<Song> song = songRepository.findById(id);
        return song.orElse(null);
    }

    @Override
    public List<Song> searchSongsByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public Song saveSong(Song song) {
        return songRepository.save(song);
    }

    @Override
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    @Override
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @Override
    public Song createSong(Song song) {
        return songRepository.save(song);
    }

    @Override
    public Song updateSong(Song song) {
        if (songRepository.existsById(song.getId())) {
            return songRepository.save(song);
        }
        return null;
    }

    @Override
    public List<Song> getSongsByArtist(Long artistId) {
        return songRepository.findByArtistId(artistId);
    }

    @Override
    public List<Song> getSongsByAlbum(Long albumId) {
        return songRepository.findByAlbumId(albumId);
    }
}