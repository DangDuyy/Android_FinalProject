import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, Dimensions } from 'react-native';
import { Audio } from 'expo-av';
import { MaterialIcons } from '@expo/vector-icons';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { MusicVisualizer } from '../components/MusicVisualizer';
import { useMusicPlayer } from '../contexts/MusicPlayerContext';
import { useTheme } from '../contexts/ThemeContext';

export default function PlayerScreen() {
  const [currentTrack, setCurrentTrack] = useState(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [visualizerType, setVisualizerType] = useState<'waveform' | 'spectrum'>('waveform');
  const [visualizerColor, setVisualizerColor] = useState('#FF6B6B');

  const router = useRouter();
  const theme = useTheme();

  return (
    <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <MaterialIcons name="arrow-back" size={24} color={theme.colors.text} />
        </TouchableOpacity>
        <Text style={[styles.headerTitle, { color: theme.colors.text }]}>Now Playing</Text>
        <TouchableOpacity onPress={handleMorePress} style={styles.moreButton}>
          <MaterialIcons name="more-vert" size={24} color={theme.colors.text} />
        </TouchableOpacity>
      </View>

      <View style={styles.artworkContainer}>
        <Image
          source={{ uri: currentTrack?.artwork }}
          style={styles.artwork}
        />
      </View>

      <View style={styles.visualizerContainer}>
        <MusicVisualizer
          audioUrl={currentTrack?.url || ''}
          isPlaying={isPlaying}
          visualizerType={visualizerType}
          color={visualizerColor}
        />
        <View style={styles.visualizerControls}>
          <TouchableOpacity
            onPress={() => setVisualizerType(visualizerType === 'waveform' ? 'spectrum' : 'waveform')}
            style={styles.visualizerButton}
          >
            <MaterialIcons
              name={visualizerType === 'waveform' ? 'graphic-eq' : 'waves'}
              size={24}
              color={theme.colors.text}
            />
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEEAD'];
              const currentIndex = colors.indexOf(visualizerColor);
              const nextIndex = (currentIndex + 1) % colors.length;
              setVisualizerColor(colors[nextIndex]);
            }}
            style={styles.visualizerButton}
          >
            <MaterialIcons name="palette" size={24} color={theme.colors.text} />
          </TouchableOpacity>
        </View>
      </View>

      <View style={styles.infoContainer}>
        <Text style={[styles.title, { color: theme.colors.text }]} numberOfLines={1}>
          {currentTrack?.title || 'Unknown Title'}
        </Text>
        <Text style={[styles.artist, { color: theme.colors.textSecondary }]} numberOfLines={1}>
          {currentTrack?.artist || 'Unknown Artist'}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 10,
  },
  backButton: {
    padding: 10,
  },
  headerTitle: {
    flex: 1,
    fontSize: 18,
    fontWeight: 'bold',
  },
  moreButton: {
    padding: 10,
  },
  artworkContainer: {
    width: '100%',
    height: 200,
    marginVertical: 20,
  },
  artwork: {
    width: '100%',
    height: '100%',
    borderRadius: 10,
  },
  visualizerContainer: {
    width: '100%',
    height: 200,
    marginVertical: 20,
  },
  visualizerControls: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 10,
  },
  visualizerButton: {
    padding: 10,
    marginHorizontal: 10,
    borderRadius: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  infoContainer: {
    padding: 10,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  artist: {
    fontSize: 16,
    color: 'gray',
  },
}); 