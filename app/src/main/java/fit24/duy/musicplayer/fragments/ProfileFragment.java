package fit24.duy.musicplayer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.activities.MainActivity;
import fit24.duy.musicplayer.utils.SessionManager;

public class ProfileFragment extends Fragment {
    private TextView usernameText;
    private TextView emailText;
    private TextView totalSongsText;
    private TextView totalPlaylistsText;
    private ImageButton menuButton;
    private SessionManager sessionManager;

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

        // Load user data from SessionManager
        String username = sessionManager.getUsername() != null ? sessionManager.getUsername() : "Unknown";
        String email = sessionManager.getEmail() != null ? sessionManager.getEmail() : "No email";
        // Update UI
        usernameText.setText(username);
        emailText.setText(email);
        totalSongsText.setText("0");
        totalPlaylistsText.setText("0");

        // Set menu button click listener
        menuButton.setOnClickListener(v -> showPopupMenu(v));

        return view;
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
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