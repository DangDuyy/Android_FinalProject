package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.activities.PlayerActivity;

public class PlayerBar extends ConstraintLayout {
    private static final String TAG = "PlayerBar";
    private ImageView songThumbnail;
    private TextView songTitle;
    private TextView artistName;
    private ImageButton playPauseButton;
    private boolean isPlaying = false;
    private Song currentSong;
    private Context context;

    public PlayerBar(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PlayerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PlayerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.player_bar, this, true);
        
        // Initialize views
        songThumbnail = findViewById(R.id.songThumbnail);
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        playPauseButton = findViewById(R.id.playPauseButton);

        // Set click listener for play/pause button
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        
        // Initially hide the player bar
        setVisibility(GONE);

        // Set click listener for the entire PlayerBar
        setOnClickListener(v -> {
            if (currentSong != null) {
                try {
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("SONG_TITLE", currentSong.getTitle());
                    intent.putExtra("SONG_ARTIST", currentSong.getArtist() != null ? currentSong.getArtist().getName() : "");
                    intent.putExtra("SONG_IMAGE", currentSong.getCoverImage());
                    intent.putExtra("SONG_URL", currentSong.getAudioUrl());
                    Log.d(TAG, "Starting PlayerActivity with URL: " + currentSong.getAudioUrl());
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting PlayerActivity", e);
                }
            } else {
                Log.e(TAG, "No current song available");
            }
        });
    }

    public void setSongInfo(String title, String artist, String imageUrl, String audioUrl, Song song) {
        songTitle.setText(title);
        artistName.setText(artist);
        
        // Load image using Glide
        Glide.with(context)
            .load(imageUrl)
            .centerCrop()
            .into(songThumbnail);
            
        this.currentSong = song;
        // Show the player bar when song info is set
        setVisibility(VISIBLE);
    }

    public void togglePlayPause() {
        isPlaying = !isPlaying;
        updatePlayPauseButton();
        if (onPlayPauseClickListener != null) {
            onPlayPauseClickListener.onPlayPauseClick(isPlaying);
        }
    }

    private void updatePlayPauseButton() {
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    // Interface for play/pause click listener
    public interface OnPlayPauseClickListener {
        void onPlayPauseClick(boolean isPlaying);
    }

    private OnPlayPauseClickListener onPlayPauseClickListener;

    public void setOnPlayPauseClickListener(OnPlayPauseClickListener listener) {
        this.onPlayPauseClickListener = listener;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        updatePlayPauseButton();
    }

    public Song getCurrentSong() {
        return currentSong;
    }
} 