package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.entity.Album;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.model.Response;
import fit24.duy.musicplayer.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // API để thêm album vào thư viện của người dùng
    @PostMapping("/{albumId}/add-to-library")
    @Transactional
    public ResponseEntity<Response<String>> addAlbumToLibrary(@PathVariable Long albumId, @RequestParam Long userId) {
        User user = entityManager.find(User.class, userId);
        Album album = entityManager.find(Album.class, albumId);

        if (user == null || album == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "User or Album not found", null)
            );
        }

        if (user.getAddedAlbums().contains(album)) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "Album already in library", null)
            );
        }

        user.getAddedAlbums().add(album);
        entityManager.merge(user);

        return ResponseEntity.ok().body(
                new Response<>(true, "Album added to library", null)
        );
    }

    // API để xóa album khỏi thư viện của người dùng
    @DeleteMapping("/{albumId}/remove-from-library")
    @Transactional
    public ResponseEntity<Response<String>> removeAlbumFromLibrary(@PathVariable Long albumId, @RequestParam Long userId) {
        User user = entityManager.find(User.class, userId);
        Album album = entityManager.find(Album.class, albumId);

        if (user == null || album == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "User or Album not found", null)
            );
        }

        if (!user.getAddedAlbums().contains(album)) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "Album not in library", null)
            );
        }

        user.getAddedAlbums().remove(album);
        entityManager.merge(user);

        return ResponseEntity.ok().body(
                new Response<>(true, "Album removed from library", null)
        );
    }

    // API để kiểm tra xem album có trong thư viện của người dùng không
    @GetMapping("/{albumId}/is-in-library")
    @Transactional(readOnly = true)
    public ResponseEntity<Response<Boolean>> isAlbumInLibrary(@PathVariable Long albumId, @RequestParam Long userId) {
        User user = entityManager.find(User.class, userId);
        Album album = entityManager.find(Album.class, albumId);

        if (user == null || album == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>(false, "User or Album not found", null)
            );
        }

        boolean isInLibrary = user.getAddedAlbums().contains(album);
        return ResponseEntity.ok().body(
                new Response<>(true, "Library status retrieved successfully", isInLibrary)
        );
    }

    // Lấy danh sách album trong thư viện của người dùng
    @GetMapping("/library/{userId}")
    public ResponseEntity<List<Album>> getLibraryAlbums(@PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        Set<Album> addedAlbums = user.getAddedAlbums();
        return ResponseEntity.ok(addedAlbums.stream().toList());
    }
}