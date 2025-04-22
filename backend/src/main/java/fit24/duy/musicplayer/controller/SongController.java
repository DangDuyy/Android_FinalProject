package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.entity.Song;
import fit24.duy.musicplayer.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @GetMapping("/recently-played")
    public ResponseEntity<List<Song>> getRecentlyPlayedSongs() {
        List<Song> recentlyPlayed = songService.getRecentlyPlayedSongs();
        return new ResponseEntity<>(recentlyPlayed, HttpStatus.OK);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<Song>> getRecommendedSongs() {
        List<Song> recommended = songService.getRecommendedSongs();
        return new ResponseEntity<>(recommended, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        Song song = songService.getSongById(id);
        if (song != null) {
            return new ResponseEntity<>(song, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Song>> searchSongs(@RequestParam String title) {
        List<Song> searchResults = songService.searchSongsByTitle(title);
        return new ResponseEntity<>(searchResults, HttpStatus.OK);
    }

    // Add other song-related endpoints as needed
}