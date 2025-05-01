package fit24.duy.musicplayer.repository;

import fit24.duy.musicplayer.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByArtist_NameIgnoreCase(String artistName);
    List<Song> findByTitleContainingIgnoreCase(String title);
    List<Song> findByArtistId(Long artistId);
    List<Song> findByAlbumId(Long albumId);
    List<Song> findByMediaTypeId(Long mediaTypeId);

    @Query("SELECT s FROM Song s ORDER BY s.id DESC LIMIT 5")
    List<Song> findTop5ByOrderByIdDesc();

    @Query("SELECT s FROM Song s ORDER BY s.playCount DESC LIMIT 5")
    List<Song> findTop5ByOrderByPlayCountDesc();
}
