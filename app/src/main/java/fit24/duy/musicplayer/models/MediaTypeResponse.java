package fit24.duy.musicplayer.models;

import com.google.gson.annotations.SerializedName;

public class MediaTypeResponse {
    private Long id;
    private String title;
    private String coverImage;
    private String filePath;
    private String duration;
    private Integer playCount;

    // Constructor
    public MediaTypeResponse(Long id, String title, String coverImage, String filePath, String duration, Integer playCount) {
        this.id = id;
        this.title = title;
        this.coverImage = coverImage;
        this.filePath = filePath;
        this.duration = duration;
        this.playCount = playCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }
}