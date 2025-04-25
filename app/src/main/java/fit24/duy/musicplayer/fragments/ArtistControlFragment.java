package fit24.duy.musicplayer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;

public class ArtistControlFragment extends Fragment {
    private String artistName, artistImage;
    private Long userId, artistId;
    private ApiService apiService;
    private ImageButton followButton;
    private boolean isFollowing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_control, container, false);

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            artistName = args.getString("artist_name");
            artistImage = args.getString("artist_image");
            artistId = args.getLong("artist_id", -1);
            if (artistId == -1) {
                Toast.makeText(requireContext(), "Artist ID not found", Toast.LENGTH_SHORT).show();
            }

            // Hiển thị thông tin nghệ sĩ
            TextView artistNameControl = view.findViewById(R.id.artist_name_control);
            ImageView artistImageControl = view.findViewById(R.id.artist_image_control);

            artistNameControl.setText(artistName);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + artistImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(artistImageControl);

            // Kiểm tra trạng thái follow sau khi artistId được xác nhận
            checkFollowStatus();
        }

        // Khởi tạo nút Follow
        followButton = view.findViewById(R.id.follow_button);
        setupFollowButton();

        // Xử lý các tùy chọn
        view.findViewById(R.id.option_follow_artist).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Follow artist", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_do_not_play).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Do not play this artist", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_share).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Share", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_add_to_queue).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Add to queue", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_go_to_radio).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Go to radio", Toast.LENGTH_SHORT).show();
        });

        // Nút Close
        view.findViewById(R.id.btn_close).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp();
        });

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
}