package fit24.duy.musicplayer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String artist;

    private String album;

    private String duration;

    private String coverArt;

    private String filePath;

    @ManyToMany(mappedBy = "songs")
    private List<Playlist> playlists;
} 