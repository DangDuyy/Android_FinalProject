package fit24.duy.musicplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fit24.duy.musicplayer.R;
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

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_song, parent, false);
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
        private TextView artist;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.album_art);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.artist_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(songs.get(position));
                }
            });
        }

        public void bind(Song song) {
            albumArt.setImageResource(song.getAlbumArt());
            title.setText(song.getTitle());
            artist.setText(song.getArtist().getName());
        }
    }
} 