package fit24.duy.musicplayer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.SessionManager;
import fit24.duy.musicplayer.utils.UrlUtils;
import fit24.duy.musicplayer.visualizer.MusicVisualizerView;
import fit24.duy.musicplayer.visualizer.VisualizerService;
import fit24.duy.musicplayer.utils.QueueManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;
import java.io.IOException;
import android.media.MediaPlayer;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat, btnMenu, btnLike;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration;
    private ImageView imgAlbumArt;
    private TextView tvSongTitle, tvArtist;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private boolean isLiked = false;
    private QueueManager queueManager;
    private Runnable updateSeekBarRunnable;
    private Long userId, songId;
    private ApiService apiService;
    private SessionManager sessionManager;

    // Visualizer variables
    private MusicVisualizerView visualizerView;
    private VisualizerService visualizerService;
    private boolean isWaveform = true;
    private int[] colors = {
        android.graphics.Color.parseColor("#FF6B6B"),
        android.graphics.Color.parseColor("#4ECDC4"),
        android.graphics.Color.parseColor("#45B7D1"),
        android.graphics.Color.parseColor("#96CEB4"),
        android.graphics.Color.parseColor("#FFEEAD")
    };
    private int currentColorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        queueManager = QueueManager.getInstance(this);
        queueManager.setPlaybackStateListener(isPlaying -> {
            this.isPlaying = isPlaying;
            updatePlayPauseButton();
        });

        initializeViews();
        setupClickListeners();

        if (checkPermission()) {
            initializeVisualizer();
        } else {
            requestPermission();
        }

        updateSongInfoAndSeekBar();
    }

    private void initializeViews() {
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnNext = findViewById(R.id.btn_next);
        btnPrev = findViewById(R.id.btn_prev);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnRepeat = findViewById(R.id.btn_repeat);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);
        imgAlbumArt = findViewById(R.id.img_album_art);
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_artist);
        visualizerView = findViewById(R.id.visualizer);
        btnMenu = findViewById(R.id.btn_menu);
        btnLike = findViewById(R.id.btn_like);

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy userId từ SessionManager
        String userIdString = sessionManager.getUserId();
        if (userIdString == null || userIdString.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            userId = Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> {
            MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
            if (mediaPlayer.isPlaying()) {
                queueManager.pause();
            } else {
                queueManager.play();
            }
            updatePlayPauseButton();
        });
        
        btnNext.setOnClickListener(v -> {
            queueManager.playNext();
            updateSongInfoAndSeekBar();
            updatePlayPauseButton();
        });
        
        btnPrev.setOnClickListener(v -> {
            queueManager.playPrevious();
            updateSongInfoAndSeekBar();
            updatePlayPauseButton();
        });
        
        btnShuffle.setOnClickListener(v -> toggleShuffle());
        btnRepeat.setOnClickListener(v -> toggleRepeat());
        
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Intent intent = new Intent(PlayerActivity.this, SongControlActivity.class);
                Song song = (Song) getIntent().getSerializableExtra("song");
                if (song != null) {
                    intent.putExtra("song_id", song.getId());
                    intent.putExtra("song_title", song.getTitle());
                    intent.putExtra("artist_name", song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
                    intent.putExtra("album_art_url", UrlUtils.getImageUrl(song.getCoverImage()));
                }
                startActivity(intent);
            });
        }
        
        if (btnLike != null) {
            btnLike.setOnClickListener(v -> {
                if (isLiked) {
                    unlikeSong();
                } else {
                    likeSong();
                }
            });
        }
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                        tvCurrentTime.setText(formatTime(progress));
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        btnPlayPause.bringToFront();
        btnNext.bringToFront();
        btnPrev.bringToFront();
        btnShuffle.bringToFront();
        btnRepeat.bringToFront();
    }

    private void updatePlayPauseButton() {
        MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
        boolean isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void updateSongInfoAndSeekBar() {
        Song song = queueManager.getCurrentSong();
        MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
        if (song != null && mediaPlayer != null) {
            tvSongTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
            String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imgAlbumArt);
            seekBar.setMax(mediaPlayer.getDuration());
            tvDuration.setText(formatTime(mediaPlayer.getDuration()));
            updateSeekBar();
        }
    }

    private void updateSeekBar() {
        if (updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
        
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        btnRepeat.setAlpha(isRepeat ? 1.0f : 0.5f);
        queueManager.setRepeatMode(isRepeat);
    }

    private void toggleShuffle() {
        // TODO: Implement shuffle logic
        Toast.makeText(this, "Shuffle feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(int milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeVisualizer();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeVisualizer() {
        MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
        visualizerService = new VisualizerService(mediaPlayer, visualizerView);
        visualizerService.startVisualizing();
    }

    private void checkSongLiked() {
        Song currentSong = queueManager.getCurrentSong();
        if (currentSong != null) {
            apiService.isSongLiked(currentSong.getId(), userId).enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isLiked = response.body().getData();
                        updateLikeButton();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    Log.e(TAG, "Error checking if song is liked", t);
                }
            });
        }
    }

    private void likeSong() {
        Song currentSong = queueManager.getCurrentSong();
        if (currentSong != null) {
            apiService.likeSong(currentSong.getId(), userId).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isLiked = true;
                        updateLikeButton();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Log.e(TAG, "Error liking song", t);
                }
            });
        }
    }

    private void unlikeSong() {
        Song currentSong = queueManager.getCurrentSong();
        if (currentSong != null) {
            apiService.unlikeSong(currentSong.getId(), userId).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isLiked = false;
                        updateLikeButton();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Log.e(TAG, "Error unliking song", t);
                }
            });
        }
    }

    private void updateLikeButton() {
        btnLike.setImageResource(isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
        if (visualizerService != null) {
            visualizerService.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSongInfoAndSeekBar();
        checkSongLiked();
    }
}