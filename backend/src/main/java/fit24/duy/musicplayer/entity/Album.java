package fit24.duy.musicplayer.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String coverImage;

    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    @JsonBackReference
    private Artist artist;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Song> songs = new ArrayList<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Content> contents = new ArrayList<>();
}
