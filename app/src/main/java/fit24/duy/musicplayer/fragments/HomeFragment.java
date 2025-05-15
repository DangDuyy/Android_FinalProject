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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.activities.MainActivity;
import fit24.duy.musicplayer.activities.PlayerActivity;
import fit24.duy.musicplayer.adapters.MusicAdapter;
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
    private ProgressBar recentlyPlayedProgress;
    private ProgressBar recommendedProgress;
    private TextView recentlyPlayedEmpty;
    private TextView recommendedEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "onCreateView: Initializing HomeFragment");

        // Initialize views
        recommendedRecyclerView = view.findViewById(R.id.recommended_recycler_view);
        recommendedProgress = view.findViewById(R.id.recommended_progress);
        recommendedEmpty = view.findViewById(R.id.recommended_empty);

        // Set up layout with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2); // 2 items per row
        recommendedRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize adapter with empty list
        recommendedAdapter = new MusicAdapter(new ArrayList<>());
        recommendedAdapter.setOnItemClickListener(this::onSongSelected);
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Show loading indicator
        recommendedProgress.setVisibility(View.VISIBLE);
        recommendedEmpty.setVisibility(View.GONE);

        // Fetch recommended songs
        fetchRecommendedSongs();

        return view;
    }

    private void onSongSelected(Song song) {
        if (song != null) {
            Intent intent = new Intent(getActivity(), fit24.duy.musicplayer.activities.PlayerActivity.class);
            intent.putExtra("song_id", song.getId());
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