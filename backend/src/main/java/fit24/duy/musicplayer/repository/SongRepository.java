package fit24.duy.musicplayer.repository;

import fit24.duy.musicplayer.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByArtistId(Long artistId);
    List<Song> findByAlbumId(Long albumId);
    List<Song> findByTitleContainingIgnoreCase(String title);

    List<Song> findByArtist_NameIgnoreCase(String artistName);

    List<Song> findByMediaTypeId(Long mediaTypeId);
}
