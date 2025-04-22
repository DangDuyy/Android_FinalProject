package fit24.duy.musicplayer.repository;

import fit24.duy.musicplayer.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    List<PlayHistory> findTop5ByUserOrderByPlayedAtDesc(fit24.duy.musicplayer.entity.User user);

}
