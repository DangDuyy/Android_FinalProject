package fit24.duy.musicplayer.utils;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit24.duy.musicplayer.models.LyricLine;

public class LyricsManager {
    private static final String TAG = "LyricsManager";
    private List<LyricLine> lyrics;
    private MediaPlayer mediaPlayer;
    private TextView currentLyricView;
    private TextView nextLyricView;
    private Handler handler;
    private int currentIndex = 0;
    private String currentLanguage = "vi"; // Mặc định là tiếng Việt
    private boolean isVisible = true;

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
        try {
            parseLrc(lrcContent);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing lyrics: " + e.getMessage());
            currentLyricView.setText("Error parsing lyrics");
            nextLyricView.setText("");
        }
    }

    private void parseLrc(String lrcContent) {
        if (lrcContent == null || lrcContent.trim().isEmpty()) {
            Log.w(TAG, "Empty lyrics content");
            return;
        }

        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)");
        String[] lines = lrcContent.split("\n");

        for (String line : lines) {
            try {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    int minutes = Integer.parseInt(matcher.group(1));
                    int seconds = Integer.parseInt(matcher.group(2));
                    int milliseconds = Integer.parseInt(matcher.group(3));
                    String text = matcher.group(4).trim();

                    if (text.isEmpty()) continue;

                    long startTime = (minutes * 60 + seconds) * 1000 + milliseconds;
                    lyrics.add(new LyricLine(startTime, text, currentLanguage));
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing line: " + line + ", " + e.getMessage());
            }
        }

        // Sort lyrics by start time
        lyrics.sort((a, b) -> Long.compare(a.getStartTime(), b.getStartTime()));
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
        if (currentIndex >= lyrics.size() || !isVisible) {
            return;
        }

        long currentTime = mediaPlayer.getCurrentPosition();
        LyricLine currentLine = lyrics.get(currentIndex);

        if (currentTime >= currentLine.getStartTime()) {
            // Fade out animation
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(300);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    currentLyricView.setText(currentLine.getText());
                    // Fade in animation
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    fadeIn.setDuration(300);
                    currentLyricView.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            currentLyricView.startAnimation(fadeOut);
            
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

    public void toggleVisibility() {
        isVisible = !isVisible;
        if (isVisible) {
            currentLyricView.setVisibility(View.VISIBLE);
            nextLyricView.setVisibility(View.VISIBLE);
            updateLyrics();
        } else {
            currentLyricView.setVisibility(View.GONE);
            nextLyricView.setVisibility(View.GONE);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }
} 