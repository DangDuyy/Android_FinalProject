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
import fit24.duy.musicplayer.utils.UrlUtils;

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
        songThumbnail = findViewById(R.id.song_thumbnail);
        songTitle = findViewById(R.id.song_title);
        artistName = findViewById(R.id.artist_name);
        playPauseButton = findViewById(R.id.play_pause_button);

        // Set click listener for play/pause button
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        
        // Initially hide the player bar
        setVisibility(GONE);

        // Set click listener for the entire PlayerBar
        setOnClickListener(v -> {
            if (currentSong != null) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("song", currentSong); // Pass the entire song object
                context.startActivity(intent);
            }
        });
    }

    public void setSongInfo(String title, String artist, String imageUrl, String audioUrl, Song song) {
        songTitle.setText(title);
        artistName.setText(artist);
        
        // Load image using Glide with UrlUtils
        String processedImageUrl = UrlUtils.getImageUrl(imageUrl);
        Glide.with(context)
            .load(processedImageUrl)
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