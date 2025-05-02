package fit24.duy.musicplayer.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.PlayerBar;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private PlayerBar playerBar;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup navigation
        setupNavigation();

        // Setup player bar
        setupPlayerBar();

        // Listener to manage toolbar visibility
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_search_result || destination.getId() == R.id.navigation_artist
                    || destination.getId() == R.id.navigation_album || destination.getId() == R.id.navigation_album_control
                    || destination.getId() == R.id.navigation_artist_control || destination.getId() == R.id.navigation_media_type
                    || destination.getId() == R.id.navigation_liked_songs) {
                getSupportActionBar().hide();  // Ẩn toolbar khi vào trang SearchResult hoặc Artist
            } else {
                getSupportActionBar().show();  // Hiển thị toolbar khi vào các trang khác
            }
        });

        // Set listener for play/pause button
        playerBar.setOnPlayPauseClickListener(isPlaying -> {
            if (isPlaying) {
                playMusic();
            } else {
                pauseMusic();
            }
        });
    }

    public void logout() {
        // Clear session
        sessionManager.logout();

        // Navigate to welcome screen
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_library, R.id.navigation_profile
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void setupPlayerBar() {
        playerBar = new PlayerBar(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    // When a song is selected from your list
    public void onSongSelected(Song song) {
        if (song != null && song.getAudioUrl() != null) {
            Log.d(TAG, "Selected song: " + song.getTitle() + ", URL: " + song.getAudioUrl());
            playerBar.setSongInfo(
                    song.getTitle(),
                    song.getArtist() != null ? song.getArtist().getName() : "",
                    song.getCoverImage(),
                    song.getAudioUrl(),
                    song
            );
            playerBar.setPlaying(true);
            playMusic();
        } else {
            Log.e(TAG, "Invalid song or audio URL");
        }
    }

    private void playMusic() {
        if (playerBar.getCurrentSong() == null || playerBar.getCurrentSong().getAudioUrl() == null) {
            Log.e(TAG, "No song selected or invalid audio URL");
            return;
        }

        String audioUrl = playerBar.getCurrentSong().getAudioUrl();
        Log.d(TAG, "Playing music from URL: " + audioUrl);

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    isPlaying = true;
                    Log.d(TAG, "MediaPlayer started successfully");
                });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                    return false;
                });
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "Error preparing MediaPlayer", e);
                e.printStackTrace();
            }
        } else if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            Log.d(TAG, "Resumed playback");
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            Log.d(TAG, "Playback paused");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer released");
        }
    }
} 
