package fit24.duy.musicplayer.models;

import java.io.Serializable;
import android.util.Log;

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
    private String path;
    private String thumbnail;
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // URL của server của bạn
    private static final String TAG = "Song";

    public Song(long id, String title, String artist, String path) {
        this.id = id;
        this.title = title;
        this.artist = new Artist(artist, "", ""); // Tạo một Artist object với tên nghệ sĩ và các thông tin khác
        this.path = path;
        this.thumbnail = path; // Sử dụng path làm thumbnail tạm thời
    }

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

    public String getArtistName() {
        return artist != null ? artist.getName() : "";
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAudioUrl() {
        Log.d(TAG, "Getting audio URL for song: " + title);
        Log.d(TAG, "audioUrl: " + audioUrl);
        Log.d(TAG, "filePath: " + filePath);
        Log.d(TAG, "path: " + path);

        if (audioUrl != null && !audioUrl.isEmpty()) {
            Log.d(TAG, "Using audioUrl: " + audioUrl);
            return audioUrl;
        }
        // Nếu không có audioUrl, sử dụng filePath
        if (filePath != null && !filePath.isEmpty()) {
            // Nếu là URL đầy đủ
            if (filePath.toLowerCase().startsWith("http://") || filePath.toLowerCase().startsWith("https://")) {
                Log.d(TAG, "Using filePath as full URL: " + filePath);
                return filePath;
            }
            // Nếu là tên file trong uploads
            String url = BASE_URL + "uploads/" + filePath;
            Log.d(TAG, "Using filePath with BASE_URL: " + url);
            return url;
        }
        // Nếu không có cả hai, thử dùng path
        if (path != null && !path.isEmpty()) {
            if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
                Log.d(TAG, "Using path as full URL: " + path);
                return path;
            }
            String url = BASE_URL + "uploads/" + path;
            Log.d(TAG, "Using path with BASE_URL: " + url);
            return url;
        }
        Log.e(TAG, "No valid audio URL found for song: " + title);
        return null;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}