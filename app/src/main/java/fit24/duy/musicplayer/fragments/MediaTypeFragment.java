package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.MediaTypeAdapter;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.MediaTypeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MediaTypeFragment extends Fragment {
    private RecyclerView recyclerView;
    private MediaTypeAdapter songAdapter;
    private ApiService apiService;
    private String mediaTypeName, mediaTypeDescription;
    private Long mediaTypeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_type, container, false);
        recyclerView = view.findViewById(R.id.media_type_list);

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Nút Back
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp();
        });

        // Nhận dữ liệu được truyền qua
        Bundle args = getArguments();
        if (args != null) {
            mediaTypeName = args.getString("media_type_name");
            mediaTypeDescription = args.getString("media_type_description");
            mediaTypeId = args.getLong("media_type_id", -1);
            if (mediaTypeId == -1) {
                Toast.makeText(requireContext(), "Media Type ID not found", Toast.LENGTH_SHORT).show();
                return view;
            }

            // Hiển thị thông tin MediaType
            TextView nameView = view.findViewById(R.id.media_type_name);
            TextView descriptionView = view.findViewById(R.id.media_type_description);

            nameView.setText(mediaTypeName);
            descriptionView.setText(mediaTypeDescription);

            // Tải danh sách bài hát thuộc MediaType
            loadSongsByMediaType(mediaTypeId);
        }

        return view;
    }

    private void loadSongsByMediaType(Long mediaTypeId) {
        Call<List<MediaTypeResponse>> call = apiService.getSongsByMediaType(mediaTypeId);
        call.enqueue(new Callback<List<MediaTypeResponse>>() {
            @Override
            public void onResponse(Call<List<MediaTypeResponse>> call, Response<List<MediaTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MediaTypeResponse> songs = response.body();
                    List<Object> items = new ArrayList<>(songs);
                    songAdapter = new MediaTypeAdapter(requireContext(), items);
                    recyclerView.setAdapter(songAdapter);
                } else {
                    Toast.makeText(requireContext(), "Failed to load songs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MediaTypeResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}