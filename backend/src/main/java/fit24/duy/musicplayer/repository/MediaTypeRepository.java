package fit24.duy.musicplayer.repository;

import fit24.duy.musicplayer.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaTypeRepository extends JpaRepository<MediaType, Long> {

}
