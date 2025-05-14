package fit24.duy.musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class MusicPlayerService extends Service {
    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;

    private int userStatus;  // Biến lưu trữ thông tin người dùng

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent != null) {
            userStatus = intent.getIntExtra("userStatus", -1);  // Lấy userStatus từ Intent
        }
        return binder;
    }

    public void play(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public int getUserStatus() {
        return userStatus;  // Trả về userStatus khi cần
    }
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
} 