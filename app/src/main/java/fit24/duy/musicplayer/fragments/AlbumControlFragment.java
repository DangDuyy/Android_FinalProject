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

import com.bumptech.glide.Glide;

import fit24.duy.musicplayer.R;

public class AlbumControlFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_control, container, false);

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            String albumTitle = args.getString("album_title");
            String albumImage = args.getString("album_image");

            // Hiển thị thông tin album
            TextView albumTitleControl = view.findViewById(R.id.album_title_control);
            ImageView albumImageControl = view.findViewById(R.id.album_image_control);

            albumTitleControl.setText(albumTitle);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + albumImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .into(albumImageControl);
        }

        // Xử lý các tùy chọn
        view.findViewById(R.id.option_view_artist).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn View artist", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_share).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Share", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_like_all).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Like all songs", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_add_to_playlist).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Add to playlist", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_add_to_queue).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Add to queue", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_go_to_radio).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Go to radio", Toast.LENGTH_SHORT).show();
        });

        // Nút Close
        view.findViewById(R.id.btn_close).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp();
        });

        return view;
    }
}