package com.jay.hideandseek;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.jay.hideandseek.screens.LoadingScreen;

public class Main extends Game {
    private Screen currentScreen;
    
    @Override
    public void create() {
        Gdx.app.log("Main", "Creating game");
        setScreen(new LoadingScreen(this));
    }
    
    @Override
    public void setScreen(Screen screen) {
        // Dispose the current screen properly before switching
        if (currentScreen != null) {
            Gdx.app.log("Main", "Disposing previous screen: " + currentScreen.getClass().getSimpleName());
            currentScreen.hide();
            currentScreen.dispose();
        }
        
        currentScreen = screen;
        Gdx.app.log("Main", "Setting new screen: " + screen.getClass().getSimpleName());
        super.setScreen(screen);
    }
    
    @Override
    public void dispose() {
        Gdx.app.log("Main", "Disposing game");
        if (currentScreen != null) {
            currentScreen.dispose();
            currentScreen = null;
        }
        super.dispose();
    }
}
