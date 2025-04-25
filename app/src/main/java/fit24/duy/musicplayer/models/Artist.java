package fit24.duy.musicplayer.models;

public class Artist {
    private Long id;
    private String name;
    private String profileImage; // URL
    private String bio;

    public Artist(String name, String profileImage, String bio) {
        this.name = name;
        this.profileImage = profileImage;
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getBio() {
        return bio;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
