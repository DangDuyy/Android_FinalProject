package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.MusicAdapter;
import fit24.duy.musicplayer.models.Song;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recentlyPlayedRecyclerView;
    private RecyclerView recommendedRecyclerView;
    private MusicAdapter recentlyPlayedAdapter;
    private MusicAdapter recommendedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        recentlyPlayedRecyclerView = view.findViewById(R.id.recently_played_recycler_view);
        recommendedRecyclerView = view.findViewById(R.id.recommended_recycler_view);

        // Set up layouts
        recentlyPlayedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize adapters
        recentlyPlayedAdapter = new MusicAdapter(getDummyRecentlyPlayed());
        recommendedAdapter = new MusicAdapter(getDummyRecommended());

        // Set adapters
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        return view;
    }

    private List<Song> getDummyRecentlyPlayed() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Song 1", "Artist 1", R.drawable.album_placeholder));
        songs.add(new Song("Song 2", "Artist 2", R.drawable.album_placeholder));
        songs.add(new Song("Song 3", "Artist 3", R.drawable.album_placeholder));
        return songs;
    }

    private List<Song> getDummyRecommended() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Recommended 1", "Artist 1", R.drawable.album_placeholder));
        songs.add(new Song("Recommended 2", "Artist 2", R.drawable.album_placeholder));
        songs.add(new Song("Recommended 3", "Artist 3", R.drawable.album_placeholder));
        return songs;
    }
} 