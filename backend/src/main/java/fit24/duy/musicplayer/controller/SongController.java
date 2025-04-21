package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.dto.SongResponse;
import fit24.duy.musicplayer.entity.Album;
import fit24.duy.musicplayer.entity.Song;
import fit24.duy.musicplayer.repository.AlbumRepository;
import fit24.duy.musicplayer.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AlbumRepository albumRepository;

    // Lấy bài hát theo tên nghệ sĩ
    @GetMapping("/artist")
    public List<SongResponse> getSongsByArtistName(@RequestParam("name") String artistName) {
        List<Song> songs = songRepository.findByArtist_NameIgnoreCase(artistName);

        return songs.stream().map(song -> {
            SongResponse dto = new SongResponse();
            dto.setId(song.getId());
            dto.setTitle(song.getTitle());
            dto.setCoverImage(song.getCoverImage());
            dto.setArtist(song.getArtist());
            return dto;
        }).collect(Collectors.toList());
    }

    // Lấy bài hát theo tên album
    @GetMapping("/album")
    public List<SongResponse> getSongsByAlbumTitle(@RequestParam("title") String albumTitle) {
        List<Album> albums = albumRepository.findByTitleIgnoreCase(albumTitle);

        if (albums.isEmpty()) {
            return List.of();
        }

        Album album = albums.get(0);
        List<Song> songs = album.getSongs();

        return songs.stream().map(song -> {
            SongResponse dto = new SongResponse();
            dto.setId(song.getId());
            dto.setTitle(song.getTitle());
            dto.setCoverImage(song.getCoverImage());
            dto.setArtist(song.getArtist());
            return dto;
        }).collect(Collectors.toList());
    }
}
