package fit24.duy.musicplayer.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
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
import fit24.duy.musicplayer.dialogs.EditLyricsDialog;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.LyricsManager;
import fit24.duy.musicplayer.utils.UrlUtils;
import fit24.duy.musicplayer.visualizer.MusicVisualizerView;
import fit24.duy.musicplayer.visualizer.VisualizerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;

public class PlayerActivity extends AppCompatActivity implements EditLyricsDialog.EditLyricsListener {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private MediaPlayer mediaPlayer;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration;
    private ImageView imgAlbumArt;
    private TextView tvSongTitle, tvArtist;
    private TextView currentLyricText, nextLyricText;
    private ImageButton editLyricsButton;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;
    private LyricsManager lyricsManager;
    private boolean isRepeat = false;

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

        // Initialize views
        initializeViews();
        
        // Setup MediaPlayer
        setupMediaPlayer();

        // Setup button click listeners
        setupClickListeners();

        // Check and request permission before initializing visualizer
        if (checkPermission()) {
            initializeVisualizer();
        } else {
            requestPermission();
        }

        // Nút back
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish();
        });
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
        currentLyricText = findViewById(R.id.currentLyricText);
        nextLyricText = findViewById(R.id.nextLyricText);
        editLyricsButton = findViewById(R.id.editLyricsButton);
        visualizerView = findViewById(R.id.visualizer);

        // Get data from Intent
        Song song = (Song) getIntent().getSerializableExtra("song");
        if (song != null) {
            tvSongTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
            
            // Load image using Glide with UrlUtils
            String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imgAlbumArt);
        }
    }

    private void setupMediaPlayer() {
        Song song = (Song) getIntent().getSerializableExtra("song");
        if (song == null) {
            Log.e(TAG, "No song data provided");
            finish();
            return;
        }

        String audioUrl = UrlUtils.getAudioUrl(song.getFilePath());
        Log.d(TAG, "Setting up MediaPlayer with URL: " + audioUrl);

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            
            // Set audio attributes for proper audio focus handling
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            );

            // Add OnBufferingUpdateListener to track loading progress
            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                Log.d(TAG, "Buffer update: " + percent + "%");
            });

            mediaPlayer.setDataSource(audioUrl);
            
            // Show loading indicator
            // TODO: Add a loading indicator in your layout
            Log.d(TAG, "Starting media preparation...");
            
            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    int duration = mp.getDuration();
                    Log.d(TAG, "Media duration from file: " + duration + "ms");
                    
                    if (duration > 0) {
                        tvDuration.setText(formatTime(duration));
                        seekBar.setMax(duration);
                        
                        // Hide loading indicator here
                        mp.start();
                        isPlaying = true;
                        updatePlayPauseButton();
                        startProgressUpdates();
                        
                        // Initialize LyricsManager after MediaPlayer is prepared
                        lyricsManager = new LyricsManager(mediaPlayer, currentLyricText, nextLyricText);
                        
                        // Fetch lyrics if available
                        if (song.getId() != null) {
                            fetchLyricsFromDatabase(song.getId().toString());
                        }
                        
                        // Initialize visualizer after MediaPlayer is prepared
                        if (checkPermission()) {
                            initializeVisualizer();
                        }
                        
                        Log.d(TAG, "MediaPlayer started successfully");
                    } else {
                        Log.e(TAG, "Invalid duration received from media file");
                        Toast.makeText(PlayerActivity.this, "Error: Could not determine media duration", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in onPrepared", e);
                    Toast.makeText(PlayerActivity.this, "Error starting playback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeat) {
                    mp.seekTo(0);
                    mp.start();
                } else {
                    isPlaying = false;
                    updatePlayPauseButton();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                String errorMessage;
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        errorMessage = "Network or file system error";
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        errorMessage = "Connection timeout";
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        errorMessage = "Unsupported audio format";
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        errorMessage = "Malformed audio data";
                        break;
                    default:
                        errorMessage = "Unknown error (what=" + what + ", extra=" + extra + ")";
                }
                Log.e(TAG, "MediaPlayer error: " + errorMessage);
                Toast.makeText(PlayerActivity.this, "Error playing audio: " + errorMessage, Toast.LENGTH_SHORT).show();
                return false;
            });

            // Add OnInfoListener for additional state information
            mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d(TAG, "Buffering started");
                        // TODO: Show buffering indicator
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.d(TAG, "Buffering ended");
                        // TODO: Hide buffering indicator
                        break;
                }
                return false;
            });

            Log.d(TAG, "Starting async preparation...");
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error preparing MediaPlayer", e);
            e.printStackTrace();
            Toast.makeText(this, "Error setting up audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNext());
        btnPrev.setOnClickListener(v -> playPrevious());
        btnShuffle.setOnClickListener(v -> toggleShuffle());
        btnRepeat.setOnClickListener(v -> toggleRepeat());
        editLyricsButton.setOnClickListener(v -> showEditLyricsDialog());

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
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            try {
                if (isPlaying) {
                    mediaPlayer.pause();
                    if (visualizerService != null) {
                        visualizerService.stopVisualizing();
                    }
                    if (lyricsManager != null) {
                        lyricsManager.stop();
                    }
                    Log.d(TAG, "Playback paused");
                } else {
                    mediaPlayer.start();
                    if (visualizerService != null) {
                        visualizerService.startVisualizing();
                    }
                    if (lyricsManager != null) {
                        lyricsManager.start();
                    }
                    Log.d(TAG, "Playback resumed");
                }
                isPlaying = !isPlaying;
                updatePlayPauseButton();
            } catch (Exception e) {
                Log.e(TAG, "Error toggling play/pause", e);
                Toast.makeText(this, "Error controlling playback", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePlayPauseButton() {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        btnRepeat.setImageResource(isRepeat ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);
    }

    private void playNext() {
        // TODO: Implement next song logic
        Toast.makeText(this, "Next song feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void playPrevious() {
        // TODO: Implement previous song logic
        Toast.makeText(this, "Previous song feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void toggleShuffle() {
        // TODO: Implement shuffle logic
        Toast.makeText(this, "Shuffle feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void startProgressUpdates() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    try {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        tvCurrentTime.setText(formatTime(currentPosition));
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating progress", e);
                    }
                }
                handler.postDelayed(this, 100); // Update more frequently
            }
        }, 100);
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
        // Initialize visualizer views
        ImageButton btnVisualizerType = findViewById(R.id.btn_visualizer_type);
        ImageButton btnVisualizerColor = findViewById(R.id.btn_visualizer_color);

        // Initialize VisualizerService
        visualizerService = new VisualizerService(mediaPlayer, visualizerView);

        // Set up visualizer type toggle
        btnVisualizerType.setOnClickListener(v -> {
            isWaveform = !isWaveform;
            visualizerService.setVisualizerType(isWaveform);
            btnVisualizerType.setImageResource(isWaveform ? 
                R.drawable.ic_waveform : R.drawable.ic_spectrum);
        });

        // Set up color change
        btnVisualizerColor.setOnClickListener(v -> {
            currentColorIndex = (currentColorIndex + 1) % colors.length;
            visualizerService.setVisualizerColor(colors[currentColorIndex]);
        });

        // Start visualizer if music is playing
        if (isPlaying) {
            visualizerService.startVisualizing();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer", e);
            }
            mediaPlayer = null;
        }
        if (visualizerService != null) {
            visualizerService.release();
            visualizerService = null;
        }
    }
} 