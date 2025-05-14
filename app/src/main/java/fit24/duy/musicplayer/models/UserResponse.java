package fit24.duy.musicplayer.models;

public class UserResponse {
    private long id;
    private String username;
    private String email;
    private String token;
    private int status;  // 👈 phải có dòng này


    private String profileImage;

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getId() {
        return id;
    }


    // Getter & Setter cho status
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}