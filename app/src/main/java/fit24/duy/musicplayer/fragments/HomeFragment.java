package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.util.Log;
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
import fit24.duy.musicplayer.adapters.PlayerBar;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.Song;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recentlyPlayedRecyclerView;
    private RecyclerView recommendedRecyclerView;
    private MusicAdapter recentlyPlayedAdapter;
    private MusicAdapter recommendedAdapter;
    private ApiService apiService;
    private PlayerBar playerBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        recentlyPlayedRecyclerView = view.findViewById(R.id.recently_played_recycler_view);
        recommendedRecyclerView = view.findViewById(R.id.recommended_recycler_view);

        // Get reference to PlayerBar from activity
        playerBar = requireActivity().findViewById(R.id.playerBar);

        // Set up layouts
        LinearLayoutManager recentlyPlayedLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recentlyPlayedRecyclerView.setLayoutManager(recentlyPlayedLayoutManager);
        LinearLayoutManager recommendedLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recommendedRecyclerView.setLayoutManager(recommendedLayoutManager);

        // Initialize adapters with empty lists
        recentlyPlayedAdapter = new MusicAdapter(new ArrayList<>());
        recommendedAdapter = new MusicAdapter(new ArrayList<>());

        // Set click listeners for adapters
        recentlyPlayedAdapter.setOnItemClickListener(this::onSongSelected);
        recommendedAdapter.setOnItemClickListener(this::onSongSelected);

        // Set adapters
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Fetch data
        fetchRecentlyPlayedSongs();
        fetchRecommendedSongs();

        return view;
    }

    private void onSongSelected(Song song) {
        if (playerBar != null && song != null) {
            String artistName = song.getArtist() != null ? song.getArtist().getName() : getString(R.string.unknown_artist);
            playerBar.setSongInfo(
                    song.getTitle(),
                    artistName,
                    song.getCoverImage(),
                    song.getAudioUrl(), // hoặc song.getUrl() nếu đúng tên trường
                    song
            );
            playerBar.setPlaying(true);
        }
    }

    private void fetchRecentlyPlayedSongs() {
        Call<List<Song>> call = apiService.getRecentlyPlayed();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recentlyPlayedAdapter.updateData(response.body());
                } else {
                    Log.e("HomeFragment", "Failed to fetch recently played songs: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Error fetching recently played songs: " + t.getMessage());
            }
        });
    }

    private void fetchRecommendedSongs() {
        Call<List<Song>> call = apiService.getRecommended();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recommendedAdapter.updateData(response.body());
                } else {
                    Log.e("HomeFragment", "Failed to fetch recommended songs: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Error fetching recommended songs: " + t.getMessage());
            }
        });
    }
}