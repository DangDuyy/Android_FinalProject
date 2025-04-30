package fit24.duy.musicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import fit24.duy.musicplayer.visualizer.MusicVisualizerView;
import fit24.duy.musicplayer.visualizer.VisualizerService;

public class PlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
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
        visualizerView = findViewById(R.id.visualizer);
        ImageButton btnVisualizerType = findViewById(R.id.btn_visualizer_type);
        ImageButton btnVisualizerColor = findViewById(R.id.btn_visualizer_color);

        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();
        // Set up your MediaPlayer with the audio source
        // mediaPlayer.setDataSource(...);
        // mediaPlayer.prepare();

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
        ImageButton btnPlayPause = findViewById(R.id.btn_play_pause);
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                visualizerService.stopVisualizing();
                btnPlayPause.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                visualizerService.startVisualizing();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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