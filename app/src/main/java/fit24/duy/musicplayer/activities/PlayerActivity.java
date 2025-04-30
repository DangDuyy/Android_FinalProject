package fit24.duy.musicplayer.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import fit24.duy.musicplayer.dialogs.EditLyricsDialog;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.models.Artist;
import fit24.duy.musicplayer.utils.LyricsManager;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import fit24.duy.musicplayer.visualizer.MusicVisualizerView;
import fit24.duy.musicplayer.visualizer.VisualizerService;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;
import java.io.OutputStream;

public class PlayerActivity extends AppCompatActivity implements EditLyricsDialog.EditLyricsListener {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private MediaPlayer mediaPlayer;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration;
    private ImageView imgAlbumArt;
    private TextView tvSongTitle, tvArtist;
    private TextView currentLyricText, nextLyricText;
    private ImageButton editLyricsButton;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private LyricsManager lyricsManager;

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
    }

    private void initializeViews() {
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnNext = findViewById(R.id.btn_next);
        btnPrev = findViewById(R.id.btn_prev);
        btnShuffle = findViewById(R.id.btn_shuffle);
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

        // Nhận dữ liệu từ Intent
        String title = getIntent().getStringExtra("SONG_TITLE");
        String artist = getIntent().getStringExtra("SONG_ARTIST");
        String image = getIntent().getStringExtra("SONG_IMAGE");
        String songId = getIntent().getStringExtra("SONG_ID");

        // Hiển thị lên UI
        tvSongTitle.setText(title);
        tvArtist.setText(artist);
        Glide.with(this).load(image).into(imgAlbumArt);

        // Khởi tạo visualizer
        if (checkPermission()) {
            initializeVisualizer();
        }
    }

    private void setupMediaPlayer() {
        String url = getIntent().getStringExtra("SONG_URL");
        String songId = getIntent().getStringExtra("SONG_ID");
        
        Log.d(TAG, "Setting up MediaPlayer with URL: " + url);
        Log.d(TAG, "Song ID: " + songId);
        
        if (url == null) {
            Log.e(TAG, "No audio URL provided");
            finish();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                tvDuration.setText(formatTime(duration));
                seekBar.setMax(duration);
                mp.start();
                isPlaying = true;
                updatePlayPauseButton();
                startProgressUpdates();
                
                // Khởi tạo LyricsManager sau khi MediaPlayer đã sẵn sàng
                lyricsManager = new LyricsManager(mediaPlayer, currentLyricText, nextLyricText);
                
                // Lấy lyrics từ database nếu có songId
                if (songId != null) {
                    Log.d(TAG, "Fetching lyrics for song ID: " + songId);
                    fetchLyricsFromDatabase(songId);
                } else {
                    Log.e(TAG, "No song ID provided");
                    currentLyricText.setText("No lyrics available");
                    nextLyricText.setText("");
                }
                
                // Initialize visualizer after MediaPlayer is prepared
                if (checkPermission()) {
                    initializeVisualizer();
                }
                
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
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNext());
        btnPrev.setOnClickListener(v -> playPrevious());
        btnShuffle.setOnClickListener(v -> toggleShuffle());
        editLyricsButton.setOnClickListener(v -> showEditLyricsDialog());

        // Thêm SeekBar listener
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
            if (isPlaying) {
                mediaPlayer.pause();
                visualizerService.stopVisualizing();
                lyricsManager.stop();
                Log.d(TAG, "Playback paused");
            } else {
                mediaPlayer.start();
                visualizerService.startVisualizing();
                lyricsManager.start();
                Log.d(TAG, "Playback resumed");
            }
            isPlaying = !isPlaying;
            updatePlayPauseButton();
        }
    }

    private void updatePlayPauseButton() {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void playNext() {
        // TODO: Implement next song logic
    }

    private void playPrevious() {
        // TODO: Implement previous song logic
    }

    private void toggleShuffle() {
        // TODO: Implement shuffle logic
    }

    private void startProgressUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
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
        String apiUrl = "http://10.0.2.2:8080/api/songs/" + songId + "/lyrics";
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
        
        // Lưu lyrics lên database
        String songId = getIntent().getStringExtra("SONG_ID");
        if (songId != null) {
            saveLyricsToDatabase(songId, lyrics);
        }
    }

    private void saveLyricsToDatabase(String songId, String lyrics) {
        // TODO: Thay thế URL bằng URL thực của API của bạn
        String apiUrl = "http://10.0.2.2:8080/api/songs/" + songId + "/lyrics";
        
        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                // Tạo JSON body
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("lyrics", lyrics);
                
                // Gửi request
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();
                
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer released");
        }
        if (visualizerService != null) {
            visualizerService.release();
            visualizerService = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
} 