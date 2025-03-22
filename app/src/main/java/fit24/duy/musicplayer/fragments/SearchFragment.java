package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
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

public class SearchFragment extends Fragment {
    private SearchView searchView;
    private RecyclerView searchResultsRecyclerView;
    private MusicAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.search_view);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);

        // Set up RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new MusicAdapter(new ArrayList<>());
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        return view;
    }

    private void performSearch(String query) {
        // This is a dummy search implementation
        // In a real app, you would search your music database
        List<Song> searchResults = new ArrayList<>();
        if (!query.isEmpty()) {
            searchResults.add(new Song("Search Result 1: " + query, "Artist 1", R.drawable.album_placeholder));
            searchResults.add(new Song("Search Result 2: " + query, "Artist 2", R.drawable.album_placeholder));
            searchResults.add(new Song("Search Result 3: " + query, "Artist 3", R.drawable.album_placeholder));
        }
        searchAdapter = new MusicAdapter(searchResults);
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }
} 