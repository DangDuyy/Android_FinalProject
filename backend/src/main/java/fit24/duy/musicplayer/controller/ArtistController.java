package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.entity.Artist;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.model.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    @PersistenceContext
    private EntityManager entityManager;

    // API để follow artist
    @PostMapping("/{artistId}/follow")
    @Transactional
    public ResponseEntity<Response<String>> followArtist(@PathVariable Long artistId, @RequestParam Long userId) {
        User user = entityManager.find(User.class, userId);
        Artist artist = entityManager.find(Artist.class, artistId);

        if (user == null || artist == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "User or Artist not found", null)
            );
        }

        user.getFollowedArtists().add(artist);
        entityManager.merge(user);

        return ResponseEntity.ok().body(
                new Response<>(true, "Artist followed successfully", null)
        );
    }

    // API để unfollow artist
    @DeleteMapping("/{artistId}/unfollow")
    @Transactional
    public ResponseEntity<Response<String>> unfollowArtist(@PathVariable Long artistId, @RequestParam Long userId) {
        User user = entityManager.find(User.class, userId);
        Artist artist = entityManager.find(Artist.class, artistId);

        if (user == null || artist == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "User or Artist not found", null)
            );
        }

        user.getFollowedArtists().remove(artist);
        entityManager.merge(user);

        return ResponseEntity.ok().body(
                new Response<>(true, "Artist unfollowed successfully", null)
        );
    }

    // API để kiểm tra trạng thái follow
    @GetMapping("/{artistId}/is-followed")
    @Transactional(readOnly = true)
    public ResponseEntity<Response<Boolean>> isArtistFollowed(@PathVariable Long artistId, @RequestParam Long userId) {
        User user = entityManager.find(User.class, userId);
        Artist artist = entityManager.find(Artist.class, artistId);

        if (user == null || artist == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "User or Artist not found", null)
            );
        }

        boolean isFollowed = user.getFollowedArtists().contains(artist);
        return ResponseEntity.ok().body(
                new Response<>(true, "Follow status retrieved successfully", isFollowed)
        );
    }
}