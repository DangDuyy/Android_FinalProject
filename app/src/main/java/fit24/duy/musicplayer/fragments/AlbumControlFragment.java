package fit24.duy.musicplayer.fragments;

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
import fit24.duy.musicplayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumControlFragment extends Fragment {
    private ApiService apiService;
    private Long userId, albumId, artistId;
    private String albumTitle, albumImage, artistName, artistImage;
    private boolean isInLibrary;
    private ImageButton addButton;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_control, container, false);

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

        // Nút Add/Remove Album
        addButton = view.findViewById(R.id.add_button);
        setupAddButton();

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            albumTitle = args.getString("album_title");
            albumImage = args.getString("album_image");
            albumId = args.getLong("album_id", -1);
            //
            artistName = args.getString("artist_name", "Unknown Artist");
            artistImage = args.getString("artist_image", null);
            artistId = args.getLong("artist_id", -1);

            if (albumId == -1) {
                Toast.makeText(requireContext(), "Album ID not found", Toast.LENGTH_SHORT).show();
            }

            // Hiển thị thông tin album
            TextView albumTitleControl = view.findViewById(R.id.album_title_control);
            ImageView albumImageControl = view.findViewById(R.id.album_image_control);
            TextView artistNameControl = view.findViewById(R.id.artist_name_control);

            albumTitleControl.setText(albumTitle);
            artistNameControl.setText(artistName);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + albumImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .into(albumImageControl);

            // Kiểm tra trạng thái album và xử lý sự kiện nút Add
            checkAlbumInLibrary();

            // Xử lý nhấn "View artist"
            view.findViewById(R.id.option_view_artist).setOnClickListener(v -> {
                if (artistId == -1) {
                    Toast.makeText(requireContext(), "Artist information not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("artist_name", artistName);
                bundle.putString("artist_image", artistImage);
                bundle.putLong("artist_id", artistId);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_artist, bundle);
            });
        }

        // Xử lý các tùy chọn
        view.findViewById(R.id.option_share).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Share", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_like_all).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Like all songs", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_add_to_playlist).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Add to playlist", Toast.LENGTH_SHORT).show();
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

    private void setupAddButton() {
        addButton.setOnClickListener(v -> {
            if (isInLibrary) {
                // Xóa album khỏi thư viện
                Call<ApiResponse<String>> call = apiService.removeAlbumFromLibrary(albumId, userId);
                call.enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                            isInLibrary = false;
                            addButton.setImageResource(R.drawable.ic_add_album);
                            Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(requireContext(), "Failed to remove: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Thêm album vào thư viện
                Call<ApiResponse<String>> call = apiService.addAlbumToLibrary(albumId, userId);
                call.enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                            isInLibrary = true;
                            addButton.setImageResource(R.drawable.ic_check_green);
                            Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(requireContext(), "Failed to add: " + errorMsg, Toast.LENGTH_SHORT).show();
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

    private void checkAlbumInLibrary() {
        if (albumId == null || userId == -1) {
            Toast.makeText(requireContext(), "Invalid album or user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Boolean>> call = apiService.isAlbumInLibrary(albumId, userId);
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    isInLibrary = response.body().getData();
                    addButton.setImageResource(isInLibrary ? R.drawable.ic_check_green : R.drawable.ic_add_album);
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(requireContext(), "Failed to check library status: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}