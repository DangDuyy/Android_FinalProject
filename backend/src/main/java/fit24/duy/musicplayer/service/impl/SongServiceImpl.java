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
        // Implementation for recommended songs would go here
        // This could involve analyzing user's listening history, liked songs, etc.
        // For now, let's return a placeholder
        return songRepository.findAll().stream().limit(7).collect(Collectors.toList());
    }

    @Override
    public Song getSongById(Long id) {
        return songRepository.findById(id).orElse(null);
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
}