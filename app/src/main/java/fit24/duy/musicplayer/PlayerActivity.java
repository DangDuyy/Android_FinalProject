package fit24.duy.musicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import fit24.duy.musicplayer.visualizer.MusicVisualizerView;
import fit24.duy.musicplayer.visualizer.VisualizerService;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private MusicVisualizerView visualizerView;
    private VisualizerService visualizerService;
    private boolean isWaveform = true;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private int[] colors = {
        android.graphics.Color.parseColor("#FF6B6B"),
        android.graphics.Color.parseColor("#4ECDC4"),
        android.graphics.Color.parseColor("#45B7D1"),
        android.graphics.Color.parseColor("#96CEB4"),
        android.graphics.Color.parseColor("#FFEEAD")
    };
    private int currentColorIndex = 0;
    private ArrayList<String> playlist;
    private int currentSongIndex = 0;
    private Handler handler = new Handler();
    private SeekBar seekBar;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private ImageButton btnShuffle;
    private ImageButton btnRepeat;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnPlayPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Initialize views
        visualizerView = findViewById(R.id.visualizer);
        ImageButton btnVisualizerType = findViewById(R.id.btn_visualizer_type);
        ImageButton btnVisualizerColor = findViewById(R.id.btn_visualizer_color);
        seekBar = findViewById(R.id.seekBar);
        currentTimeText = findViewById(R.id.tv_current_time);
        totalTimeText = findViewById(R.id.tv_duration);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnRepeat = findViewById(R.id.btn_repeat);
        btnPrevious = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnPlayPause = findViewById(R.id.btn_play_pause);

        // Initialize playlist
        playlist = new ArrayList<>();
        // TODO: Load playlist from your data source

        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();
        setupMediaPlayer();

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

        // Set up play/pause button
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                pauseMusic();
            } else {
                playMusic();
            }
        });

        // Set up shuffle button
        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setImageResource(isShuffle ? 
                R.drawable.ic_shuffle : R.drawable.ic_shuffle);
        });

        // Set up repeat button
        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setImageResource(isRepeat ? 
                R.drawable.ic_repeat : R.drawable.ic_repeat);
        });

        // Set up previous button
        btnPrevious.setOnClickListener(v -> playPrevious());

        // Set up next button
        btnNext.setOnClickListener(v -> playNext());

        // Set up seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    updateCurrentTimeText(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Start updating seekbar
        startSeekBarUpdate();
    }

    private void setupMediaPlayer() {
        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeat) {
                mp.seekTo(0);
                mp.start();
            } else {
                playNext();
            }
        });
    }

    private void playNext() {
        if (playlist.isEmpty()) return;
        
        if (isShuffle) {
            Random random = new Random();
            currentSongIndex = random.nextInt(playlist.size());
        } else {
            currentSongIndex = (currentSongIndex + 1) % playlist.size();
        }
        playCurrentSong();
    }

    private void playPrevious() {
        if (playlist.isEmpty()) return;
        
        if (isShuffle) {
            Random random = new Random();
            currentSongIndex = random.nextInt(playlist.size());
        } else {
            currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        }
        playCurrentSong();
    }

    private void playCurrentSong() {
        try {
            // Stop and reset current playback
            stopMusic();
            
            // Set up new song
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playlist.get(currentSongIndex));
            mediaPlayer.prepare();
            mediaPlayer.start();
            visualizerService.startVisualizing();
            updateTotalTimeText(mediaPlayer.getDuration());
            seekBar.setMax(mediaPlayer.getDuration());
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            visualizerService.startVisualizing();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            visualizerService.stopVisualizing();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            visualizerService.stopVisualizing();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void startSeekBarUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    updateCurrentTimeText(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateCurrentTimeText(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        currentTimeText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateTotalTimeText(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        totalTimeText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (visualizerService != null) {
            visualizerService.release();
            visualizerService = null;
        }
    }
}