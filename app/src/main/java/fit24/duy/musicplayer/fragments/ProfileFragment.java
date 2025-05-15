package fit24.duy.musicplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.activities.MainActivity;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.SessionManager;
import fit24.duy.musicplayer.utils.UrlUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView usernameText;
    private TextView emailText;
    private TextView totalSongsText;
    private TextView totalPlaylistsText;
    private ImageButton menuButton;
    private SessionManager sessionManager;
    private Button editProfileButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize SessionManager
        sessionManager = new SessionManager(requireActivity());

        // Initialize views
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        totalSongsText = view.findViewById(R.id.total_songs_text);
        totalPlaylistsText = view.findViewById(R.id.total_playlists_text);
        menuButton = view.findViewById(R.id.menu_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);

        // Load user data from SessionManager
        String username = sessionManager.getUsername() != null ? sessionManager.getUsername() : "Unknown";
        String email = sessionManager.getEmail() != null ? sessionManager.getEmail() : "No email";
        String profileImage = sessionManager.getProfileImage();

        // Update UI
        usernameText.setText(username);
        emailText.setText(email);
        totalSongsText.setText("0");
        totalPlaylistsText.setText("0");

        // Set menu button click listener
        menuButton.setOnClickListener(v -> showPopupMenu(v));

        // Gọi API để lấy số lượng bài hát đã like
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String userId = sessionManager.getUserId(); // đảm bảo bạn đã lưu userId khi login
        long userIdLong = Long.parseLong(userId);

        Call<List<Song>> likedSongsCall = apiService.getLikedSongs(userIdLong);
        likedSongsCall.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int songCount = response.body().size();
                    totalSongsText.setText(String.valueOf(songCount));
                } else {
                    totalSongsText.setText("0");
                    Log.e("ProfileFragment", "Không thể tải số bài hát đã thích");
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                totalSongsText.setText("0");
                Log.e("ProfileFragment", "Lỗi khi tải liked songs: " + t.getMessage());
            }
        });


        // Set edit profile button click listener
        editProfileButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.navigation_edit_profile);
        });

        // load ảnh
        String imageUrl = UrlUtils.getImageUrl(profileImage);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .centerCrop()
                .into((ImageView) view.findViewById(R.id.profile_image));

        return view;
    }

    private void showPopupMenu(View view) {
        // Tạo ContextThemeWrapper với theme PopupMenuDark
        Context context = new ContextThemeWrapper(requireContext(), R.style.PopupMenuDark);
        // Sử dụng context đã bao bọc để tạo PopupMenu
        PopupMenu popup = new PopupMenu(context, view); // Sửa ở đây
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_logout) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
                return true;
            }
            return false;
        });

        popup.show();
    }
}