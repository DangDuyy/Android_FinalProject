import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Dimensions } from 'react-native';
import { Audio } from 'expo-av';

interface MusicVisualizerProps {
  audioUrl: string;
  isPlaying: boolean;
  visualizerType: 'waveform' | 'spectrum';
  color: string;
}

const { width } = Dimensions.get('window');

export const MusicVisualizer: React.FC<MusicVisualizerProps> = ({
  audioUrl,
  isPlaying,
  visualizerType,
  color,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const animationFrameRef = useRef<number>();

  useEffect(() => {
    if (!audioUrl) return;

    const setupAudioContext = async () => {
      try {
        const audioContext = new AudioContext();
        const analyser = audioContext.createAnalyser();
        analyser.fftSize = 256;
        
        const source = audioContext.createMediaElementSource(new Audio(audioUrl));
        source.connect(analyser);
        analyser.connect(audioContext.destination);

        audioContextRef.current = audioContext;
        analyserRef.current = analyser;

        if (isPlaying) {
          audioContext.resume();
        }
      } catch (error) {
        console.error('Error setting up audio context:', error);
      }
    };

    setupAudioContext();

    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
      if (audioContextRef.current) {
        audioContextRef.current.close();
      }
    };
  }, [audioUrl]);

  useEffect(() => {
    if (!canvasRef.current || !analyserRef.current || !isPlaying) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const analyser = analyserRef.current;
    const bufferLength = analyser.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);

    const draw = () => {
      animationFrameRef.current = requestAnimationFrame(draw);

      if (visualizerType === 'waveform') {
        analyser.getByteTimeDomainData(dataArray);
        drawWaveform(ctx, dataArray, bufferLength, color);
      } else {
        analyser.getByteFrequencyData(dataArray);
        drawSpectrum(ctx, dataArray, bufferLength, color);
      }
    };

    draw();
  }, [isPlaying, visualizerType, color]);

  const drawWaveform = (
    ctx: CanvasRenderingContext2D,
    dataArray: Uint8Array,
    bufferLength: number,
    color: string
  ) => {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.2)';
    ctx.fillRect(0, 0, width, 200);

    ctx.lineWidth = 2;
    ctx.strokeStyle = color;
    ctx.beginPath();

    const sliceWidth = width / bufferLength;
    let x = 0;

    for (let i = 0; i < bufferLength; i++) {
      const v = dataArray[i] / 128.0;
      const y = (v * 200) / 2;

      if (i === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }

      x += sliceWidth;
    }

    ctx.lineTo(width, 200 / 2);
    ctx.stroke();
  };

  const drawSpectrum = (
    ctx: CanvasRenderingContext2D,
    dataArray: Uint8Array,
    bufferLength: number,
    color: string
  ) => {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.2)';
    ctx.fillRect(0, 0, width, 200);

    const barWidth = (width / bufferLength) * 2.5;
    let x = 0;

    for (let i = 0; i < bufferLength; i++) {
      const barHeight = (dataArray[i] / 255) * 200;

      ctx.fillStyle = color;
      ctx.fillRect(x, 200 - barHeight, barWidth, barHeight);

      x += barWidth + 1;
    }
  };

  return (
    <View style={styles.container}>
      <canvas
        ref={canvasRef}
        width={width}
        height={200}
        style={styles.canvas}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 200,
    width: '100%',
    backgroundColor: 'transparent',
  },
  canvas: {
    width: '100%',
    height: '100%',
  },
}); 