package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Song;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.SongViewHolder> {
    private Context context;
    private List<Song> songList;

    public AlbumAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songList = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_artist, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(songList.get(position));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView songImage;
        TextView songTitle, songArtist;
        ImageButton moreButton;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songImage = itemView.findViewById(R.id.song_image);
            songTitle = itemView.findViewById(R.id.song_title);
            songArtist = itemView.findViewById(R.id.song_artist);
            moreButton = itemView.findViewById(R.id.more_button);
        }

        public void bind(Song song) {
            songTitle.setText(song.getTitle());
            songArtist.setText(song.getArtist().getName());

            Glide.with(context)
                    .load("http://10.0.2.2:8080/uploads/" + song.getCoverImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .into(songImage);

            moreButton.setOnClickListener(v -> {
                Toast.makeText(context, "More clicked: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
