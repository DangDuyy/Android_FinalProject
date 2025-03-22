package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import fit24.duy.musicplayer.R;

public class ProfileFragment extends Fragment {
    private TextView usernameText;
    private TextView emailText;
    private TextView totalSongsText;
    private TextView totalPlaylistsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        totalSongsText = view.findViewById(R.id.total_songs_text);
        totalPlaylistsText = view.findViewById(R.id.total_playlists_text);

        // Set dummy data
        usernameText.setText("John Doe");
        emailText.setText("john.doe@example.com");
        totalSongsText.setText("125 songs");
        totalPlaylistsText.setText("8 playlists");

        return view;
    }
} 