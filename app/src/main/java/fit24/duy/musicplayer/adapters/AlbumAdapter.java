package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
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
import fit24.duy.musicplayer.activities.PlayerActivity;
import fit24.duy.musicplayer.activities.LoginActivity;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.UrlUtils;
import fit24.duy.musicplayer.utils.SessionManager;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.SongViewHolder> {
    private Context context;
    private List<Song> songList;
    private OnItemClickListener listener;
    private SessionManager sessionManager;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AlbumAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songList = songs;
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_album, parent, false);
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (sessionManager.isLoggedIn()) {
                        Song song = songList.get(position);
                        Intent intent = new Intent(context, PlayerActivity.class);
                        intent.putExtra("song_id", song.getId());
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Vui lòng đăng nhập để nghe nhạc", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                }
            });
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

            moreButton.setOnClickListener(v -> {
                // TODO: Thêm menu tuỳ chọn bài hát nếu cần
                Toast.makeText(context, "More clicked: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
