package fit24.duy.musicplayer.dto;

import fit24.duy.musicplayer.entity.Artist;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AlbumResponse {
    private Long id;
    private String title;
    private String coverImage;
    private LocalDate releaseDate;
    private Artist artist;
}
