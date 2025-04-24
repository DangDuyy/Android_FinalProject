package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.AlbumAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.Song;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumFragment extends Fragment {
    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private ApiService apiService;
    private String albumTitle;
    private String albumImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false); // Dùng lại layout artist
        recyclerView = view.findViewById(R.id.album_content_list);

        // Nút Back
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp();
        });

        // Nút Play/Pause
        ImageButton playButton = view.findViewById(R.id.play_button);
        final boolean[] isPlaying = {false};
        playButton.setOnClickListener(v -> {
            if (isPlaying[0]) {
                playButton.setImageResource(R.drawable.ic_play);
                isPlaying[0] = false;
            } else {
                playButton.setImageResource(R.drawable.ic_pause);
                isPlaying[0] = true;
            }
        });


        // Nút More
        ImageButton moreButton = view.findViewById(R.id.more_button);
        moreButton.setOnClickListener(v -> {
            // Chuẩn bị dữ liệu để gửi đến AlbumControlFragment
            Bundle bundle = new Bundle();
            bundle.putString("album_title", albumTitle);
            bundle.putString("album_image", albumImage);

            // Điều hướng đến AlbumControlFragment
            try {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_album_control, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            albumTitle = args.getString("album_title");
            albumImage = args.getString("album_image");

            TextView nameView = view.findViewById(R.id.album_name);
            ImageView imageView = view.findViewById(R.id.album_image);

            nameView.setText(albumTitle);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + albumImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .into(imageView);

            loadSongsByAlbum(albumTitle);
        }

        // hiện thanh search + sort

        return view;
    }

    private void loadSongsByAlbum(String albumTitle) {
        apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Song>> call = apiService.getSongsByAlbum(albumTitle);
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    albumAdapter = new AlbumAdapter(requireContext(), response.body());
                    recyclerView.setAdapter(albumAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
