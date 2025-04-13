package fit24.duy.musicplayer.dto;

public class UserResponse {
    private long id;
    private String username;
    private String email;

    public UserResponse(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters và setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}