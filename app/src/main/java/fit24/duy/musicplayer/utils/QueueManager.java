package fit24.duy.musicplayer.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.api.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueueManager {
    private static final String TAG = "QueueManager";
    private static final int MAX_QUEUE_SIZE = 50;
    private static QueueManager instance;
    private List<Song> queue;
    private int currentIndex;
    private Song lastPlayedSong;
    private OnQueueChangeListener queueChangeListener;
    private PlaybackStateListener playbackStateListener;
    private Context context;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private boolean isRepeat;

    // --- Interface để lắng nghe thay đổi queue ---
    public interface OnQueueChangeListener {
        void onQueueChanged(List<Song> queue, int currentIndex);
    }

    // --- Interface để lắng nghe trạng thái phát nhạc ---
    public interface PlaybackStateListener {
        void onPlaybackStateChanged(boolean isPlaying);
    }

    // --- Interface callback lấy bài hát ngẫu nhiên (bất đồng bộ) ---
    public interface RandomSongCallback {
        void onSongFetched(Song song);
    }

    private QueueManager(Context context) {
        this.context = context.getApplicationContext();
        this.queue = new ArrayList<>();
        this.currentIndex = -1;
        this.mediaPlayer = new MediaPlayer();
        this.isRepeat = false;
    }

    public static synchronized QueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new QueueManager(context);
        }
        return instance;
    }

    public void setOnQueueChangeListener(OnQueueChangeListener listener) {
        this.queueChangeListener = listener;
    }

    public void setPlaybackStateListener(PlaybackStateListener listener) {
        this.playbackStateListener = listener;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
        if (playbackStateListener != null) {
            playbackStateListener.onPlaybackStateChanged(isPlaying);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void addSong(Song song) {
        if (song == null) {
            Log.e(TAG, "Cannot add null song to queue");
            return;
        }

        Log.d(TAG, "Adding song to queue: " + song.getTitle());

        if (queue.size() >= MAX_QUEUE_SIZE) {
            Song removedSong = queue.remove(0);
            Log.d(TAG, "Queue full, removing oldest song: " + removedSong.getTitle());
            if (currentIndex > 0) {
                currentIndex--;
            }
        }
        queue.add(song);
        Log.d(TAG, "Current queue size: " + queue.size());
        notifyQueueChanged();
    }

    public void removeSong(int position) {
        if (position >= 0 && position < queue.size()) {
            queue.remove(position);
            if (position < currentIndex) {
                currentIndex--;
            } else if (position == currentIndex) {
                if (currentIndex >= queue.size()) {
                    currentIndex = queue.size() - 1;
                }
            }
            notifyQueueChanged();
        }
    }

    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < queue.size()) {
            return queue.get(currentIndex);
        }
        return null;
    }

    // Chú ý: Không thể trả về Song trực tiếp vì fetchRandomSong bất đồng bộ
    // Bạn nên tạo hàm lấy bài hát kế tiếp theo callback hoặc gọi playNext()

    public void moveToNext() {
        if (currentIndex + 1 < queue.size()) {
            currentIndex++;
            notifyQueueChanged();
        } else {
            // Lấy bài hát ngẫu nhiên bất đồng bộ
            fetchRandomSong(new RandomSongCallback() {
                @Override
                public void onSongFetched(Song song) {
                    if (song != null) {
                        addSong(song);
                        currentIndex = queue.size() - 1;
                        notifyQueueChanged();
                    } else {
                        Log.e(TAG, "Không lấy được bài hát ngẫu nhiên");
                    }
                }
            });
        }
    }

    public void moveToPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            notifyQueueChanged();
        }
    }

    // Hàm lấy bài hát ngẫu nhiên từ API với callback
    private void fetchRandomSong(RandomSongCallback callback) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Song>> call = apiService.getRandomSongs(1);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSongFetched(response.body().get(0));
                } else {
                    Log.e(TAG, "Không có bài hát nào từ API");
                    callback.onSongFetched(null);
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi gọi API lấy bài hát ngẫu nhiên", t);
                callback.onSongFetched(null);
            }
        });
    }

    private void notifyQueueChanged() {
        if (queueChangeListener != null) {
            queueChangeListener.onQueueChanged(queue, currentIndex);
        }
    }

    public List<Song> getQueue() {
        return queue;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void fillQueueWithRandomSongs(int count) {
        queue.clear();
        currentIndex = -1;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Song>> call = apiService.getRandomSongs(count);

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    queue.addAll(response.body());
                    currentIndex = queue.isEmpty() ? -1 : 0;
                    notifyQueueChanged();
                } else {
                    Log.e(TAG, "API không trả về danh sách bài hát");
                    Log.e(TAG, "Response code: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi đọc errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi gọi API lấy danh sách bài hát", t);
            }
        });
    }


    public void setCurrentIndex(int index) {
        if (index >= 0 && index < queue.size()) {
            currentIndex = index;
            play();
        }
    }

    public void setQueue(List<Song> songs) {
        this.queue = new ArrayList<>(songs);
        this.currentIndex = -1;
        if (queueChangeListener != null) {
            queueChangeListener.onQueueChanged(queue, currentIndex);
        }
    }

    public void setRepeatMode(boolean repeat) {
        this.isRepeat = repeat;
    }

    public void play() {
        if (currentIndex >= 0 && currentIndex < queue.size()) {
            try {
                Song song = queue.get(currentIndex);
                mediaPlayer.reset(); // Reset MediaPlayer để đảm bảo trạng thái sạch
                String audioUrl = UrlUtils.getAudioUrl(song.getFilePath());
                Log.d(TAG, "Playing song: " + song.getTitle() + ", URL: " + audioUrl);
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.setOnPreparedListener(mp -> {
                    Log.d(TAG, "MediaPlayer prepared, duration: " + mp.getDuration());
                    mp.start();
                    setPlaying(true);
                    notifyQueueChanged();
                    if (playbackStateListener != null) {
                        playbackStateListener.onPlaybackStateChanged(true);
                    }
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "Song completed, playing next");
                    playNext();
                });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                    Toast.makeText(context, "Cannot play this song, trying next", Toast.LENGTH_SHORT).show();
                    // Reset MediaPlayer và thử bài tiếp theo
                    mediaPlayer.reset();
                    playNext();
                    return true;
                });
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "Play error: " + e.getMessage() + ", URL: ", e);
                Toast.makeText(context, "Cannot play this song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Reset MediaPlayer và thử bài tiếp theo
                mediaPlayer.reset();
                playNext();
            }
        } else {
            Log.w(TAG, "Invalid currentIndex: " + currentIndex + ", queue size: " + queue.size());
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            setPlaying(false);
        }
    }

    public void playNext() {
        if (queue.isEmpty()) return;

        if (currentIndex < queue.size() - 1) {
            currentIndex++;
        } else if (isRepeat) {
            currentIndex = 0;
        } else {
            return;
        }

        play();
    }

    public void playPrevious() {
        if (queue.isEmpty()) return;

        if (currentIndex > 0) {
            currentIndex--;
        } else if (isRepeat) {
            currentIndex = queue.size() - 1;
        } else {
            return;
        }

        play();
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public void playSongAt(int position) {
        if (position >= 0 && position < queue.size()) {
            currentIndex = position;
            play(); // Gọi phương thức play() để phát bài hát tại vị trí hiện tại
        }
    }

    public void removeSongAt(int position) {
        if (position >= 0 && position < queue.size()) {
            queue.remove(position);
            if (currentIndex >= position) {
                currentIndex = Math.max(0, currentIndex - 1);
            }
            notifyQueueChanged();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
