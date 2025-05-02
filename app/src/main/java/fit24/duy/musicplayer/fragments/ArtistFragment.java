package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.ArtistAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;

public class ArtistFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArtistAdapter songAdapter;
    private ApiService apiService;
    private TextView artistNameView;
    private ImageView artistImageView;
    private String artistName, artistImage;
    private Long userId, artistId;
    private ImageButton followButton;
    private boolean isFollowing;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        recyclerView = view.findViewById(R.id.artist_content_list);

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

        // Nút back
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

        // Khởi tạo nút Follow
        followButton = view.findViewById(R.id.follow_button);
        setupFollowButton();

        // Nút More
        ImageButton moreButton = view.findViewById(R.id.more_button);
        moreButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("artist_name", artistName);
            bundle.putString("artist_image", artistImage);
            bundle.putLong("artist_id", artistId);
            try {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_artist_control, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Nhận dữ liệu được truyền qua
        Bundle args = getArguments();
        if (args != null) {
            artistName = args.getString("artist_name");
            artistImage = args.getString("artist_image");
            artistId = args.getLong("artist_id", -1);
            if (artistId == -1) {
                Toast.makeText(requireContext(), "Artist ID not found", Toast.LENGTH_SHORT).show();
                return view;
            }

            TextView nameView = view.findViewById(R.id.artist_name);
            ImageView imageView = view.findViewById(R.id.artist_image);

            nameView.setText(artistName);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + artistImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageView);

            // Kiểm tra trạng thái follow sau khi artistId được xác nhận
            checkFollowStatus();
            loadSongsByArtist(artistName);
        }

        return view;
    }

    private void setupFollowButton() {
        followButton.setOnClickListener(v -> {
            String TAG = "ArtistFragment-Follow";

            if (isFollowing) {
                // Gọi API unfollow
                Call<ApiResponse<String>> call = apiService.unfollowArtist(artistId, userId);
                call.enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, retrofit2.Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                            isFollowing = false;
                            followButton.setImageResource(R.drawable.ic_follow);
                            Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(requireContext(), "Failed to unfollow: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Gọi API follow
                Call<ApiResponse<String>> call = apiService.followArtist(artistId, userId);
                call.enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, retrofit2.Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                            isFollowing = true;
                            followButton.setImageResource(R.drawable.ic_followed);
                            Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(requireContext(), "Failed to follow: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void checkFollowStatus() {
        if (artistId == null || userId == -1) {
            Toast.makeText(requireContext(), "Invalid artist or user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Boolean>> call = apiService.isArtistFollowed(artistId, userId);
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, retrofit2.Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    isFollowing = response.body().getData();
                    followButton.setImageResource(isFollowing ? R.drawable.ic_followed : R.drawable.ic_follow);
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(requireContext(), "Failed to check follow status: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSongsByArtist(String artistName) {
        Call<List<Song>> call = apiService.getSongsByArtist(artistName);
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, retrofit2.Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    songAdapter = new ArtistAdapter(requireContext(), songs);
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