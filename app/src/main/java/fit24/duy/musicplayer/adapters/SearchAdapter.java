package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Album;
import fit24.duy.musicplayer.models.Artist;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.UrlUtils;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SONG = 0;
    private static final int TYPE_ARTIST = 1;
    private static final int TYPE_ALBUM = 2;

    private List<Object> results;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Object item);
    }

    public SearchAdapter(Context context, List<Object> results) {
        this.context = context;
        this.results = results;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
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
            View view = inflater.inflate(R.layout.item_song_artist, parent, false);
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

    class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        ImageView imgCover;

        public SongViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.song_title);
            tvArtist = itemView.findViewById(R.id.song_artist);
            imgCover = itemView.findViewById(R.id.song_image);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(results.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText("Bài hát • " + song.getArtist().getName());

            String imageUrl = UrlUtils.getImageUrl(song.getCoverImage());
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(imgCover);
        }
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgArtist;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            imgArtist = itemView.findViewById(R.id.img_artist);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(results.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Artist artist) {
            tvName.setText(artist.getName());

            String imageUrl = UrlUtils.getImageUrl(artist.getProfileImage());
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imgArtist);
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imgAlbum;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imgAlbum = itemView.findViewById(R.id.img_album);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(results.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Album album) {
            tvTitle.setText(album.getTitle());

            String imageUrl = UrlUtils.getImageUrl(album.getCoverImage());
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(imgAlbum);
        }
    }
}