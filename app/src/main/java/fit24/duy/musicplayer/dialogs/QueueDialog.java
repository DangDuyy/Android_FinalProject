package fit24.duy.musicplayer.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.utils.QueueManager;

import java.util.List;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    private QueueAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_queue, container, false);

        // Set up toolbar
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> dismiss());

        TextView titleText = view.findViewById(R.id.title_text);
        titleText.setText("Queue");

        // Luôn random queue mới nhất từ backend mỗi lần mở dialog
        QueueManager queueManager = QueueManager.getInstance(requireContext());
        queueManager.fillQueueWithRandomSongs(10);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.queue_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QueueAdapter(queueManager.getQueue(), queueManager.getCurrentIndex());
        recyclerView.setAdapter(adapter);

        // Update adapter when queue changes
        QueueManager.getInstance(requireContext()).setOnQueueChangeListener((newQueue, newCurrentIndex) -> {
            queue = newQueue;
            currentIndex = newCurrentIndex;
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
                    listener.onItemClick(position); // Gọi callback onItemClick
                    dismiss(); // Đóng dialog sau khi chọn bài hát
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

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(position);
                    }
                });
            }
        }
    }
} 