package fit24.duy.musicplayer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "play_history")
public class PlayHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    private Integer playDuration; // in seconds
    private Float playProgress; // percentage of content played

    @PrePersist
    protected void onCreate() {
        playedAt = LocalDateTime.now();
    }
} 