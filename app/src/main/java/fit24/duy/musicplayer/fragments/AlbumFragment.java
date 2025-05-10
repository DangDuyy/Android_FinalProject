package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.AlbumAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.UrlUtils;
import fit24.duy.musicplayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumFragment extends Fragment {
    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private ApiService apiService;
    private String albumTitle, albumImage, artistName, artistImage;
    private Long userId, albumId, artistId;
    private boolean isInLibrary;
    private ImageButton addButton;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.album_content_list);

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

        // Nút Add/Remove Album
        addButton = view.findViewById(R.id.add_button);
        setupAddButton();

        // Nút More
        ImageButton moreButton = view.findViewById(R.id.more_button);
        moreButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("album_title", albumTitle);
            bundle.putString("album_image", albumImage);
            bundle.putLong("album_id", albumId);
            //
            bundle.putLong("artist_id", artistId);
            bundle.putString("artist_name", artistName);
            bundle.putString("artist_image", artistImage);
            try {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_album_control, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            albumTitle = args.getString("album_title");
            albumImage = args.getString("album_image");
            albumId = args.getLong("album_id", -1);
            //
            artistId = args.getLong("artist_id", -1);
            artistName = args.getString("artist_name", "Unknown Artist");
            artistImage = args.getString("artist_image", null);

            if (albumId == -1) {
                Toast.makeText(requireContext(), "Album ID not found", Toast.LENGTH_SHORT).show();
            }

            TextView nameView = view.findViewById(R.id.album_name);
            ImageView imageView = view.findViewById(R.id.album_image);
            nameView.setText(albumTitle);
            String imageUrl = UrlUtils.getImageUrl(albumImage);
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.album_placeholder)
                    .into(imageView);

            // Kiểm tra trạng thái album và xử lý sự kiện nút Add
            checkAlbumInLibrary();
            loadSongsByAlbum(albumTitle);
        }

        return view;
    }

    private void loadSongsByAlbum(String albumTitle) {
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