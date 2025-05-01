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

/**
 * Adapter for displaying a list of songs in a RecyclerView.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<Song> songs;
    private OnItemClickListener listener;

    /**
     * Interface definition for a callback to be invoked when an item in this
     * AdapterView has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * @param song The song that was clicked.
         */
        void onItemClick(Song song);
    }

    /**
     * Constructor for MusicAdapter.
     * @param songs The initial list of songs to display.
     */
    public MusicAdapter(List<Song> songs) {
        this.songs = songs;
    }

    /**
     * Register a callback to be invoked when an item in this adapter has been clicked.
     * @param listener The callback that will run.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the data set of the adapter.
     * @param newSongs The new list of songs.
     */
    public void updateData(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged(); // Notify the adapter to refresh the view
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each song item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false); // Ensure you have 'item_song.xml' layout
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        // Get the song at the current position and bind its data to the ViewHolder
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        // Return the total number of songs in the list
        return songs != null ? songs.size() : 0;
    }

    /**
     * ViewHolder class for song items. Holds references to the views within the item layout.
     */
    class MusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumArt;
        private TextView title;
        private TextView artistName;

        /**
         * Constructor for the ViewHolder.
         * @param itemView The view of the inflated layout for each item.
         */
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by their IDs defined in item_song.xml
            albumArt = itemView.findViewById(R.id.album_art);
            title = itemView.findViewById(R.id.song_title);
            artistName = itemView.findViewById(R.id.artist_name);

            // Set an OnClickListener for the entire item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                // Ensure the position is valid and a listener is set
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(songs.get(position));
                }
            });
        }

        /**
         * Binds the data from a Song object to the views in the ViewHolder.
         * @param song The Song object containing the data to display.
         */
        public void bind(Song song) {
            // Set song title
            title.setText(song.getTitle());

            // Set artist name, handle null artist
            if (song.getArtist() != null) {
                artistName.setText(song.getArtist().getName());
            } else {
                artistName.setText(itemView.getContext().getString(R.string.unknown_artist)); // Use string resource
            }

            // --- Load album art using Picasso ---
            String coverImage = song.getCoverImage();
            if (coverImage != null && !coverImage.isEmpty()) {
                String imageUrl;
                // Check if coverImage is a full URL (like Cloudinary)
                if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                    imageUrl = coverImage; // Use the full URL directly
                } else {
                    // Assume it's a filename and construct the local server URL
                    // **Important: Replace "10.0.2.2:8080" with your actual server IP/domain if needed**
                    // "10.0.2.2" is typically used to access the host machine's localhost from an Android emulator.
                    imageUrl = "http://10.0.2.2:8080/uploads/" + coverImage;
                }

                // Load the image using Picasso
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.album_placeholder) // Image shown while loading
                        .error(R.drawable.ic_broken_image) // Optional: Image shown on error (ensure you have this drawable)
                        .into(albumArt);
            } else {
                // Set a default placeholder image if no cover image is available
                albumArt.setImageResource(R.drawable.album_placeholder);
            }
        }
    }
}
