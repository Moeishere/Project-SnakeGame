package com.gamecodeschool.c17snake;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;

import java.io.IOException;

public class GameSoundManager {
    private SoundPool soundPool;
    private MediaPlayer menuMediaPlayer, gameMediaPlayer;
    private final Context context;
    private int eatSoundId = -1;
    private int drinkSoundId = -1;
    private int crashSoundId = -1;
    private int rockSoundId = -1;

    public GameSoundManager(Context context) {
        this.context = context;
        initializeSoundPool();
        initializeMediaPlayers();
    }
    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        loadSoundEffects();
    }
    private void loadSoundEffects() {
        eatSoundId = loadSound("snake_eat.mp3");
        drinkSoundId = loadSound("snake_drink.mp3");
        crashSoundId = loadSound("snake_death.mp3");
        rockSoundId = loadSound("rock_destroy.mp3");
    }
    private int loadSound(String fileName) {
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = context.getAssets().openFd(fileName);
            return soundPool.load(descriptor, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (descriptor != null) {
                try {
                    descriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void initializeMediaPlayers() {
        menuMediaPlayer = createMediaPlayer("menu_music.mp3");
        gameMediaPlayer = createMediaPlayer("ingame_music.mp3");
    }
    private MediaPlayer createMediaPlayer(String fileName) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd(fileName);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaPlayer;
    }
    public void playEatSound() {
        soundPool.play(eatSoundId, 1, 1, 0, 0, 1);
    }
    public void playDrinkSound() {
        soundPool.play(drinkSoundId, 1, 1, 0, 0, 1);
    }
    public void playCrashSound() {
        soundPool.play(crashSoundId, 1, 1, 0, 0, 1);
    }
    public void playRockSound() {
        soundPool.play(rockSoundId, 1, 1, 0, 0, 1);
    }
    public void playMenuMusic() {
        if (!menuMediaPlayer.isPlaying()) {
            menuMediaPlayer.start();
        }
    }
    public void stopMenuMusic() {
        if (menuMediaPlayer.isPlaying()) {
            menuMediaPlayer.pause();
        }
    }
    public void playGameMusic() {
        if (!gameMediaPlayer.isPlaying()) {
            gameMediaPlayer.start();
        }
    }
    public void stopGameMusic() {
        if (gameMediaPlayer.isPlaying()) {
            gameMediaPlayer.pause();
        }
    }
    public void releaseResources() {
        soundPool.release();
        menuMediaPlayer.release();
        gameMediaPlayer.release();
    }
}
