package fit24.duy.musicplayer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaTypeResponse {
    private Long id;
    private String title;
    private String coverImage;
    private String filePath;
    private Long playCount;

    public MediaTypeResponse(Long id, String title, String coverImage, String filePath, Long playCount) {
        this.id = id;
        this.title = title;
        this.coverImage = coverImage;
        this.filePath = filePath;
        this.playCount = playCount;
    }

}