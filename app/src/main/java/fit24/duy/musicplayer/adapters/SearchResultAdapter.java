package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Album;
import fit24.duy.musicplayer.models.Artist;
import fit24.duy.musicplayer.models.Song;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SONG = 0;
    private static final int TYPE_ARTIST = 1;
    private static final int TYPE_ALBUM = 2;

    private List<Object> results;
    private Context context;

    public SearchResultAdapter(Context context, List<Object> results) {
        this.context = context;
        this.results = results;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = results.get(position);
        if (item instanceof Song) return TYPE_SONG;
        else if (item instanceof Artist) return TYPE_ARTIST;
        else return TYPE_ALBUM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_SONG) {
            View view = inflater.inflate(R.layout.item_song, parent, false);
            return new SongViewHolder(view);
        } else if (viewType == TYPE_ARTIST) {
            View view = inflater.inflate(R.layout.item_artist, parent, false);
            return new ArtistViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_album, parent, false);
            return new AlbumViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = results.get(position);
        if (holder instanceof SongViewHolder) {
            Song song = (Song) item;
            ((SongViewHolder) holder).bind(song);
        } else if (holder instanceof ArtistViewHolder) {
            Artist artist = (Artist) item;
            ((ArtistViewHolder) holder).bind(artist);
        } else {
            Album album = (Album) item;
            ((AlbumViewHolder) holder).bind(album);
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        ImageView imgCover;

        public SongViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.song_title);
            tvArtist = itemView.findViewById(R.id.artist_name);
            imgCover = itemView.findViewById(R.id.album_art);
        }

        public void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist().getName());
            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + song.getCoverImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(imgCover);
        }
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgArtist;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            imgArtist = itemView.findViewById(R.id.img_artist);
        }

        public void bind(Artist artist) {
            tvName.setText(artist.getName());
            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + artist.getProfileImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imgArtist);

            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("artist_name", artist.getName());
                bundle.putString("artist_image", artist.getProfileImage());

                Navigation.findNavController(itemView).navigate(R.id.navigation_artist, bundle);
            });
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imgAlbum;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgAlbum = itemView.findViewById(R.id.img_album);
        }

        public void bind(Album album) {
            tvTitle.setText(album.getTitle());
            Glide.with(itemView.getContext())
                    .load("http://10.0.2.2:8080/uploads/" + album.getCoverImage() + "?t=" + System.currentTimeMillis())
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(imgAlbum);
        }
    }
}

