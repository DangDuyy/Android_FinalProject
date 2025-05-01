package fit24.duy.musicplayer.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String coverImage;
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    @JsonManagedReference
    private Artist artist;

    @ManyToOne
    @JoinColumn(name = "album_id")
    @JsonBackReference
    private Album album;

    @ManyToOne
    @JoinColumn(name = "media_type_id", nullable = false)
    @JsonBackReference
    private MediaType mediaType;

    @ManyToMany
    @JoinTable(
            name = "user_liked_songs",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();

    @Column(name = "play_count", nullable = false)
    private Long playCount = 0L;
}
