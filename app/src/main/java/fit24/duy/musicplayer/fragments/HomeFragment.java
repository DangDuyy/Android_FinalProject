package fit24.duy.musicplayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.constraintlayout.widget.ConstraintLayout;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.activities.MainActivity;
import fit24.duy.musicplayer.activities.PlayerActivity;
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
    private static final String TAG = "HomeFragment";
    private RecyclerView recentlyPlayedRecyclerView;
    private RecyclerView recommendedRecyclerView;
    private MusicAdapter recentlyPlayedAdapter;
    private MusicAdapter recommendedAdapter;
    private ApiService apiService;
    private PlayerBar playerBar;
    private ProgressBar recentlyPlayedProgress;
    private ProgressBar recommendedProgress;
    private TextView recentlyPlayedEmpty;
    private TextView recommendedEmpty;
    private ConstraintLayout playerBarLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "onCreateView: Initializing HomeFragment");

        // Initialize views
        recentlyPlayedRecyclerView = view.findViewById(R.id.recently_played_recycler_view);
        recommendedRecyclerView = view.findViewById(R.id.recommended_recycler_view);
        recentlyPlayedProgress = view.findViewById(R.id.recently_played_progress);
        recommendedProgress = view.findViewById(R.id.recommended_progress);
        recentlyPlayedEmpty = view.findViewById(R.id.recently_played_empty);
        recommendedEmpty = view.findViewById(R.id.recommended_empty);

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

        // Show loading indicators
        recentlyPlayedProgress.setVisibility(View.VISIBLE);
        recommendedProgress.setVisibility(View.VISIBLE);
        recentlyPlayedEmpty.setVisibility(View.GONE);
        recommendedEmpty.setVisibility(View.GONE);

        // Fetch data
        fetchRecentlyPlayedSongs();
        fetchRecommendedSongs();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get reference to PlayerBar from activity
        View playerBarView = requireActivity().findViewById(R.id.playerBar);
        playerBar = new PlayerBar(playerBarView, fit24.duy.musicplayer.utils.QueueManager.getInstance(requireContext()));
        playerBarLayout = (ConstraintLayout) playerBarView;
        
        // Set click listener for the entire player bar to open PlayerActivity
        playerBarLayout.setOnClickListener(v -> {
            if (playerBar.getCurrentSong() != null) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra("song", playerBar.getCurrentSong());
                startActivity(intent);
            }
        });
    }

    private void onSongSelected(Song song) {
        if (playerBar != null && song != null) {
            // Lấy danh sách hiện tại của adapter (có thể là recentlyPlayed hoặc recommended)
            List<Song> currentQueue;
            int index;
            if (recentlyPlayedAdapter != null && recentlyPlayedAdapter.getSongs().contains(song)) {
                currentQueue = recentlyPlayedAdapter.getSongs();
                index = currentQueue.indexOf(song);
            } else if (recommendedAdapter != null && recommendedAdapter.getSongs().contains(song)) {
                currentQueue = recommendedAdapter.getSongs();
                index = currentQueue.indexOf(song);
            } else {
                currentQueue = new ArrayList<>();
                index = -1;
            }
            if (index != -1) {
                fit24.duy.musicplayer.utils.QueueManager queueManager = fit24.duy.musicplayer.utils.QueueManager.getInstance(requireContext());
                queueManager.setQueue(currentQueue);
                queueManager.setCurrentIndex(index);
                queueManager.play();
            }
            playerBarLayout.setVisibility(View.VISIBLE);
            playerBar.setSongInfo(song);
            playerBar.togglePlayPause(true);
            // Mở PlayerActivity (không cần truyền song qua Intent)
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            startActivity(intent);
        }
    }

    private void fetchRecentlyPlayedSongs() {
        Log.d(TAG, "fetchRecentlyPlayedSongs: Starting API call");
        Call<List<Song>> call = apiService.getRecentlyPlayed();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                recentlyPlayedProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    Log.d(TAG, "fetchRecentlyPlayedSongs: Success! Received " + songs.size() + " songs");
                    if (songs.isEmpty()) {
                        recentlyPlayedEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recentlyPlayedEmpty.setVisibility(View.GONE);
                        recentlyPlayedAdapter.updateData(songs);
                    }
                } else {
                    Log.e(TAG, "fetchRecentlyPlayedSongs: Failed with code " + response.code());
                    recentlyPlayedEmpty.setVisibility(View.VISIBLE);
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchRecentlyPlayedSongs: Network error", t);
                recentlyPlayedProgress.setVisibility(View.GONE);
                recentlyPlayedEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Failed to load recently played songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecommendedSongs() {
        Log.d(TAG, "fetchRecommendedSongs: Starting API call");
        Call<List<Song>> call = apiService.getRecommended();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                recommendedProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    Log.d(TAG, "fetchRecommendedSongs: Success! Received " + songs.size() + " songs");
                    if (songs.isEmpty()) {
                        recommendedEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recommendedEmpty.setVisibility(View.GONE);
                        recommendedAdapter.updateData(songs);
                    }
                } else {
                    Log.e(TAG, "fetchRecommendedSongs: Failed with code " + response.code());
                    recommendedEmpty.setVisibility(View.VISIBLE);
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchRecommendedSongs: Network error", t);
                recommendedProgress.setVisibility(View.GONE);
                recommendedEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Failed to load recommended songs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}