package fit24.duy.musicplayer.dto;

import fit24.duy.musicplayer.entity.Artist;
import lombok.Data;

@Data
public class SongResponse {
    private Long id;
    private String title;
    private String coverImage;
    private Artist artist;
}
