package com.jay.hideandseek.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.jay.hideandseek.Main;
import java.util.Random;

public class LoadingScreen implements Screen {
    private final Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private String currentQuote;
    private String loadingText = "Loading";
    private float dotTimer = 0;
    private final float dotInterval = 0.5f;
    private float progress;
    private boolean assetsLoaded;
    private float loadingTimer;
    private boolean isTransitioning = false;
    private Texture whitePixel; // Added texture for drawing rectangles

    private final String[] quotes = {
        "I don't hate San Antonio. I just don't like it.",
        "The only thing good in San Antonio is the River Walk.",
        "I was a fat guy. I didn't get offended when people said I was fat.",
        "The NBA is a big family, but we don't all like each other.",
        "If you go to a game and don't have fun, that's your fault!",
        "Some of these guys think because they can shoot threes, they're good. No.",
        "I may be wrong, but I doubt it.",
        "San Antonio got them big ol' women!",
        "Just because you watch YouTube doesn't make you an expert.",
        "I'm not a role model. Just because I dunk a basketball doesn't mean I should raise your kids."
    };

    private Random random;
    private Timer.Task quoteTask;
    private Timer.Task transitionTask;

    public LoadingScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        
        try {
            font = new BitmapFont(Gdx.files.internal("ui/default.fnt"));
        } catch (Exception e) {
            font = new BitmapFont();
            Gdx.app.error("LoadingScreen", "Error loading font: " + e.getMessage());
        }
        
        font.setColor(Color.WHITE);
        float fontScale = Gdx.graphics.getHeight() / 320f;
        font.getData().setScale(fontScale);

        // Create a 1x1 white pixel texture for drawing colored rectangles
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        layout = new GlyphLayout();
        random = new Random();
        updateQuote();
        
        this.progress = 0f;
        this.assetsLoaded = false;
        this.loadingTimer = 0f;
        
        quoteTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                updateQuote();
                Gdx.app.log("LoadingScreen", "Quote updated: " + currentQuote);
            }
        }, 6, 6);

        transitionTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.log("LoadingScreen", "Scheduling transition to MapSelectionScreen");
                isTransitioning = true;
            }
        }, 7);
        
        Gdx.app.log("LoadingScreen", "Loading screen initialized");
    }

    private void updateQuote() {
        String baseQuote = quotes[random.nextInt(quotes.length)];
        currentQuote = wrapText(baseQuote, 40) + "\n-- Charles Barkley";
    }

    private String wrapText(String text, int maxCharsPerLine) {
        String[] words = text.split(" ");
        StringBuilder wrappedText = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() + word.length() + 1 > maxCharsPerLine) {
                wrappedText.append(line.toString().trim()).append("\n");
                line.setLength(0);
            }
            line.append(word).append(" ");
        }

        if (!line.isEmpty()) {
            wrappedText.append(line.toString().trim());
        }

        return wrappedText.toString();
    }

    @Override
    public void show() {
        Gdx.app.log("LoadingScreen", "show() called");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        
        // Draw background with a dark semi-transparent overlay
        batch.setColor(0.2f, 0.2f, 0.3f, 1f);
        batch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1);

        // Animate Loading Text Dots
        dotTimer += delta;
        if (dotTimer >= dotInterval) {
            dotTimer = 0;
            loadingText = loadingText.equals("Loading...") ? "Loading" : loadingText + ".";
        }

        // Draw Quote
        layout.setText(font, currentQuote, Color.WHITE, Gdx.graphics.getWidth() * 0.8f, Align.center, true);
        font.draw(batch, layout,
            (Gdx.graphics.getWidth() - Gdx.graphics.getWidth() * 0.8f) / 2,
            Gdx.graphics.getHeight() * 0.6f
        );

        // Simulate loading progress
        if (!assetsLoaded) {
            progress += delta * 0.5f;
            if (progress >= 1.0f) {
                progress = 1.0f;
                assetsLoaded = true;
            }
        } else {
            loadingTimer += delta;
        }
        
        // Draw progress bar
        float barWidth = Gdx.graphics.getWidth() * 0.7f;
        float barHeight = 20;
        float barX = (Gdx.graphics.getWidth() - barWidth) / 2;
        float barY = Gdx.graphics.getHeight() * 0.1f;
        
        // Draw background bar (gray)
        batch.setColor(0.3f, 0.3f, 0.3f, 1);
        batch.draw(whitePixel, barX, barY, barWidth, barHeight);
        
        // Draw progress bar (orange)
        batch.setColor(1, 0.5f, 0, 1);
        batch.draw(whitePixel, barX, barY, barWidth * progress, barHeight);
        
        batch.setColor(1, 1, 1, 1);
        
        // Draw loading text with percentage
        String progressText = loadingText + " " + (int)(progress * 100) + "%";
        layout.setText(font, progressText);
        font.draw(batch, progressText, 
                  Gdx.graphics.getWidth() / 2 - layout.width / 2,
                  barY + barHeight + 30);

        batch.end();
        
        if (isTransitioning) {
            try {
                Gdx.app.log("LoadingScreen", "Transitioning to MapSelectionScreen now");
                game.setScreen(new MapSelectionScreen(game));
                isTransitioning = false;
            } catch (Exception e) {
                Gdx.app.error("LoadingScreen", "Error during transition: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("LoadingScreen", "resize() called");
    }

    @Override
    public void pause() {
        Gdx.app.log("LoadingScreen", "pause() called");
    }

    @Override
    public void resume() {
        Gdx.app.log("LoadingScreen", "resume() called");
    }

    @Override
    public void hide() {
        Gdx.app.log("LoadingScreen", "hide() called");
        if (quoteTask != null) quoteTask.cancel();
        if (transitionTask != null) transitionTask.cancel();
    }

    @Override
    public void dispose() {
        Gdx.app.log("LoadingScreen", "dispose() called");
        if (quoteTask != null) quoteTask.cancel();
        if (transitionTask != null) transitionTask.cancel();
        
        batch.dispose();
        font.dispose();
        whitePixel.dispose(); // Clean up our texture
    }
}