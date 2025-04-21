package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.ArtistSongAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.Song;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArtistSongAdapter songAdapter;
    private ApiService apiService;
    private TextView artistNameView;
    private ImageView artistImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        recyclerView = view.findViewById(R.id.artist_content_list);

        // Nút back
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp(); // Quay lại trang trước
        });

        ImageButton playButton = view.findViewById(R.id.play_button);

        // Biến lưu trạng thái đang phát hay không
        final boolean[] isPlaying = {false};

        playButton.setOnClickListener(v -> {
            if (isPlaying[0]) {
                // Đang phát, giờ chuyển thành dừng => đổi về nút Play
                playButton.setImageResource(R.drawable.ic_play);
                isPlaying[0] = false;

                // TODO: Thêm code dừng phát nhạc nếu có
            } else {
                // Đang dừng, giờ phát => đổi về nút Pause
                playButton.setImageResource(R.drawable.ic_pause);
                isPlaying[0] = true;

                // TODO: Thêm code phát nhạc nếu có
            }
        });


        // Nhận dữ liệu được truyền qua
        Bundle args = getArguments();
        if (args != null) {
            String artistName = args.getString("artist_name");
            String artistImage = args.getString("artist_image");

            TextView nameView = view.findViewById(R.id.artist_name);
            ImageView imageView = view.findViewById(R.id.artist_image);

            nameView.setText(artistName);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + artistImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageView);

            // Gọi API để lấy danh sách bài hát của nghệ sĩ
            loadSongsByArtist(artistName);
        }

        return view;
    }

    private void loadSongsByArtist(String artistName) {
        apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Song>> call = apiService.getSongsByArtist(artistName);
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    songAdapter = new ArtistSongAdapter(requireContext(), songs);
                    recyclerView.setAdapter(songAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
