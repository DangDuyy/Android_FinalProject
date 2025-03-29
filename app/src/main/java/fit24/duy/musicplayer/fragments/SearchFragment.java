package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.MediaTypeAdapter;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.api.RetrofitClient;
import fit24.duy.musicplayer.models.MediaType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private GridView gridViewMediaTypes;
    private MediaTypeAdapter mediaTypeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        gridViewMediaTypes = view.findViewById(R.id.gridViewMediaTypes);
        SearchView searchView = view.findViewById(R.id.search_view);

        // Chuyển sang SearchResultFragment khi click vào SearchView
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.navigation_search_result);
            }
        });

        searchView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.navigation_search_result);
        });


        fetchMediaTypes();
        return view;

    }

    private void fetchMediaTypes() {
        ApiService apiService = RetrofitClient.getRetrofit().create(ApiService.class);
        apiService.getMediaTypes().enqueue(new Callback<List<MediaType>>() {
            @Override
            public void onResponse(Call<List<MediaType>> call, Response<List<MediaType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mediaTypeAdapter = new MediaTypeAdapter(getContext(), response.body());
                    gridViewMediaTypes.setAdapter(mediaTypeAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<MediaType>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải danh mục!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
