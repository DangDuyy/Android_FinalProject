package fit24.duy.musicplayer.models;

import java.io.Serializable;

public class Song implements Serializable {
    private Long id;
    private String title;
    private String coverImage;
    private String audioUrl;
    private Artist artist;
    private Album album;
    private MediaType mediaType;
    private int duration;
    private int playCount;
    private String lyrics;
    private int albumArt;
    private String filePath;
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // URL của server của bạn

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

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public int getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(int albumArt) {
        this.albumArt = albumArt;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAudioUrl() {
        if (audioUrl != null && !audioUrl.isEmpty()) {
            return audioUrl;
        }
        // Nếu không có audioUrl, sử dụng filePath
        return filePath != null ? BASE_URL + "uploads/" + filePath : null;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
}