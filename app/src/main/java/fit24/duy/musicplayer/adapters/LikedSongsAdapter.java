package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.UrlUtils;
import java.util.List;

public class LikedSongsAdapter extends RecyclerView.Adapter<LikedSongsAdapter.SongViewHolder> {
    private Context context;
    private List<Song> songs;

    public LikedSongsAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_liked_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(songs.get(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView songImage;
        TextView songTitle, songArtist;
        ImageView songMenu;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songImage = itemView.findViewById(R.id.song_image);
            songTitle = itemView.findViewById(R.id.song_title);
            songArtist = itemView.findViewById(R.id.song_artist);
            songMenu = itemView.findViewById(R.id.song_menu);
        }

        public void bind(Song song) {
            songTitle.setText(song.getTitle());
            songArtist.setText(song.getArtist().getName());

            String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(songImage);

            songMenu.setOnClickListener(v -> {
                Toast.makeText(context, "More clicked: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}