package fit24.duy.musicplayer.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.QueueManager;

public class QueueDialog extends DialogFragment {
    private List<Song> queue;
    private int currentIndex;
    private OnQueueItemClickListener listener;

    public interface OnQueueItemClickListener {
        void onItemClick(int position);
        void onItemRemoved(int position);
    }

    public static QueueDialog newInstance(List<Song> queue, int currentIndex) {
        QueueDialog dialog = new QueueDialog();
        dialog.queue = queue;
        dialog.currentIndex = currentIndex;
        return dialog;
    }

    public void setOnQueueItemClickListener(OnQueueItemClickListener listener) {
        this.listener = listener;
    }

    private QueueAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_queue, container, false);

        // Toolbar setup
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> dismiss());

        TextView titleText = view.findViewById(R.id.title_text);
        titleText.setText("Queue");

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.queue_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Use queue & currentIndex passed from outside (do NOT call fillQueue here)
        adapter = new QueueAdapter(queue, currentIndex);
        recyclerView.setAdapter(adapter);

        // Listen for queue changes and update adapter
        QueueManager.getInstance(requireContext()).setOnQueueChangeListener((newQueue, newCurrentIndex) -> {
            queue = newQueue;
            currentIndex = newCurrentIndex;
            adapter.songs = newQueue;
            adapter.currentIndex = newCurrentIndex;
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    private class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {
        private List<Song> songs;
        private int currentIndex;

        public QueueAdapter(List<Song> songs, int currentIndex) {
            this.songs = songs;
            this.currentIndex = currentIndex;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_queue, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Song song = songs.get(position);
            holder.songTitle.setText(song.getTitle());
            holder.artistName.setText(song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");

            // Highlight current song
            holder.itemView.setAlpha(position == currentIndex ? 1.0f : 0.7f);
            holder.cardView.setCardBackgroundColor(position == currentIndex ?
                    getContext().getColor(R.color.purple_500) :
                    getContext().getColor(R.color.white));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView songTitle;
            TextView artistName;
            MaterialCardView cardView;

            ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_view);
                songTitle = itemView.findViewById(R.id.song_title);
                artistName = itemView.findViewById(R.id.artist_name);
            }
        }
    }
}
