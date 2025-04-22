package fit24.duy.musicplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Artist;
import fit24.duy.musicplayer.models.Song;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<Song> songs;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public MusicAdapter(List<Song> songs) {
        this.songs = songs;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false); // Assuming your item layout is named item_song.xml
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumArt;
        private TextView title;
        private TextView artistName;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.album_art); // Make sure these IDs exist in item_song.xml
            title = itemView.findViewById(R.id.song_title);
            artistName = itemView.findViewById(R.id.artist_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(songs.get(position));
                }
            });
        }

        // Trong class MusicAdapter.MusicViewHolder

        public void bind(Song song) {
            title.setText(song.getTitle());
            if (song.getArtist() != null) {
                artistName.setText(song.getArtist().getName());
            } else {
                artistName.setText("");
            }

            // Load album art using Picasso
            if (song.getCoverImage() != null && !song.getCoverImage().isEmpty()) {
                String imageUrl = "http://10.0.2.2:8080/uploads/" + song.getCoverImage(); // **Quan trọng: Thay IP server của bạn**
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.album_placeholder)
                        //.error(R.drawable.error_image)
                        .into(albumArt);
            } else {
                albumArt.setImageResource(R.drawable.album_placeholder);
            }
        }
    }
}