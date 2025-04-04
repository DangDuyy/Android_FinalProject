package fit24.duy.musicplayer.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.adapters.MusicAdapter;
import fit24.duy.musicplayer.adapters.SearchResultAdapter;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.api.RetrofitClient;
import fit24.duy.musicplayer.models.Album;
import fit24.duy.musicplayer.models.Artist;
import fit24.duy.musicplayer.models.SearchResponse;
import fit24.duy.musicplayer.models.Song;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultFragment extends Fragment {
    private EditText edtSearch;
    private TextView tvCancel;
    private ImageView btnBack;
    private RecyclerView searchResultsRecyclerView;
    private MusicAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        edtSearch = view.findViewById(R.id.edt_search);
        tvCancel = view.findViewById(R.id.tv_cancel);
        btnBack = view.findViewById(R.id.btn_back);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);

        // Luôn hiển thị nút Cancel
        tvCancel.setVisibility(View.VISIBLE);

        // Set up RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new MusicAdapter(new ArrayList<>());
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Bắt sự kiện nhập vào ô tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý khi nhấn nút Cancel
        tvCancel.setOnClickListener(v -> {
            edtSearch.setText("");
            edtSearch.clearFocus();
        });

        // Sự kiện click nút Back
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp(); // Quay lại trang trước
        });

        return view;
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Nếu query trống, ẩn danh sách kết quả
            searchResultsRecyclerView.setAdapter(null);
            return; // Dừng tìm kiếm khi không có chữ
        }

        ApiService apiService = RetrofitClient.getRetrofit().create(ApiService.class);
        apiService.search(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Object> results = new ArrayList<>();
                    results.addAll(response.body().getSongs());
                    results.addAll(response.body().getArtists());
                    results.addAll(response.body().getAlbums());

                    SearchResultAdapter adapter = new SearchResultAdapter(getContext(), results);
                    searchResultsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().show();
    }
}

