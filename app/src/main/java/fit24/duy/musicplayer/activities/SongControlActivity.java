package fit24.duy.musicplayer.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;

public class SongControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_control);

        // Lấy dữ liệu từ Intent
        String songTitle = getIntent().getStringExtra("song_title");
        String artistName = getIntent().getStringExtra("artist_name");
        String albumArtUrl = getIntent().getStringExtra("album_art_url");

        // Gán dữ liệu vào view
        TextView tvSongTitle = findViewById(R.id.tv_song_title);
        TextView tvArtist = findViewById(R.id.tv_artist);
        ImageView imgAlbumArt = findViewById(R.id.img_album_art);

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
    }
}