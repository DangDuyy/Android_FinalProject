package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.activities.PlayerActivity;
import fit24.duy.musicplayer.dialogs.QueueDialog;
import fit24.duy.musicplayer.utils.QueueManager;
import fit24.duy.musicplayer.utils.UrlUtils;

public class PlayerBar extends RecyclerView.ViewHolder {
    private static final String TAG = "PlayerBar";
    private ImageView songThumbnail;
    private TextView songTitle;
    private TextView artistName;
    private ImageButton playPauseButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton queueButton;
    private ConstraintLayout playerBarLayout;
    private boolean isPlaying = false;
    private Song currentSong;
    private Context context;
    private QueueManager queueManager;
    private OnPlayPauseClickListener playPauseClickListener;

    public interface OnPlayPauseClickListener {
        void onPlayPauseClick();
        void onPlayNext();
        void onPlayPrevious();
        void onQueueSongSelected(Song song);
    }

    public PlayerBar(@NonNull View itemView, QueueManager queueManager) {
        super(itemView);
        this.context = itemView.getContext();
        this.queueManager = queueManager;
        initializeViews(itemView);
        setupClickListeners();
        
        // Set up queue change listener
        this.queueManager.setOnQueueChangeListener((queue, currentIndex) -> {
            if (currentIndex >= 0 && currentIndex < queue.size()) {
                Song song = queue.get(currentIndex);
                setSongInfo(song);
                isPlaying = queueManager.isPlaying();
                togglePlayPause(isPlaying);
            }
        });

        // Set up playback state listener
        this.queueManager.setPlaybackStateListener(isPlaying -> {
            this.isPlaying = isPlaying;
            togglePlayPause(isPlaying);
        });

        // Initialize with current state
        if (queueManager.getCurrentSong() != null) {
            setSongInfo(queueManager.getCurrentSong());
            isPlaying = queueManager.isPlaying();
            togglePlayPause(isPlaying);
        }
    }

    private void initializeViews(View itemView) {
        songThumbnail = itemView.findViewById(R.id.song_thumbnail);
        songTitle = itemView.findViewById(R.id.song_title);
        artistName = itemView.findViewById(R.id.artist_name);
        playPauseButton = itemView.findViewById(R.id.play_pause_button);
        prevButton = itemView.findViewById(R.id.prev_button);
        nextButton = itemView.findViewById(R.id.next_button);
        queueButton = itemView.findViewById(R.id.queue_button);
        playerBarLayout = (ConstraintLayout) itemView;
    }

    private void setupClickListeners() {
        // Control buttons
        playPauseButton.setOnClickListener(v -> {
            if (playPauseClickListener != null) {
                playPauseClickListener.onPlayPauseClick();
                isPlaying = !isPlaying;
                togglePlayPause(isPlaying);
            }
        });

        prevButton.setOnClickListener(v -> {
            if (playPauseClickListener != null) {
                playPauseClickListener.onPlayPrevious();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (playPauseClickListener != null) {
                playPauseClickListener.onPlayNext();
            }
        });

        queueButton.setOnClickListener(v -> showQueueDialog());

        // Song info container click
        View songInfoContainer = itemView.findViewById(R.id.song_info_container);
        songInfoContainer.setOnClickListener(v -> {
            Song currentSong = queueManager.getCurrentSong();
            if (currentSong != null) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("song", currentSong);
                context.startActivity(intent);
            }
        });

        // Enable click events for control buttons
        playPauseButton.setClickable(true);
        playPauseButton.setFocusable(true);
        prevButton.setClickable(true);
        prevButton.setFocusable(true);
        nextButton.setClickable(true);
        nextButton.setFocusable(true);
        queueButton.setClickable(true);
        queueButton.setFocusable(true);

        // Make sure the buttons are not covered by other views
        playPauseButton.bringToFront();
        prevButton.bringToFront();
        nextButton.bringToFront();
        queueButton.bringToFront();

        // Disable click events for individual song info elements
        songThumbnail.setClickable(false);
        songThumbnail.setFocusable(false);
        songTitle.setClickable(false);
        songTitle.setFocusable(false);
        artistName.setClickable(false);
        artistName.setFocusable(false);
    }

    public void setPlayPauseClickListener(OnPlayPauseClickListener listener) {
        this.playPauseClickListener = listener;
    }

    public void setSongInfo(Song song) {
        if (song != null) {
            songTitle.setText(song.getTitle());
            artistName.setText(song.getArtistName());
            String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
            Glide.with(songThumbnail.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_play)
                    .error(R.drawable.ic_play)
                    .into(songThumbnail);
            playerBarLayout.setVisibility(View.VISIBLE);
            this.currentSong = song;
            isPlaying = queueManager.isPlaying();
            togglePlayPause(isPlaying);
        }
    }

    public void togglePlayPause(boolean isPlaying) {
        this.isPlaying = isPlaying;
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    public void showQueueDialog() {
        if (context instanceof androidx.fragment.app.FragmentActivity) {
            QueueDialog dialog = QueueDialog.newInstance(queueManager.getQueue(), queueManager.getCurrentIndex());
            dialog.setOnQueueItemClickListener(new QueueDialog.OnQueueItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    queueManager.setCurrentIndex(position);
                    if (playPauseClickListener != null) {
                        Song song = queueManager.getQueue().get(position);
                        playPauseClickListener.onQueueSongSelected(song);
                    }
                }
                @Override
                public void onItemRemoved(int position) {}
            });
            dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "QueueDialog");
        }
    }

    public void addToQueue(Song song) {
        queueManager.addSong(song);
    }

    public Song getCurrentSong() {
        return currentSong;
    }
} 