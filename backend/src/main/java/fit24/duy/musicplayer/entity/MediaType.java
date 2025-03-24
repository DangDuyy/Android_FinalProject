package fit24.duy.musicplayer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "media_types")
public class MediaType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // SONG, PODCAST, AUDIOBOOK, STORY, etc.

    private String description;

    private String icon;

    @OneToMany(mappedBy = "mediaType", cascade = CascadeType.ALL)
    private List<Content> contents = new ArrayList<>();
} 