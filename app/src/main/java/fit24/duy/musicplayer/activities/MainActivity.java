package fit24.duy.musicplayer.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

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
import fit24.duy.musicplayer.service.MusicPlayerService;
import fit24.duy.musicplayer.utils.QueueManager;
import fit24.duy.musicplayer.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private PlayerBar playerBar;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private SessionManager sessionManager;
    private MusicPlayerService musicService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

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

        // Fill queue with random songs from backend if empty
        QueueManager queueManager = QueueManager.getInstance(this);
        if (queueManager.getQueue().isEmpty()) {
            queueManager.fillQueueWithRandomSongsFromApi(10);
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
                    || destination.getId() == R.id.navigation_liked_songs || destination.getId() == R.id.navigation_edit_profile
                    || destination.getId() == R.id.navigation_profile) {
                getSupportActionBar().hide();  // Ẩn toolbar khi vào trang SearchResult hoặc Artist
            } else {
                getSupportActionBar().show();  // Hiển thị toolbar khi vào các trang khác
            }
        });

        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
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
        View playerBarView = findViewById(R.id.playerBar);
        QueueManager queueManager = QueueManager.getInstance(this);
        playerBar = new PlayerBar(playerBarView, queueManager);
        playerBarView.setVisibility(View.GONE); // Ẩn playerBar ban đầu
        
        playerBarView.setOnClickListener(v -> {
            if (playerBar.getCurrentSong() != null) {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("song", playerBar.getCurrentSong());
                startActivity(intent);
            }
        });

        playerBar.setPlayPauseClickListener(new PlayerBar.OnPlayPauseClickListener() {
            @Override
            public void onPlayPauseClick() {
                if (isPlaying) {
                    queueManager.pause();
                    isPlaying = false;
                } else {
                    queueManager.play();
                    isPlaying = true;
                }
                playerBar.togglePlayPause(isPlaying);
            }

            @Override
            public void onPlayNext() {
                queueManager.playNext();
                isPlaying = true;
                playerBar.togglePlayPause(true);
            }

            @Override
            public void onPlayPrevious() {
                queueManager.playPrevious();
                isPlaying = true;
                playerBar.togglePlayPause(true);
            }

            @Override
            public void onQueueSongSelected(Song song) {
                int index = queueManager.getQueue().indexOf(song);
                if (index != -1) {
                    queueManager.setCurrentIndex(index);
                    queueManager.play();
                    isPlaying = true;
                    playerBar.togglePlayPause(true);
                    playerBar.setSongInfo(song);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    // When a song is selected from your list
    public void onSongSelected(Song song) {
        if (song != null && song.getAudioUrl() != null) {
            Log.d(TAG, "Selected song: " + song.getTitle() + ", URL: " + song.getAudioUrl());
            View playerBarView = findViewById(R.id.playerBar);
            if (playerBarView != null) {
                playerBarView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Adding song to queue: " + song.getTitle());
                playerBar.addToQueue(song);
                playerBar.setSongInfo(song);
                playerBar.togglePlayPause(false); // Start with play button
                playSong(song); // Sử dụng service
            } else {
                Log.e(TAG, "PlayerBar view not found");
            }
        } else {
            Log.e(TAG, "Invalid song or audio URL. Song: " + (song != null ? song.getTitle() : "null") + ", URL: " + (song != null ? song.getAudioUrl() : "null"));
        }
    }

    public void playSong(Song song) {
        if (isServiceBound && musicService != null && song != null && song.getAudioUrl() != null) {
            musicService.play(song.getAudioUrl());
            isPlaying = true;
            playerBar.togglePlayPause(true);
        }
    }

    public void pauseMusic() {
        if (isServiceBound && musicService != null) {
            musicService.pause();
            isPlaying = false;
            playerBar.togglePlayPause(false);
        }
    }

    public void resumeMusic() {
        if (isServiceBound && musicService != null) {
            musicService.resume();
            isPlaying = true;
            playerBar.togglePlayPause(true);
        }
    }

    public void stopMusic() {
        if (isServiceBound && musicService != null) {
            musicService.stop();
            isPlaying = false;
            playerBar.togglePlayPause(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer released");
        }
    }
} 
