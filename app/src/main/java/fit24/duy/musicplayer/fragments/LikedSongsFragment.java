package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.LikedSongsAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikedSongsFragment extends Fragment {
    private RecyclerView likedSongsRecyclerView;
    private LikedSongsAdapter likedSongsAdapter;
    private TextView likedSongsCount;
    private ApiService apiService;
    private Long userId;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liked_songs, container, false);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(requireContext());

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy userId từ SessionManager
        String userIdString = sessionManager.getUserId();
        if (userIdString == null || userIdString.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        try {
            userId = Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Ánh xạ
        likedSongsCount = view.findViewById(R.id.liked_songs_count);
        likedSongsRecyclerView = view.findViewById(R.id.liked_songs_recycler_view);
        likedSongsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        likedSongsAdapter = new LikedSongsAdapter(getContext(), new ArrayList<>());
        likedSongsRecyclerView.setAdapter(likedSongsAdapter);

        // Tải danh sách bài hát đã like
        loadLikedSongs();

        // Nút Back
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp();
        });

        return view;
    }

    private void loadLikedSongs() {
        Call<List<Song>> likedSongsCall = apiService.getLikedSongs(userId);
        likedSongsCall.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    likedSongsCount.setText(songs.size() + " songs");
                    likedSongsAdapter = new LikedSongsAdapter(getContext(), songs);
                    likedSongsRecyclerView.setAdapter(likedSongsAdapter);
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách bài hát đã thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}