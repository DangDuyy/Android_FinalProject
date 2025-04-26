package fit24.duy.musicplayer.adapters;

import android.content.Context;
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
import java.util.List;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Album;
import fit24.duy.musicplayer.models.Artist;

public class LibraryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTIST = 0;
    private static final int TYPE_ALBUM = 1;

    private List<Object> items;
    private Context context;

    public LibraryItemAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Artist) {
            return TYPE_ARTIST;
        } else {
            return TYPE_ALBUM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ARTIST) {
            View view = inflater.inflate(R.layout.item_artist_library, parent, false);
            return new ArtistViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_album_library, parent, false);
            return new AlbumViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ArtistViewHolder) {
            Artist artist = (Artist) items.get(position);
            ((ArtistViewHolder) holder).bind(artist);
        } else {
            Album album = (Album) items.get(position);
            ((AlbumViewHolder) holder).bind(album);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        ImageView artistImage;
        TextView artistName;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistImage = itemView.findViewById(R.id.artist_image);
            artistName = itemView.findViewById(R.id.artist_name);
        }

        public void bind(Artist artist) {
            artistName.setText(artist.getName());
            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + artist.getProfileImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(artistImage);

            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("artist_name", artist.getName());
                bundle.putString("artist_image", artist.getProfileImage());
                bundle.putLong("artist_id", artist.getId());
                Navigation.findNavController(itemView).navigate(R.id.navigation_artist, bundle);
            });
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumTitle, artistName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.album_image);
            albumTitle = itemView.findViewById(R.id.album_title);
            artistName = itemView.findViewById(R.id.artist_name);
        }

        public void bind(Album album) {
            albumTitle.setText(album.getTitle());
            artistName.setText(album.getArtist() != null ? album.getArtist().getName() : "Unknown Artist");
            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + album.getCoverImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(albumImage);

            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("album_title", album.getTitle());
                bundle.putString("album_image", album.getCoverImage());
                bundle.putLong("album_id", album.getId());
                if (album.getArtist() != null) {
                    bundle.putString("artist_name", album.getArtist().getName());
                    bundle.putString("artist_image", album.getArtist().getProfileImage());
                    bundle.putLong("artist_id", album.getArtist().getId());
                }
                Navigation.findNavController(itemView).navigate(R.id.navigation_album, bundle);
            });
        }
    }
}