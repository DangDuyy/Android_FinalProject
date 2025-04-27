package fit24.duy.musicplayer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class MediaTypeResponse {
    private Long id;
    private String title;
    private String coverImage;
    private String filePath;
    private Duration duration;
    private Long playCount;

    public MediaTypeResponse(Long id, String title, String coverImage, String filePath, Duration duration, Long playCount) {
        this.id = id;
        this.title = title;
        this.coverImage = coverImage;
        this.filePath = filePath;
        this.duration = duration;
        this.playCount = playCount;
    }
}