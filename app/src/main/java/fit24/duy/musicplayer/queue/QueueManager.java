package fit24.duy.musicplayer.queue;

import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.List;

public class QueueManager {
    private List<String> queue;
    private int currentIndex;
    private MediaPlayer mediaPlayer;
    private int repeatMode; // 0: No repeat, 1: Repeat all, 2: Repeat one

    public QueueManager(MediaPlayer mediaPlayer) {
        this.queue = new ArrayList<>();
        this.currentIndex = -1;
        this.mediaPlayer = mediaPlayer;
        this.repeatMode = 0;
    }

    public void setQueue(List<String> songs) {
        this.queue = new ArrayList<>(songs);
        this.currentIndex = -1;
    }

    public void addToQueue(String song) {
        queue.add(song);
    }

    public void setRepeatMode(int mode) {
        this.repeatMode = mode;
    }

    public void playNext() {
        if (queue.isEmpty()) return;

        if (currentIndex < queue.size() - 1) {
            currentIndex++;
        } else if (repeatMode == 1) { // Repeat all
            currentIndex = 0;
        } else {
            return;
        }

        String nextSong = queue.get(currentIndex);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(nextSong);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playPrevious() {
        if (queue.isEmpty()) return;

        if (currentIndex > 0) {
            currentIndex--;
        } else if (repeatMode == 1) { // Repeat all
            currentIndex = queue.size() - 1;
        } else {
            return;
        }

        String previousSong = queue.get(currentIndex);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(previousSong);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < queue.size()) {
            return queue.get(currentIndex);
        }
        return null;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        if (index >= 0 && index < queue.size()) {
            currentIndex = index;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
} 