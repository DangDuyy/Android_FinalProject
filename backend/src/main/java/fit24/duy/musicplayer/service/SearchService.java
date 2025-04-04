package fit24.duy.musicplayer.service;

import fit24.duy.musicplayer.dto.SearchResponse;
import fit24.duy.musicplayer.entity.Album;
import fit24.duy.musicplayer.entity.Artist;
import fit24.duy.musicplayer.entity.Song;
import fit24.duy.musicplayer.repository.AlbumRepository;
import fit24.duy.musicplayer.repository.ArtistRepository;
import fit24.duy.musicplayer.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    private final SongRepository songRepo;
    private final ArtistRepository artistRepo;
    private final AlbumRepository albumRepo;

    public SearchService(SongRepository songRepo, ArtistRepository artistRepo, AlbumRepository albumRepo) {
        this.songRepo = songRepo;
        this.artistRepo = artistRepo;
        this.albumRepo = albumRepo;
    }

    public SearchResponse search(String q) {
        List<Song> songs = songRepo.findByTitleContainingIgnoreCase(q);
        List<Artist> artists = artistRepo.findByNameContainingIgnoreCase(q);
        List<Album> albums = albumRepo.findByTitleContainingIgnoreCase(q);
        return new SearchResponse(songs, artists, albums);
    }
}
