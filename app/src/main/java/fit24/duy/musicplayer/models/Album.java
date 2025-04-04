package fit24.duy.musicplayer.models;

public class Album {
    private String title;
    private String coverImage; // URL
    private String releaseDate;

    public Album(String title, String coverImage, String releaseDate) {
        this.title = title;
        this.coverImage = coverImage;
        this.releaseDate = releaseDate;
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
