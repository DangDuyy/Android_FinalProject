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
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.service.MusicPlayerService;
import fit24.duy.musicplayer.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
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

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupNavigation();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_search_result || destination.getId() == R.id.navigation_artist
                    || destination.getId() == R.id.navigation_album || destination.getId() == R.id.navigation_album_control
                    || destination.getId() == R.id.navigation_artist_control || destination.getId() == R.id.navigation_media_type
                    || destination.getId() == R.id.navigation_liked_songs) {
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
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

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
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
