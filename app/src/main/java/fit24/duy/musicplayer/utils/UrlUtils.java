package fit24.duy.musicplayer.utils;

import android.util.Log;

public class UrlUtils {
    private static final String TAG = "UrlUtils";
    private static final String BACKEND_URL = "http://10.0.2.2:8080";

    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.d(TAG, "Image path is null or empty");
            return null;
        }

        Log.d(TAG, "Original image path: " + imagePath);

        // Nếu là URL đầy đủ (http:// hoặc https://)
        if (imagePath.toLowerCase().startsWith("http://") || imagePath.toLowerCase().startsWith("https://")) {
            Log.d(TAG, "Using full URL for image: " + imagePath);
            return imagePath;
        }

        // Nếu là tên file trong uploads
        String url = BACKEND_URL + "/uploads/" + imagePath;
        Log.d(TAG, "Generated local URL: " + url);
        return url;
    }

    public static String getAudioUrl(String audioPath) {
        if (audioPath == null || audioPath.isEmpty()) {
            Log.d(TAG, "Audio path is null or empty");
            return null;
        }

        Log.d(TAG, "Original audio path: " + audioPath);

        // Nếu là URL đầy đủ (http:// hoặc https://)
        if (audioPath.toLowerCase().startsWith("http://") || audioPath.toLowerCase().startsWith("https://")) {
            Log.d(TAG, "Using full URL for audio: " + audioPath);
            return audioPath;
        }

        // Nếu là tên file trong uploads
        String url = BACKEND_URL + "/uploads/" + audioPath;
        Log.d(TAG, "Generated local URL: " + url);
        return url;
    }

    public static String getBackendUrl() {
        return BACKEND_URL;
    }
} 