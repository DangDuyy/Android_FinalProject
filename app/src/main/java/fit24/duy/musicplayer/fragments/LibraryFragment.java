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

public class LibraryFragment extends Fragment {
    private RecyclerView playlistsRecyclerView;
    private RecyclerView savedSongsRecyclerView;
    private MusicAdapter playlistsAdapter;
    private MusicAdapter savedSongsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        playlistsRecyclerView = view.findViewById(R.id.playlists_recycler_view);
        savedSongsRecyclerView = view.findViewById(R.id.saved_songs_recycler_view);

        // Set up playlists RecyclerView
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        playlistsAdapter = new MusicAdapter(getDummyPlaylists());
        playlistsRecyclerView.setAdapter(playlistsAdapter);

        // Set up saved songs RecyclerView
        savedSongsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedSongsAdapter = new MusicAdapter(getDummySavedSongs());
        savedSongsRecyclerView.setAdapter(savedSongsAdapter);

        return view;
    }

    private List<Song> getDummyPlaylists() {
        List<Song> playlists = new ArrayList<>();
        playlists.add(new Song("Favorite Songs", "15 songs", R.drawable.album_placeholder));
        playlists.add(new Song("Workout Mix", "20 songs", R.drawable.album_placeholder));
        playlists.add(new Song("Chill Vibes", "25 songs", R.drawable.album_placeholder));
        return playlists;
    }

    private List<Song> getDummySavedSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Saved Song 1", "Artist 1", R.drawable.album_placeholder));
        songs.add(new Song("Saved Song 2", "Artist 2", R.drawable.album_placeholder));
        songs.add(new Song("Saved Song 3", "Artist 3", R.drawable.album_placeholder));
        return songs;
    }
} 