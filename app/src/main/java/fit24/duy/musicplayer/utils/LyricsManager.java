package fit24.duy.musicplayer.utils;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit24.duy.musicplayer.models.LyricLine;

public class LyricsManager {
    private List<LyricLine> lyrics;
    private MediaPlayer mediaPlayer;
    private TextView currentLyricView;
    private TextView nextLyricView;
    private Handler handler;
    private int currentIndex = 0;
    private String currentLanguage = "vi"; // Mặc định là tiếng Việt

    public LyricsManager(MediaPlayer mediaPlayer, TextView currentLyricView, TextView nextLyricView) {
        this.mediaPlayer = mediaPlayer;
        this.currentLyricView = currentLyricView;
        this.nextLyricView = nextLyricView;
        this.lyrics = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void setLyrics(String lrcContent, String language) {
        this.currentLanguage = language;
        this.lyrics.clear();
        parseLrc(lrcContent);
    }

    private void parseLrc(String lrcContent) {
        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)");
        String[] lines = lrcContent.split("\n");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                int milliseconds = Integer.parseInt(matcher.group(3));
                String text = matcher.group(4).trim();

                long startTime = (minutes * 60 + seconds) * 1000 + milliseconds;
                lyrics.add(new LyricLine(startTime, text, currentLanguage));
            }
        }
    }

    public void start() {
        currentIndex = 0;
        updateLyrics();
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
        currentLyricView.setText("");
        nextLyricView.setText("");
    }

    private void updateLyrics() {
        if (currentIndex >= lyrics.size()) {
            return;
        }

        long currentTime = mediaPlayer.getCurrentPosition();
        LyricLine currentLine = lyrics.get(currentIndex);

        if (currentTime >= currentLine.getStartTime()) {
            currentLyricView.setText(currentLine.getText());
            
            if (currentIndex + 1 < lyrics.size()) {
                nextLyricView.setText(lyrics.get(currentIndex + 1).getText());
            } else {
                nextLyricView.setText("");
            }
            
            currentIndex++;
        }

        handler.postDelayed(this::updateLyrics, 100);
    }

    public void setLanguage(String language) {
        this.currentLanguage = language;
        // Có thể thêm logic để chuyển đổi ngôn ngữ ở đây
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
} 