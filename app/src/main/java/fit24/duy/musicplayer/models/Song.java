package fit24.duy.musicplayer.models;

public class Song {
    private String title;
    private Artist artist;
    private int albumArt;
    private String coverImage;
    private String duration;
    private String filePath;
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // URL của server của bạn

    public Song(String title, Artist artist, int albumArt) {
        this.title = title;
        this.artist = artist;
        this.albumArt = albumArt;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
        // Kết hợp BASE_URL với file_path để tạo URL đầy đủ
        return BASE_URL + "uploads/" + filePath;
    }
}