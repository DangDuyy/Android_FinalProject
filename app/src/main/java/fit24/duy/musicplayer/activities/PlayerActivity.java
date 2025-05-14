package fit24.duy.musicplayer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.dialogs.QueueDialog;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import android.widget.PopupMenu;


public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private ImageButton btnPlayPause, btnNext, btnPrev, btnMenu, btnLike, btnBack, btnQueue;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration, tvLyrics;
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

    private void downloadSong() {
        // Logic to download the song
        Toast.makeText(this, "Downloading song...", Toast.LENGTH_SHORT).show();
    }
    private int[] colors = {
        android.graphics.Color.parseColor("#FF6B6B"),
        android.graphics.Color.parseColor("#4ECDC4"),
        android.graphics.Color.parseColor("#45B7D1"),
        android.graphics.Color.parseColor("#96CEB4"),
        android.graphics.Color.parseColor("#FFEEAD")
    };
    private int currentColorIndex = 0;

    private android.media.MediaPlayer mediaPlayer;

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

        // Lấy song_id từ Intent
        long songId = getIntent().getLongExtra("song_id", -1);
        if (songId == -1) {
            Toast.makeText(this, "Invalid song", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gọi API lấy thông tin bài hát
        fetchSongInfo(songId);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        btnPrev = findViewById(R.id.btn_prev);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnQueue = findViewById(R.id.queue_button);
        btnNext = findViewById(R.id.btn_next);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);
        imgAlbumArt = findViewById(R.id.img_album_art);
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_artist);
        tvLyrics = findViewById(R.id.tv_lyrics);
        visualizerView = findViewById(R.id.visualizer);
        btnMenu = findViewById(R.id.btn_menu);
        btnLike = findViewById(R.id.btn_like);
        sessionManager = new SessionManager(this);

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

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            initializeVisualizer();
        } else {
            Log.e(TAG, "MediaPlayer chưa phát nhạc. Visualizer không thể khởi động.");
        }
    }

    private void setupClickListeners() {
        btnPrev.setOnClickListener(v -> {
            Toast.makeText(this, "Previous song feature coming soon", Toast.LENGTH_SHORT).show();
        });
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            Toast.makeText(this, "Next song feature coming soon", Toast.LENGTH_SHORT).show();
        });

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

        btnQueue.setOnClickListener(v -> {
            if (queueManager.getQueue().isEmpty()) {
                queueManager.fillQueueWithRandomSongs(10); // Populate queue with random songs
            }
            openQueueDialog();
        });


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
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnPrev.bringToFront();
        btnPlayPause.bringToFront();
        btnNext.bringToFront();

        // Nút back
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnMenu.setOnClickListener(v -> {
            // Create a PopupMenu
            PopupMenu popupMenu = new PopupMenu(this, btnMenu);
            popupMenu.getMenuInflater().inflate(R.menu.song_menu, popupMenu.getMenu());

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_download) {
                    downloadSong();
                    return true;
                }
                return false;
            });

            // Show the menu
            popupMenu.show();
        });
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateSongInfoAndSeekBar();
        checkSongLiked();
    }

    private void fetchSongInfo(long songId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getSongById(songId).enqueue(new retrofit2.Callback<Song>() {
            @Override
            public void onResponse(retrofit2.Call<Song> call, retrofit2.Response<Song> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Song song = response.body();
                    Log.d("PlayerActivity", "API filePath: " + song.getFilePath());
                    tvSongTitle.setText(song.getTitle());
                    tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
                    tvLyrics.setText(song.getLyrics());
                    Glide.with(PlayerActivity.this)
                        .load(fit24.duy.musicplayer.utils.UrlUtils.getImageUrl(song.getCoverImage()))
                        .centerCrop()
                        .into(imgAlbumArt);
                    playSong(song.getFilePath());

                } else {
                    Toast.makeText(PlayerActivity.this, "Failed to load song info", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<Song> call, Throwable t) {
                Toast.makeText(PlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playSong(String filePath) {
        try {
            String fullUrl = UrlUtils.getAudioUrl(filePath);
            Log.d("PlayerActivity", "Full audio URL: " + fullUrl);
            if (mediaPlayer == null) mediaPlayer = new android.media.MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fullUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            int duration = mediaPlayer.getDuration();
            seekBar.setMax(duration);
            tvDuration.setText(formatTime(duration));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PlayerActivity", "Play error: " + e.getMessage());
            Toast.makeText(this, "Cannot play this song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openQueueDialog() {
        List<Song> queue = queueManager.getQueue();
        int currentIndex = queueManager.getCurrentIndex();

        QueueDialog dialog = QueueDialog.newInstance(queue, currentIndex);
        dialog.setOnQueueItemClickListener(new QueueDialog.OnQueueItemClickListener() {
            @Override
            public void onItemClick(int position) {
                queueManager.playSongAt(position); // Gọi playSongAt từ QueueManager
            }

            @Override
            public void onItemRemoved(int position) {
                queueManager.removeSongAt(position); // Gọi removeSongAt từ QueueManager
            }
        });
        dialog.show(getSupportFragmentManager(), "QueueDialog");
    }
}