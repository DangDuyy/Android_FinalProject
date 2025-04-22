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

public class ArtistControlFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_control, container, false);

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            String artistName = args.getString("artist_name");
            String artistImage = args.getString("artist_image");

            // Hiển thị thông tin nghệ sĩ
            TextView artistNameControl = view.findViewById(R.id.artist_name_control);
            ImageView artistImageControl = view.findViewById(R.id.artist_image_control);

            artistNameControl.setText(artistName);
            Glide.with(requireContext())
                    .load("http://10.0.2.2:8080/uploads/" + artistImage + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(artistImageControl);
        }

        // Xử lý các tùy chọn
        view.findViewById(R.id.option_like).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Like", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_follow_artist).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Follow artist", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.option_share).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã nhấn Share", Toast.LENGTH_SHORT).show();
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