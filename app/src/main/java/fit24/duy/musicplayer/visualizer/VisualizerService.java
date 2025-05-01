package fit24.duy.musicplayer.visualizer;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;

public class VisualizerService {
    private Visualizer visualizer;
    private MediaPlayer mediaPlayer;
    private MusicVisualizerView visualizerView;
    private Handler handler;
    private boolean isVisualizing = false;

    public VisualizerService(MediaPlayer mediaPlayer, MusicVisualizerView visualizerView) {
        this.mediaPlayer = mediaPlayer;
        this.visualizerView = visualizerView;
        this.handler = new Handler(Looper.getMainLooper());
        setupVisualizer();
    }

    private void setupVisualizer() {
        try {
            int audioSessionId = mediaPlayer.getAudioSessionId();
            visualizer = new Visualizer(audioSessionId);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    if (isVisualizing) {
                        handler.post(() -> visualizerView.setWaveformData(waveform));
                    }
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    if (isVisualizing) {
                        handler.post(() -> visualizerView.setSpectrumData(fft));
                    }
                }
            };

            visualizer.setDataCaptureListener(
                captureListener,
                Visualizer.getMaxCaptureRate() / 2,
                true,
                true
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startVisualizing() {
        if (visualizer != null) {
            isVisualizing = true;
            visualizer.setEnabled(true);
        }
    }

    public void stopVisualizing() {
        if (visualizer != null) {
            isVisualizing = false;
            visualizer.setEnabled(false);
        }
    }

    public void release() {
        if (visualizer != null) {
            visualizer.release();
            visualizer = null;
        }
    }

    public void setVisualizerType(boolean isWaveform) {
        visualizerView.setVisualizerType(isWaveform);
    }

    public void setVisualizerColor(int color) {
        visualizerView.setVisualizerColor(color);
    }
} 