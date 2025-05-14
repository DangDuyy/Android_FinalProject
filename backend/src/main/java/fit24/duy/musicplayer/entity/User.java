package fit24.duy.musicplayer.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String profileImage;

    @Column(nullable = false)
    private int status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Playlist> playlists = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinTable(
            name = "user_liked_contents",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "content_id")
    )
    private Set<Content> likedContents = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinTable(
            name = "user_followed_artists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> followedArtists = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinTable(
            name = "user_add_albums",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    private Set<Album> addedAlbums = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<PlayHistory> playHistory = new ArrayList<>();

    public User() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}