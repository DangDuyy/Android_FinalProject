package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.MediaType;
import fit24.duy.musicplayer.models.MediaTypeResponse; // Thêm import

public class MediaTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Object> items; // Danh sách chứa cả MediaType và MediaTypeResponse
    private static final int TYPE_MEDIA_TYPE = 1;
    private static final int TYPE_SONG = 2;

    public MediaTypeAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof MediaType) {
            return TYPE_MEDIA_TYPE;
        } else if (item instanceof MediaTypeResponse) {
            return TYPE_SONG;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MEDIA_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_media_type, parent, false);
            return new MediaTypeViewHolder(view);
        } else if (viewType == TYPE_SONG) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_song_grid, parent, false);
            return new SongViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MediaTypeViewHolder) {
            MediaType mediaType = (MediaType) items.get(position);
            ((MediaTypeViewHolder) holder).bind(mediaType);
        } else if (holder instanceof SongViewHolder) {
            MediaTypeResponse song = (MediaTypeResponse) items.get(position);
            ((SongViewHolder) holder).bind(song);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder cho MediaType
    static class MediaTypeViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgIcon;
        View background;

        public MediaTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txName);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            background = itemView.findViewById(R.id.rootLayout);
        }

        public void bind(MediaType mediaType) {
            txtName.setText(mediaType.getName());

            // Danh sách các màu
            int[] colors = {
                    Color.parseColor("#E91E63"), // Hồng
                    Color.parseColor("#4CAF50"), // Xanh lá
                    Color.parseColor("#9C27B0"), // Tím
                    Color.parseColor("#03A9F4"), // Xanh nhạt
                    Color.parseColor("#F44336"), // Đỏ
                    Color.parseColor("#2196F3"), // Xanh biển
                    Color.parseColor("#795548"), // Nâu
                    Color.parseColor("#FF9800"), // Cam
                    Color.parseColor("#8BC34A"), // Xanh lá mạ
                    Color.parseColor("#607D8B"), // Xám xanh
                    Color.parseColor("#F06292")  // Hồng sáng
            };

            // Chọn màu ngẫu nhiên
            int randomColor = colors[new Random().nextInt(colors.length)];

            // Thay đổi màu nền mà vẫn giữ bo góc
            GradientDrawable drawable = (GradientDrawable) background.getBackground();
            drawable.setColor(randomColor);

            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + mediaType.getIcon() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(imgIcon);

            // Điều hướng đến MediaTypeFragment khi nhấn
            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("media_type_name", mediaType.getName());
                bundle.putString("media_type_description", mediaType.getDescription());
                bundle.putLong("media_type_id", mediaType.getId());
                Navigation.findNavController(v).navigate(R.id.navigation_media_type, bundle);
            });
        }
    }

    // ViewHolder cho MediaTypeResponse
    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_song_cover);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
        }

        public void bind(MediaTypeResponse song) {
            tvTitle.setText(song.getTitle());

            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + song.getCoverImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(imgCover);
        }
    }
}