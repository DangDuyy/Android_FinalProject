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
import fit24.duy.musicplayer.dialogs.EditLyricsDialog;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.LyricsManager;
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

public class PlayerActivity extends AppCompatActivity implements EditLyricsDialog.EditLyricsListener {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat, btnMenu, btnLike, btnLyrics;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration;
    private ImageView imgAlbumArt;
    private TextView tvSongTitle, tvArtist;
    private TextView currentLyricText, nextLyricText;
    private ImageButton editLyricsButton;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private boolean isLiked = false;
    private LyricsManager lyricsManager;
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
        btnLyrics = findViewById(R.id.btn_lyrics);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);
        imgAlbumArt = findViewById(R.id.img_album_art);
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_artist);
        currentLyricText = findViewById(R.id.currentLyricText);
        nextLyricText = findViewById(R.id.nextLyricText);
        editLyricsButton = findViewById(R.id.editLyricsButton);
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

        // Set up lyrics toggle button
        btnLyrics.setOnClickListener(v -> {
            if (lyricsManager != null) {
                lyricsManager.toggleVisibility();
                btnLyrics.setAlpha(lyricsManager.isVisible() ? 1.0f : 0.5f);
            }
        });
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
        editLyricsButton.setOnClickListener(v -> showEditLyricsDialog());
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
        MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
        if (mediaPlayer == null) return;
        handler.removeCallbacksAndMessages(null);
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        queueManager.setRepeatMode(isRepeat);
        btnRepeat.setImageResource(isRepeat ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);
    }

    private void toggleShuffle() {
        // TODO: Implement shuffle logic
        Toast.makeText(this, "Shuffle feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(int milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void showEditLyricsDialog() {
        String currentLyrics = getIntent().getStringExtra("SONG_LYRICS");
        EditLyricsDialog dialog = EditLyricsDialog.newInstance(
            currentLyrics != null ? currentLyrics : "",
            "vi"
        );
        dialog.show(getSupportFragmentManager(), "EditLyricsDialog");
    }

    private void fetchLyricsFromDatabase(String songId) {
        String apiUrl = UrlUtils.getBackendUrl() + "/api/songs/" + songId + "/lyrics";
        Log.d(TAG, "Fetching lyrics from: " + apiUrl);

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                Log.d(TAG, "Connecting to API...");
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream;
                    if (responseCode >= 400) {
                        inputStream = connection.getErrorStream();
                    } else {
                        inputStream = connection.getInputStream();
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String responseBody = response.toString();
                    Log.d(TAG, "Response body: " + responseBody);

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String lyrics = jsonResponse.optString("lyrics", "");

                    Log.d(TAG, "Parsed lyrics: " + lyrics);

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (!lyrics.isEmpty()) {
                            lyricsManager.setLyrics(lyrics, "vi");
                            Log.d(TAG, "Lyrics loaded successfully");
                        } else {
                            Log.d(TAG, "No lyrics found for this song");
                            currentLyricText.setText("No lyrics available");
                            nextLyricText.setText("");
                        }
                    });
                } else {
                    String errorMessage = "Error fetching lyrics. Response code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    runOnUiThread(() -> {
                        currentLyricText.setText("Error loading lyrics");
                        nextLyricText.setText("");
                        Toast.makeText(PlayerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                String errorMessage = "Error fetching lyrics: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                runOnUiThread(() -> {
                    currentLyricText.setText("Error loading lyrics");
                    nextLyricText.setText("");
                    Toast.makeText(PlayerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    @Override
    public void onLyricsSaved(String lyrics, String language) {
        lyricsManager.setLyrics(lyrics, language);
        if (isPlaying) {
            lyricsManager.start();
        }

        // Save lyrics to database
        Song song = (Song) getIntent().getSerializableExtra("song");
        if (song != null && song.getId() != null) {
            saveLyricsToDatabase(song.getId().toString(), lyrics);
        }
    }

    private void saveLyricsToDatabase(String songId, String lyrics) {
        String apiUrl = UrlUtils.getBackendUrl() + "/api/songs/" + songId + "/lyrics";

        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON body
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("lyrics", lyrics);

                // Send request
                connection.getOutputStream().write(jsonBody.toString().getBytes());

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Error saving lyrics: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving lyrics: " + e.getMessage());
            }
        }).start();
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeVisualizer();
            } else {
                Toast.makeText(this, "Quyền truy cập âm thanh bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeVisualizer() {
        visualizerView = findViewById(R.id.visualizer);
        MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
        visualizerService = new VisualizerService(mediaPlayer, visualizerView);
        visualizerService.startVisualizing();
    }

    private void checkSongLiked() {
        if (songId == null || userId == null) {
            Toast.makeText(this, "Invalid song or user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Boolean>> call = apiService.isSongLiked(songId, userId);
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    isLiked = response.body().getData();
                    btnLike.setImageResource(isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart);
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(PlayerActivity.this, "Failed to check like status: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(PlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void likeSong() {
        if (songId == null || userId == null) {
            Toast.makeText(this, "Invalid song or user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<String>> call = apiService.likeSong(songId, userId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    isLiked = true;
                    btnLike.setImageResource(R.drawable.ic_heart_red);
                    Toast.makeText(PlayerActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(PlayerActivity.this, "Failed to like: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(PlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlikeSong() {
        if (songId == null || userId == null) {
            Toast.makeText(this, "Invalid song or user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<String>> call = apiService.unlikeSong(songId, userId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    isLiked = false;
                    btnLike.setImageResource(R.drawable.ic_heart);
                    Toast.makeText(PlayerActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(PlayerActivity.this, "Failed to unlike: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(PlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (visualizerService != null) {
            visualizerService.release();
            visualizerService = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSongLiked();
    }
}