package fit24.duy.musicplayer.dto;

import fit24.duy.musicplayer.entity.Album;
import fit24.duy.musicplayer.entity.Artist;
import fit24.duy.musicplayer.entity.Song;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private List<Song> songs;
    private List<Artist> artists;
    private List<Album> albums;

    public SearchResponse(List<Song> songs, List<Artist> artists, List<Album> albums) {
        this.songs = songs;
        this.artists = artists;
        this.albums = albums;
    }
}
