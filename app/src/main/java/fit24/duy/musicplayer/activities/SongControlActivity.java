package fit24.duy.musicplayer.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongControlActivity extends AppCompatActivity {

    private static final String TAG = "SongControlActivity";

    private Long userId, songId;
    private boolean isLiked = false;
    private ApiService apiService;
    private SessionManager sessionManager;
    private LinearLayout optionLike, optionViewAlbum, optionViewArtist;
    private ImageView likeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_control);

        // Khởi tạo SessionManager và ApiService
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy userId từ SessionManager
        String userIdString = sessionManager.getUserId();
        if (userIdString == null || userIdString.isEmpty()) {
            Log.e(TAG, "UserIdString is null or empty");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            userId = Long.parseLong(userIdString);
            Log.d(TAG, "User ID parsed: " + userId);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid user ID format: " + userIdString, e);
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy dữ liệu từ Intent
        songId = getIntent().getLongExtra("song_id", -1); // Lấy songId từ Intent
        if (songId == -1) {
            Log.e(TAG, "Song ID not provided or invalid in Intent");
            Toast.makeText(this, "Invalid song data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String songTitle = getIntent().getStringExtra("song_title");
        String artistName = getIntent().getStringExtra("artist_name");
        String albumArtUrl = getIntent().getStringExtra("album_art_url");

        // Gán dữ liệu vào view
        TextView tvSongTitle = findViewById(R.id.tv_song_title);
        TextView tvArtist = findViewById(R.id.tv_artist);
        ImageView imgAlbumArt = findViewById(R.id.img_album_art);
        optionLike = findViewById(R.id.option_like);
        optionViewAlbum = findViewById(R.id.option_view_album);
        optionViewArtist = findViewById(R.id.option_view_artist);

        // Lấy ImageView trong option_like để thay đổi icon
        likeIcon = (ImageView) optionLike.getChildAt(0);

        tvSongTitle.setText(songTitle != null ? songTitle : "Unknown Title");
        tvArtist.setText(artistName != null ? artistName : "Unknown Artist");

        if (albumArtUrl != null) {
            Glide.with(this)
                    .load(albumArtUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imgAlbumArt);
        }

        // Xử lý nút Close
        TextView btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());

        // Xử lý sự kiện nhấn vào option_like
        optionLike.setOnClickListener(v -> {
            if (isLiked) {
                unlikeSong();
            } else {
                likeSong();
            }
        });

        // Kiểm tra trạng thái Like khi khởi tạo
        checkSongLiked();
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
                    updateLikeIcon();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(SongControlActivity.this, "Failed to check like status: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(SongControlActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    updateLikeIcon();
                    Toast.makeText(SongControlActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(SongControlActivity.this, "Failed to like: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(SongControlActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    updateLikeIcon();
                    Toast.makeText(SongControlActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(SongControlActivity.this, "Failed to unlike: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(SongControlActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeIcon() {
        if (likeIcon != null) {
            likeIcon.setImageResource(isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart);
        }
    }
}