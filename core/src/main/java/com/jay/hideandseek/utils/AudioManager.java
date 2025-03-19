package com.jay.hideandseek.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class AudioManager implements Disposable {
    private static AudioManager instance;
    
    private ObjectMap<String, Sound> sounds;
    private float volume = 1.0f;
    private boolean soundEnabled = true;
    
    private AudioManager() {
        sounds = new ObjectMap<>();
        loadSounds();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    private void loadSounds() {
        // Load common sound effects
        addSound("click", "sounds/click.wav");
        addSound("win", "sounds/win.wav");
        addSound("lose", "sounds/lose.wav");
        addSound("hide", "sounds/hide.wav");
        addSound("seek", "sounds/seek.wav");
        
        Gdx.app.log("AudioManager", "Sounds loaded: " + sounds.size);
    }
    
    private void addSound(String name, String path) {
        try {
            sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error loading sound: " + path, e);
            // Silently continue if file is missing
        }
    }
    
    public void playSound(String name) {
        if (soundEnabled && sounds.containsKey(name)) {
            sounds.get(name).play(volume);
        }
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    @Override
    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
        instance = null;
    }
}