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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private String currentSongUrl; // thêm vào đầu class

    private ImageButton btnPlayPause, btnNext, btnPrev, btnMenu, btnLike, btnBack;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration, tvLyrics, tvSongTitle, tvArtist;
    private ImageView imgAlbumArt;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private MediaPlayer mediaPlayer;
    private SessionManager sessionManager;
    private ApiService apiService;
    private Long userId;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        initializeViews();
        setupClickListeners();

        long songId = getIntent().getLongExtra("song_id", -1);
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
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
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

        btnMenu.setOnClickListener(v -> showMenu());
        btnLike.setOnClickListener(v -> {
            if (isLiked) unlikeSong();
            else likeSong();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void showMenu() {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenuDark);
        PopupMenu popupMenu = new PopupMenu(wrapper, btnMenu);
        popupMenu.getMenuInflater().inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_download) {
                Log.d("DEBUG", "User status = " + sessionManager.getUserStatus());
                if (sessionManager.getUserStatus() == 1) {
                    if (mediaPlayer != null && currentSongUrl != null) {
                        downloadSong(currentSongUrl, tvSongTitle.getText().toString());
                    } else {
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

    private void fetchSongInfo(long songId) {
        apiService.getSongById(songId).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Song song = response.body();
                    tvSongTitle.setText(song.getTitle());
                    tvArtist.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
                    tvLyrics.setText(song.getLyrics());
                    Glide.with(PlayerActivity.this)
                            .load(UrlUtils.getImageUrl(song.getCoverImage()))
                            .centerCrop()
                            .into(imgAlbumArt);

                    currentSongUrl = BASE_URL + "uploads/" + song.getFilePath();
                    playSong(song.getFilePath());
                    checkSongLiked(song.getId());
                } else {
                    Toast.makeText(PlayerActivity.this, "Failed to load song info", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Song> call, Throwable t) {
                Toast.makeText(PlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playSong(String filePath) {
        try {
            String fullUrl = UrlUtils.getAudioUrl(filePath);
            if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
            else mediaPlayer.reset();

            mediaPlayer.setDataSource(fullUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            seekBar.setMax(mediaPlayer.getDuration());
            tvDuration.setText(formatTime(mediaPlayer.getDuration()));
            updateSeekBar();
        } catch (Exception e) {
            Log.e(TAG, "Play error", e);
            Toast.makeText(this, "Cannot play this song", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSeekBar() {
        if (updateSeekBarRunnable != null) handler.removeCallbacks(updateSeekBarRunnable);
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int pos = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(pos);
                    tvCurrentTime.setText(formatTime(pos));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    private String formatTime(int milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60);
    }

    private void checkSongLiked(long songId) {
        apiService.isSongLiked(songId, userId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isLiked = response.body().getData();
                    btnLike.setImageResource(isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "Check liked error", t);
            }
        });
    }

    private void likeSong() {
        apiService.likeSong(getIntent().getLongExtra("song_id", -1), userId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    isLiked = true;
                    btnLike.setImageResource(R.drawable.ic_heart_red);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Like error", t);
            }
        });
    }

    private void unlikeSong() {
        apiService.unlikeSong(getIntent().getLongExtra("song_id", -1), userId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    isLiked = false;
                    btnLike.setImageResource(R.drawable.ic_heart);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Unlike error", t);
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
        if (updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }
}
