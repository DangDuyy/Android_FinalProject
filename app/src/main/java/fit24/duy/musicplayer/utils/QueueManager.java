package fit24.duy.musicplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
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

    public interface OnQueueChangeListener {
        void onQueueChanged(List<Song> queue, int currentIndex);
    }

    public interface PlaybackStateListener {
        void onPlaybackStateChanged(boolean isPlaying);
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

    public Song getNextSong() {
        if (currentIndex + 1 < queue.size()) {
            return queue.get(currentIndex + 1);
        }
        return fetchRandomSong();
    }

    public Song getPreviousSong() {
        if (currentIndex > 0) {
            return queue.get(currentIndex - 1);
        }
        return null;
    }

    public void moveToNext() {
        if (currentIndex + 1 < queue.size()) {
            currentIndex++;
        } else {
            Song nextSong = fetchRandomSong();
            if (nextSong != null) {
                addSong(nextSong);
                currentIndex = queue.size() - 1;
            }
        }
        notifyQueueChanged();
    }

    public void moveToPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            notifyQueueChanged();
        }
    }

    private Song fetchRandomSong() {
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            List<Song> allSongs = new ArrayList<>();
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                Song song = new Song(id, title, artist, path);
                if (lastPlayedSong == null || !song.getPath().equals(lastPlayedSong.getPath())) {
                    allSongs.add(song);
                }
            } while (cursor.moveToNext());
            cursor.close();

            if (!allSongs.isEmpty()) {
                Random random = new Random();
                Song randomSong = allSongs.get(random.nextInt(allSongs.size()));
                lastPlayedSong = randomSong;
                return randomSong;
            }
        }
        return null;
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

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        );

        Set<String> uniquePaths = new HashSet<>();
        List<Song> allSongs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                if (!uniquePaths.contains(path)) {
                    allSongs.add(new Song(id, title, artist, path));
                    uniquePaths.add(path);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Collections.shuffle(allSongs);
        for (int i = 0; i < Math.min(count, allSongs.size()); i++) {
            addSong(allSongs.get(i));
        }
        if (!queue.isEmpty()) {
            currentIndex = 0;
        }
        notifyQueueChanged();
    }

    public void fillQueueWithRandomSongsFromApi(int count) {
        queue.clear();
        currentIndex = -1;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Song>> call = apiService.getRandomSongs(count);
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    for (Song song : songs) {
                        queue.add(song);
                    }
                    if (!queue.isEmpty()) {
                        currentIndex = 0;
                    }
                    notifyQueueChanged();
                }
            }
            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch random songs from API", t);
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
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getFilePath());
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    setPlaying(true);
                    notifyQueueChanged();
                    if (playbackStateListener != null) {
                        playbackStateListener.onPlaybackStateChanged(true);
                    }
                });
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
} 