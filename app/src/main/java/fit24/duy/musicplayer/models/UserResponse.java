package fit24.duy.musicplayer.models;

public class UserResponse {
    private long id;
    private String username;
    private String email;
    private String token;

    public long getId() {
        return id;
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