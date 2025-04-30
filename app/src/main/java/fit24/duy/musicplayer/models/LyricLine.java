package fit24.duy.musicplayer.models;

public class LyricLine {
    private long startTime; // Thời điểm bắt đầu hiển thị (milliseconds)
    private String text;    // Nội dung lời bài hát
    private String language; // Ngôn ngữ của lời bài hát

    public LyricLine(long startTime, String text, String language) {
        this.startTime = startTime;
        this.text = text;
        this.language = language;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
} 