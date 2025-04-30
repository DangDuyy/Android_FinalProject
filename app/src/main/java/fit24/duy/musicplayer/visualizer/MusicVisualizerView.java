package fit24.duy.musicplayer.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MusicVisualizerView extends View {
    private Paint paint;
    private byte[] waveformData;
    private byte[] spectrumData;
    private boolean isWaveform = true;
    private int visualizerColor = Color.parseColor("#FF6B6B");

    public MusicVisualizerView(Context context) {
        super(context);
        init();
    }

    public MusicVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2f);
    }

    public void setWaveformData(byte[] data) {
        this.waveformData = data;
        invalidate();
    }

    public void setSpectrumData(byte[] data) {
        this.spectrumData = data;
        invalidate();
    }

    public void setVisualizerType(boolean isWaveform) {
        this.isWaveform = isWaveform;
        invalidate();
    }

    public void setVisualizerColor(int color) {
        this.visualizerColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Clear background
        canvas.drawColor(Color.TRANSPARENT);
        
        if (isWaveform && waveformData != null) {
            drawWaveform(canvas);
        } else if (!isWaveform && spectrumData != null) {
            drawSpectrum(canvas);
        }
    }

    private void drawWaveform(Canvas canvas) {
        paint.setColor(visualizerColor);
        paint.setStyle(Paint.Style.STROKE);

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        float sliceWidth = width / waveformData.length;

        // Create a Path object to draw the waveform
        Path path = new Path();
        path.moveTo(0, centerY); // Start at the center

        for (int i = 0; i < waveformData.length; i++) {
            float x = i * sliceWidth;
            float normalizedAmplitude = (waveformData[i] & 0xFF) / 128.0f;
            float scaledAmplitude = normalizedAmplitude * (height / 2);
            float y = centerY + scaledAmplitude;

            path.lineTo(x, y); // Add a line to the path
        }

        // Draw the path on the canvas
        canvas.drawPath(path, paint);
    }
    private void drawSpectrum(Canvas canvas) {
        paint.setColor(visualizerColor);
        paint.setStyle(Paint.Style.FILL);

        float width = getWidth();
        float height = getHeight();
        float barWidth = (width / spectrumData.length) * 2.5f;
        float barSpacing = 1f;

        for (int i = 0; i < spectrumData.length; i++) {
            float x = i * (barWidth + barSpacing);
            float normalizedAmplitude = (spectrumData[i] & 0xFF) / 255.0f;
            float barHeight = normalizedAmplitude * height;

            canvas.drawRect(x, height - barHeight, x + barWidth, height, paint);
        }
    }
} 