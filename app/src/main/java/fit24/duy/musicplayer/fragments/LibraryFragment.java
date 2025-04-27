package fit24.duy.musicplayer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.LibraryItemAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.Album;
import fit24.duy.musicplayer.models.Artist;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {
    private RecyclerView recentlyPlayedRecyclerView;
    private LibraryItemAdapter recentlyPlayedAdapter;
    private ApiService apiService;
    private Long userId;

    // Các thành phần giao diện
    private LinearLayout buttonLayout; // Layout chứa 4 nút
    private LinearLayout likedSongsLayout;
    private LinearLayout newEpisodesLayout;
    private LinearLayout filterHeaderLayout; // Layout chứa tiêu đề lọc và nút X
    private TextView filterHeaderText; // Tiêu đề lọc (Playlists, Artists, Albums)
    private TextView btnCloseFilter; // Nút X để đóng lọc
    private Button btnPlaylists, btnArtists, btnAlbums, btnPodcasts;

    private List<Object> allItems = new ArrayList<>(); // Lưu toàn bộ Artists và Albums

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);
        if (userId == -1) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Ánh xạ
        buttonLayout = view.findViewById(R.id.button_layout);
        likedSongsLayout = view.findViewById(R.id.liked_songs_layout);
        newEpisodesLayout = view.findViewById(R.id.new_episodes_layout);
        filterHeaderLayout = view.findViewById(R.id.filter_header_layout);
        filterHeaderText = view.findViewById(R.id.filter_header_text);
        btnCloseFilter = view.findViewById(R.id.btn_close_filter);

        btnPlaylists = view.findViewById(R.id.btn_playlists);
        btnArtists = view.findViewById(R.id.btn_artists);
        btnAlbums = view.findViewById(R.id.btn_albums);
        btnPodcasts = view.findViewById(R.id.btn_podcasts);

        btnPlaylists.setOnClickListener(v -> showPlaylists());
        btnArtists.setOnClickListener(v -> showArtists());
        btnAlbums.setOnClickListener(v -> showAlbums());
        btnPodcasts.setOnClickListener(v -> Toast.makeText(getContext(), "Podcasts - Chưa triển khai", Toast.LENGTH_SHORT).show());

        btnCloseFilter.setOnClickListener(v -> resetFilter());

        // Thiết lập Liked Songs
        ImageView likedSongsImage = view.findViewById(R.id.liked_songs_image);
        TextView likedSongsTitle = view.findViewById(R.id.liked_songs_title);
        TextView likedSongsDescription = view.findViewById(R.id.liked_songs_description);

        likedSongsTitle.setText("Liked Songs");
        likedSongsDescription.setText("\uD83D\uDCCC Playlist • 58 songs");
        Glide.with(this)
                .load(R.drawable.liked_songs)
                .into(likedSongsImage);

        likedSongsLayout.setOnClickListener(v -> Toast.makeText(getContext(), "Liked Songs - Chưa triển khai", Toast.LENGTH_SHORT).show());

        // Thiết lập New Episodes
        ImageView newEpisodesImage = view.findViewById(R.id.new_episodes_image);
        TextView newEpisodesTitle = view.findViewById(R.id.new_episodes_title);
        TextView newEpisodesDescription = view.findViewById(R.id.new_episodes_description);

        newEpisodesTitle.setText("New Episodes");
        newEpisodesDescription.setText("\uD83D\uDCCC Updated 2 days ago");
        Glide.with(this)
                .load(R.drawable.new_episodes)
                .into(newEpisodesImage);

        newEpisodesLayout.setOnClickListener(v -> Toast.makeText(getContext(), "New Episodes - Chưa triển khai", Toast.LENGTH_SHORT).show());

        // Thiết lập RecyclerView
        recentlyPlayedRecyclerView = view.findViewById(R.id.recently_played_recycler_view);
        recentlyPlayedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentlyPlayedAdapter = new LibraryItemAdapter(getContext(), new ArrayList<>());
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);

        // Tải dữ liệu Artists và Albums
        loadRecentlyPlayedItems();

        return view;
    }

    private void loadRecentlyPlayedItems() {
        Call<List<Artist>> artistCall = apiService.getFollowedArtists(userId);
        artistCall.enqueue(new Callback<List<Artist>>() {
            @Override
            public void onResponse(Call<List<Artist>> call, Response<List<Artist>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allItems = new ArrayList<>(response.body());

                    Call<List<Album>> albumCall = apiService.getLibraryAlbums(userId);
                    albumCall.enqueue(new Callback<List<Album>>() {
                        @Override
                        public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                allItems.addAll(response.body());
                                recentlyPlayedAdapter = new LibraryItemAdapter(getContext(), allItems);
                                recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
                            } else {
                                Toast.makeText(getContext(), "Không thể tải danh sách album", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Album>> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách nghệ sĩ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Artist>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị chỉ Liked Songs khi nhấn Playlists
    private void showPlaylists() {
        buttonLayout.setVisibility(View.GONE);
        newEpisodesLayout.setVisibility(View.GONE);
        filterHeaderLayout.setVisibility(View.VISIBLE);
        filterHeaderText.setText("Playlists");
        recentlyPlayedRecyclerView.setVisibility(View.GONE);
    }

    // Hiển thị chỉ Artists khi nhấn Artists
    private void showArtists() {
        buttonLayout.setVisibility(View.GONE);
        likedSongsLayout.setVisibility(View.GONE);
        newEpisodesLayout.setVisibility(View.GONE);
        filterHeaderLayout.setVisibility(View.VISIBLE);
        filterHeaderText.setText("Artists");
        recentlyPlayedRecyclerView.setVisibility(View.VISIBLE);

        // Lọc chỉ hiển thị Artists
        List<Object> artistItems = new ArrayList<>();
        for (Object item : allItems) {
            if (item instanceof Artist) {
                artistItems.add(item);
            }
        }
        recentlyPlayedAdapter = new LibraryItemAdapter(getContext(), artistItems);
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
    }

    // Hiển thị chỉ Albums khi nhấn Albums
    private void showAlbums() {
        buttonLayout.setVisibility(View.GONE);
        likedSongsLayout.setVisibility(View.GONE);
        newEpisodesLayout.setVisibility(View.GONE);
        filterHeaderLayout.setVisibility(View.VISIBLE);
        filterHeaderText.setText("Albums");
        recentlyPlayedRecyclerView.setVisibility(View.VISIBLE);

        // Lọc chỉ hiển thị Albums
        List<Object> albumItems = new ArrayList<>();
        for (Object item : allItems) {
            if (item instanceof Album) {
                albumItems.add(item);
            }
        }
        recentlyPlayedAdapter = new LibraryItemAdapter(getContext(), albumItems);
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
    }

    // Reset giao diện khi nhấn nút X
    private void resetFilter() {
        buttonLayout.setVisibility(View.VISIBLE);
        likedSongsLayout.setVisibility(View.VISIBLE);
        newEpisodesLayout.setVisibility(View.VISIBLE);
        filterHeaderLayout.setVisibility(View.GONE);
        recentlyPlayedRecyclerView.setVisibility(View.VISIBLE);

        // Hiển thị lại toàn bộ danh sách Artists và Albums
        recentlyPlayedAdapter = new LibraryItemAdapter(getContext(), allItems);
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
    }
}