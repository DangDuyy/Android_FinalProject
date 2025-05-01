package fit24.duy.musicplayer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.activities.MainActivity;

public class ProfileFragment extends Fragment {
    private TextView usernameText;
    private TextView emailText;
    private TextView totalSongsText;
    private TextView totalPlaylistsText;
    private ImageButton logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        totalSongsText = view.findViewById(R.id.total_songs_text);
        totalPlaylistsText = view.findViewById(R.id.total_playlists_text);
        logoutButton = view.findViewById(R.id.logout_button);

        // Load user data from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        String username = prefs.getString("username", "Unknown");
        String email = prefs.getString("email", "No email");

        // Update UI
        usernameText.setText(username);
        emailText.setText(email);
        totalSongsText.setText("0");
        totalPlaylistsText.setText("0");

        // Set logout button click listener
        logoutButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });

        return view;
    }
}