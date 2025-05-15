package fit24.duy.musicplayer.activities;

import static fit24.duy.musicplayer.models.Song.BASE_URL;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.SessionManager;
import fit24.duy.musicplayer.utils.UrlUtils;
import fit24.duy.musicplayer.visualizer.MusicVisualizerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import fit24.duy.musicplayer.dialogs.QueueDialog;
import fit24.duy.musicplayer.visualizer.VisualizerService;
import fit24.duy.musicplayer.utils.QueueManager;

import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private String currentSongUrl; // thêm vào đầu class

    private ImageButton btnPlayPause, btnNext, btnPrev, btnMenu, btnLike, btnBack, btnQueue;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration, tvLyrics, tvSongTitle, tvArtist;
    private ImageView imgAlbumArt;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private boolean isLiked = false;
    private QueueManager queueManager;
    private Runnable updateSeekBarRunnable;
    private MediaPlayer mediaPlayer;
    private SessionManager sessionManager;
    private ApiService apiService;
    private Long userId, songId;


    // Visualizer variables
    private MusicVisualizerView visualizerView;
    private VisualizerService visualizerService;
    private boolean isWaveform = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

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

        songId = getIntent().getLongExtra("song_id", -1);
        if (songId == -1) {
            Toast.makeText(this, "Invalid song", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            userId = Long.parseLong(sessionManager.getUserId());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchSongInfo(songId);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        btnPrev = findViewById(R.id.btn_prev);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnNext = findViewById(R.id.btn_next);
        btnMenu = findViewById(R.id.btn_menu);
        btnLike = findViewById(R.id.btn_like);
        btnBack = findViewById(R.id.btn_back);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvLyrics = findViewById(R.id.tv_lyrics);
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_artist);
        imgAlbumArt = findViewById(R.id.img_album_art);
        btnQueue = findViewById(R.id.queue_button);
        visualizerView = findViewById(R.id.visualizer);


        sessionManager = new SessionManager(this);

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy userId từ SessionManager
        String userIdString = sessionManager.getUserId();
        Log.d(TAG, "User ID: " + sessionManager.getUserId());
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

        btnPrev.setOnClickListener(v -> {
            queueManager.playPrevious();
            updateSongInfoAndSeekBar();
        });

        btnPlayPause.setOnClickListener(v -> {
            MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    queueManager.pause();
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    queueManager.play();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            queueManager.playNext();
            updateSongInfoAndSeekBar();
        });

        btnQueue.setOnClickListener(v -> {
            if (queueManager.getQueue().isEmpty()) {
                queueManager.fillQueueWithRandomSongs(10);
            }
            openQueueDialog();
        });

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
                if (fromUser) {
                    MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
                    if (mediaPlayer != null && (mediaPlayer.isPlaying() || mediaPlayer.getCurrentPosition() > 0)) {
                        try {
                            mediaPlayer.seekTo(progress);
                            tvCurrentTime.setText(formatTime(progress));
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Cannot seek, MediaPlayer not prepared", e);
                        }
                    } else {
                        Log.w(TAG, "Cannot seek, MediaPlayer not ready");
                    }
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

        btnMenu.setOnClickListener(v -> showMenu());

    }

    private void showMenu() {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenuDark);
        PopupMenu popupMenu = new PopupMenu(wrapper, btnMenu);
        popupMenu.getMenuInflater().inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_download) {
                Log.d("DEBUG", "User status = " + sessionManager.getUserStatus());
                if (sessionManager.getUserStatus() == 1) {
                    if (currentSongUrl != null && !currentSongUrl.isEmpty()) {
                        downloadSong(currentSongUrl, tvSongTitle.getText().toString());
                    } else {
                        Log.e(TAG, "Download failed: Song URL is null or empty.");
                        Toast.makeText(this, "No song selected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Premium Required")
                            .setMessage("Upgrade to Premium to download this song?")
                            .setPositiveButton("Upgrade", (d, w) -> startActivity(new Intent(this, PaymentActivity.class)))
                            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                            .show();
                }
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void downloadSong(String songUrl, String songTitle) {
        if (songUrl == null || songUrl.isEmpty()) {
            Toast.makeText(this, "Invalid song URL", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(songUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(songTitle);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, songTitle + ".mp3");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        dm.enqueue(request);
        Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();
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
            currentSongUrl = song.getAudioUrl();

            tvSongTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
            String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(imgAlbumArt);

            // Kiểm tra trạng thái MediaPlayer
            try {
                if (mediaPlayer.isPlaying() || mediaPlayer.getCurrentPosition() > 0) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    tvDuration.setText(formatTime(mediaPlayer.getDuration()));
                    seekBar.setEnabled(true); // Kích hoạt seekBar
                    updateSeekBar();
                } else {
                    // Đợi MediaPlayer sẵn sàng
                    mediaPlayer.setOnPreparedListener(mp -> {
                        seekBar.setMax(mp.getDuration());
                        tvDuration.setText(formatTime(mp.getDuration()));
                        seekBar.setEnabled(true); // Kích hoạt seekBar
                        mp.start();
                        updateSeekBar();
                        btnPlayPause.setImageResource(R.drawable.ic_pause);
                    });
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "MediaPlayer not prepared yet", e);
                mediaPlayer.setOnPreparedListener(mp -> {
                    seekBar.setMax(mp.getDuration());
                    tvDuration.setText(formatTime(mp.getDuration()));
                    seekBar.setEnabled(true); // Kích hoạt seekBar
                    mp.start();
                    updateSeekBar();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                });
            }
            btnPlayPause.setImageResource(mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            seekBar.setEnabled(false); // Vô hiệu hóa seekBar nếu không có bài hát
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
                    try {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        tvCurrentTime.setText(formatTime(currentPosition));
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Cannot update seekBar, MediaPlayer not ready", e);
                    }
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

    private void fetchSongInfo(long songId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getSongById(songId).enqueue(new retrofit2.Callback<Song>() {
            @Override
            public void onResponse(retrofit2.Call<Song> call, retrofit2.Response<Song> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Song song = response.body();
                    Log.d(TAG, "API filePath: " + song.getFilePath());
                    tvSongTitle.setText(song.getTitle());
                    tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
                    tvLyrics.setText(song.getLyrics());
                    Glide.with(PlayerActivity.this)
                            .load(UrlUtils.getImageUrl(song.getCoverImage()))
                            .centerCrop()
                            .into(imgAlbumArt);
                    queueManager.addSong(song);
                    queueManager.setCurrentIndex(queueManager.getQueue().size() - 1);
                    queueManager.play();
                    // Cập nhật giao diện sau khi MediaPlayer sẵn sàng
                    MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
                    if (mediaPlayer != null) {
                        mediaPlayer.setOnPreparedListener(mp -> {
                            updateSongInfoAndSeekBar();
                        });
                    }
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

    private void updateUI(Song song) {
        if (song == null) return;

        tvSongTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");

        String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imgAlbumArt);

        MediaPlayer mediaPlayer = queueManager.getMediaPlayer();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying() || mediaPlayer.getCurrentPosition() > 0) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    tvDuration.setText(formatTime(mediaPlayer.getDuration()));
                    seekBar.setEnabled(true); // Kích hoạt seekBar
                    updateSeekBar();
                } else {
                    mediaPlayer.setOnPreparedListener(mp -> {
                        seekBar.setMax(mp.getDuration());
                        tvDuration.setText(formatTime(mp.getDuration()));
                        seekBar.setEnabled(true); // Kích hoạt seekBar
                        mp.start();
                        updateSeekBar();
                        btnPlayPause.setImageResource(R.drawable.ic_pause);
                    });
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "MediaPlayer not prepared yet", e);
                mediaPlayer.setOnPreparedListener(mp -> {
                    seekBar.setMax(mp.getDuration());
                    tvDuration.setText(formatTime(mp.getDuration()));
                    seekBar.setEnabled(true); // Kích hoạt seekBar
                    mp.start();
                    updateSeekBar();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                });
            }
            btnPlayPause.setImageResource(mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            seekBar.setEnabled(false); // Vô hiệu hóa seekBar nếu không có MediaPlayer
        }

        songId = song.getId();
        checkSongLiked();
    }

}