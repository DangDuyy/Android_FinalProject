package fit24.duy.musicplayer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "contents")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private Duration duration;

    private String coverImage;

    private String filePath;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @ManyToOne
    @JoinColumn(name = "media_type_id", nullable = false)
    private MediaType mediaType;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany(mappedBy = "contents")
    private List<Playlist> playlists = new ArrayList<>();

    @ManyToMany(mappedBy = "likedContents")
    private Set<User> likedByUsers = new HashSet<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayHistory> playHistory = new ArrayList<>();

    @Column(name = "play_count")
    private Long playCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // For podcasts and audiobooks
    private String series;
    private Integer episodeNumber;
    private String season;

    // For stories
    private Integer duration_seconds;
    private Boolean isExpirable;
    private LocalDateTime expiryDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (releaseDate == null) {
            releaseDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}