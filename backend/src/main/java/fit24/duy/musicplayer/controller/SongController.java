package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.dto.LyricsDTO;
import fit24.duy.musicplayer.dto.SongResponse;
import fit24.duy.musicplayer.entity.Album;
import fit24.duy.musicplayer.entity.Song;
import fit24.duy.musicplayer.entity.User;
import fit24.duy.musicplayer.model.Response;
import fit24.duy.musicplayer.repository.AlbumRepository;
import fit24.duy.musicplayer.repository.SongRepository;
import fit24.duy.musicplayer.repository.UserRepository;
import fit24.duy.musicplayer.service.SongService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

	@Autowired
	private SongRepository songRepository;

	@Autowired
	private AlbumRepository albumRepository;

	@Autowired
	private SongService songService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	// Lấy bài hát theo tên nghệ sĩ
	@GetMapping("/artist")
	public List<SongResponse> getSongsByArtistName(@RequestParam("name") String artistName) {
		List<Song> songs = songRepository.findByArtist_NameIgnoreCase(artistName);

		return songs.stream().map(song -> {
			SongResponse dto = new SongResponse();
			dto.setId(song.getId());
			dto.setTitle(song.getTitle());
			dto.setCoverImage(song.getCoverImage());
			dto.setArtist(song.getArtist());
			return dto;
		}).collect(Collectors.toList());
	}

	// Lấy bài hát theo tên album
	@GetMapping("/album")
	public List<SongResponse> getSongsByAlbumTitle(@RequestParam("title") String albumTitle) {
		List<Album> albums = albumRepository.findByTitleIgnoreCase(albumTitle);

		if (albums.isEmpty()) {
			return List.of();
		}

		Album album = albums.get(0);
		List<Song> songs = album.getSongs();

		return songs.stream().map(song -> {
			SongResponse dto = new SongResponse();
			dto.setId(song.getId());
			dto.setTitle(song.getTitle());
			dto.setCoverImage(song.getCoverImage());
			dto.setArtist(song.getArtist());
			return dto;
		}).collect(Collectors.toList());
	}

	@GetMapping("/recently-played")
	public ResponseEntity<List<Song>> getRecentlyPlayedSongs() {
		List<Song> recentlyPlayed = songService.getRecentlyPlayedSongs();
		if (recentlyPlayed.isEmpty()) {
			// Nếu không có bài hát đã phát gần đây, trả về 5 bài hát mới nhất
			recentlyPlayed = songRepository.findTop5ByOrderByIdDesc();
		}
		return new ResponseEntity<>(recentlyPlayed, HttpStatus.OK);
	}

	@GetMapping("/recommended")
	public ResponseEntity<List<Song>> getRecommendedSongs() {
		List<Song> recommended = songService.getRecommendedSongs();
		if (recommended.isEmpty()) {
			// Nếu không có bài hát được đề xuất, trả về 5 bài hát ngẫu nhiên
			recommended = songRepository.findTop5ByOrderByPlayCountDesc();
		}
		return new ResponseEntity<>(recommended, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Song> getSongById(@PathVariable Long id) {
		Song song = songService.getSongById(id);
		if (song == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(song);
	}

	@GetMapping("/search")
	public ResponseEntity<List<Song>> searchSongs(@RequestParam String title) {
		List<Song> searchResults = songService.searchSongsByTitle(title);
		return new ResponseEntity<>(searchResults, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<Song> createSong(@RequestBody Song song) {
		Song createdSong = songService.createSong(song);
		return ResponseEntity.ok(createdSong);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Song> updateSong(@PathVariable Long id, @RequestBody Song song) {
		song.setId(id);
		Song updatedSong = songService.updateSong(song);
		if (updatedSong == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(updatedSong);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
		songService.deleteSong(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/artist/{artistId}")
	public List<Song> getSongsByArtist(@PathVariable Long artistId) {
		return songService.getSongsByArtist(artistId);
	}

	@GetMapping("/album/{albumId}")
	public List<Song> getSongsByAlbum(@PathVariable Long albumId) {
		return songService.getSongsByAlbum(albumId);
	}

	// Lyrics endpoint - chỉ để xem lyrics
	@GetMapping("/{songId}/lyrics")
	public ResponseEntity<LyricsDTO> getLyrics(@PathVariable Long songId) {
		Song song = songService.getSongById(songId);
		if (song == null) {
			return ResponseEntity.notFound().build();
		}

		LyricsDTO lyricsDTO = new LyricsDTO();
		lyricsDTO.setLyrics(song.getLyrics());
		lyricsDTO.setLanguage("vi"); // Mặc định là tiếng Việt

		return ResponseEntity.ok(lyricsDTO);
	}


	@GetMapping("/random")
	public List<Song> getRandomSongs(@RequestParam(defaultValue = "10") int count) {
		return songService.getRandomSongs(count);
	}

	// Add other song-related endpoints as needed
	// API để người dùng like một bài hát
	@PostMapping("/{songId}/like")
	@Transactional
	public ResponseEntity<Response<String>> likeSong(@PathVariable Long songId, @RequestParam Long userId) {
		User user = entityManager.find(User.class, userId);
		Song song = entityManager.find(Song.class, songId);

		if (user == null || song == null) {
			return ResponseEntity.badRequest().body(
					new Response<>(false, "User or Song not found", null)
			);
		}

		if (song.getLikedByUsers().contains(user)) {
			return ResponseEntity.badRequest().body(
					new Response<>(false, "Song already liked", null)
			);
		}

		song.getLikedByUsers().add(user);
		entityManager.merge(song);

		return ResponseEntity.ok().body(
				new Response<>(true, "Song liked successfully", null)
		);
	}

	// API để người dùng unlike một bài hát
	@DeleteMapping("/{songId}/unlike")
	@Transactional
	public ResponseEntity<Response<String>> unlikeSong(@PathVariable Long songId, @RequestParam Long userId) {
		User user = entityManager.find(User.class, userId);
		Song song = entityManager.find(Song.class, songId);

		if (user == null || song == null) {
			return ResponseEntity.badRequest().body(
					new Response<>(false, "User or Song not found", null)
			);
		}

		if (!song.getLikedByUsers().contains(user)) {
			return ResponseEntity.badRequest().body(
					new Response<>(false, "Song not liked", null)
			);
		}

		song.getLikedByUsers().remove(user);
		entityManager.merge(song);

		return ResponseEntity.ok().body(
				new Response<>(true, "Song unliked successfully", null)
		);
	}

	// API để kiểm tra xem người dùng đã like bài hát chưa
	@GetMapping("/{songId}/is-liked")
	@Transactional(readOnly = true)
	public ResponseEntity<Response<Boolean>> isSongLiked(@PathVariable Long songId, @RequestParam Long userId) {
		User user = entityManager.find(User.class, userId);
		Song song = entityManager.find(Song.class, songId);

		if (user == null || song == null) {
			return ResponseEntity.badRequest().body(
					new Response<>(false, "User or Song not found", null)
			);
		}

		boolean isLiked = song.getLikedByUsers().contains(user);
		return ResponseEntity.ok().body(
				new Response<>(true, "Like status retrieved successfully", isLiked)
		);
	}

	// API để lấy danh sách bài hát đã like của người dùng
	@GetMapping("/liked/{userId}")
	@Transactional
	public ResponseEntity<List<Song>> getLikedSongs(@PathVariable Long userId) {
		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		User user = userOptional.get();
		List<Song> likedSongs = entityManager.createQuery(
						"SELECT s FROM Song s JOIN s.likedByUsers u WHERE u.id = :userId", Song.class)
				.setParameter("userId", userId)
				.getResultList();

		return ResponseEntity.ok(likedSongs);
	}
}
}