package fit24.duy.musicplayer.models;

public class Album {
    private Long id;
    private String title;
    private String coverImage; // URL
    private String releaseDate;
    private Artist artist;

    public Album(String title, String coverImage, String releaseDate, Artist artist) {
        this.title = title;
        this.coverImage = coverImage;
        this.releaseDate = releaseDate;
        this.artist = artist;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
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

    public String getCoverImage() {
        return coverImage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
