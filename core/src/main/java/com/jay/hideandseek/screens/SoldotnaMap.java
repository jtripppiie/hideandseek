package com.jay.hideandseek.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.jay.hideandseek.characters.Hider;
import com.jay.hideandseek.characters.Seeker;
import com.jay.hideandseek.utils.AudioManager;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;

public class SoldotnaMap implements Screen, InputProcessor {
    private static final float HIDE_TIME = 20f;
    private static final float SEEK_TIME = 20f;
    private static final int INITIAL_SNOWFLAKE_COUNT = 300;
    private static final float TRANSITION_WAIT_TIME = 0.1f;
    private static final float WEATHER_CHANGE_INTERVAL = 15f;

    private enum GameState {
        HIDING, SEEKING, FINISHED
    }

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Texture backgroundTexture;
    private final Texture thermometerTexture;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final Hider hiderCharacter;
    private final Seeker seekerCharacter;
    private final Array<Snowflake> snowflakes;
    private final Array<Button> endGameButtons;
    private final OrthographicCamera camera;

    private GameState currentState = GameState.HIDING;
    private float hideTimer = HIDE_TIME;
    private float seekTimer = SEEK_TIME;
    private boolean hiderVisible = true;
    private boolean seekerActive = false;
    private boolean gameWon = false;
    private boolean pendingTransition = false;
    private float transitionDelay = 0f;
    private int temperature;
    private float timeElapsed = 20f;
    private boolean isDayTime = true;
    private float timeOfDayTransition = 0f;
    private float dayNightCycleTime = 60f;
    private float weatherIntensity = 0.5f;
    private float weatherChangeTimer = 0f;

    public SoldotnaMap() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture(Gdx.files.internal("images/alaska_background.png"));
        thermometerTexture = new Texture(Gdx.files.internal("images/thermometer.png"));
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
        
        snowflakes = new Array<>();
        initializeSnow(INITIAL_SNOWFLAKE_COUNT);
        
        hiderCharacter = new Hider();
        seekerCharacter = new Seeker();
        temperature = MathUtils.random(-20, 0);
        
        endGameButtons = new Array<>();
        initializeButtons();

        // Register this class as the input processor
        Gdx.input.setInputProcessor(this);
    }
    
    private void initializeSnow(int count) {
        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(0f, Gdx.graphics.getWidth());
            float y = MathUtils.random(0f, Gdx.graphics.getHeight());
            snowflakes.add(new Snowflake(x, y));
        }
    }
    
    private void initializeButtons() {
        float centerX = Gdx.graphics.getWidth() / 2f - 100;
        endGameButtons.add(new Button(
            centerX, Gdx.graphics.getHeight() / 2f, 200, 60,
            "Play Again", this::resetGame
        ));
        endGameButtons.add(new Button(
            centerX, Gdx.graphics.getHeight() / 2f - 100, 200, 60,
            "Back to Menu", () -> {
                pendingTransition = true;
                transitionDelay = 0f;
            }
        ));
    }

    @Override
    public void show() {
        AudioManager.getInstance().playSound("wind");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        timeElapsed += delta;
        
        // Update game state
        updateGameState(delta);
        
        // Update environment
        timeOfDayTransition += delta;
        if (timeOfDayTransition > dayNightCycleTime) {
            timeOfDayTransition = 0f;
            isDayTime = !isDayTime;
        }
        
        weatherChangeTimer += delta;
        if (weatherChangeTimer > WEATHER_CHANGE_INTERVAL) {
            weatherChangeTimer = 0f;
            weatherIntensity = MathUtils.random(0.2f, 1.0f);
            adjustSnowflakes();
        }
        
        updateSnow(delta);
        
        // Render
        batch.begin();
        
        float timeOfDayProgress = timeOfDayTransition / dayNightCycleTime;
        float lightLevel = isDayTime ? 
            Math.min(1.0f, 0.7f + 0.3f * (float)Math.sin(timeOfDayProgress * Math.PI)) :
            Math.max(0.3f, 0.7f - 0.4f * (float)Math.sin(timeOfDayProgress * Math.PI));
        
        batch.setColor(lightLevel, lightLevel, lightLevel, 1); // Fixed purple tint
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1);
        
        renderCharacters();
        renderUI();
        
        if (currentState == GameState.FINISHED) {
            renderEndGameUI();
        }
        
        batch.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Snowflake snowflake : snowflakes) {
            snowflake.draw(shapeRenderer); // Snow stays white
        }
        shapeRenderer.end();
        
        if (pendingTransition) {
            transitionDelay += delta;
            if (transitionDelay >= TRANSITION_WAIT_TIME) {
                executeScreenTransition();
            }
        }
    }
    
    private void updateGameState(float delta) {
        switch (currentState) {
            case HIDING:
                hideTimer -= delta;
                if (hideTimer <= 0) {
                    hideTimer = 0;
                    hiderVisible = false;
                    seekerActive = true;
                    currentState = GameState.SEEKING;
                }
                if (hiderVisible) {
                    hiderCharacter.update(delta);
                }
                break;
            case SEEKING:
                seekTimer -= delta;
                if (seekerActive) {
                    seekerCharacter.update(delta);
                    if (checkCollision()) {
                        gameWon = true;
                        seekerActive = false;
                        currentState = GameState.FINISHED;
                    }
                }
                if (seekTimer <= 0) {
                    seekTimer = 0;
                    currentState = GameState.FINISHED;
                }
                break;
            case FINISHED:
                break;
        }
    }
    
    private boolean checkCollision() {
        Rectangle seekerRect = new Rectangle(seekerCharacter.getPosition().x, seekerCharacter.getPosition().y, 
            seekerCharacter.getWidth(), seekerCharacter.getHeight());
        Rectangle hiderRect = new Rectangle(hiderCharacter.getPosition().x, hiderCharacter.getPosition().y, 
            hiderCharacter.getWidth(), hiderCharacter.getHeight());
        return seekerRect.overlaps(hiderRect);
    }
    
    private void updateSnow(float delta) {
        for (Snowflake snowflake : snowflakes) {
            snowflake.update(delta);
            if (snowflake.y < 0) {
                snowflake.y = Gdx.graphics.getHeight() + MathUtils.random(0f, 50f);
                snowflake.x = MathUtils.random(0f, Gdx.graphics.getWidth());
            }
        }
    }
    
    private void adjustSnowflakes() {
        int targetSnowflakes = (int)(200 + 300 * weatherIntensity);
        while (snowflakes.size < targetSnowflakes) {
            snowflakes.add(new Snowflake(
                MathUtils.random(0f, Gdx.graphics.getWidth()),
                Gdx.graphics.getHeight() + MathUtils.random(0f, 50f)
            ));
        }
        while (snowflakes.size > targetSnowflakes) {
            snowflakes.removeIndex(snowflakes.size - 1);
        }
    }
    
    private void renderCharacters() {
        Array<RenderableObject> renderableObjects = new Array<>();
        if (hiderVisible) {
            renderableObjects.add(new RenderableObject(hiderCharacter.getTexture(), 
                hiderCharacter.getPosition(), hiderCharacter.getWidth() * 1.5f, hiderCharacter.getHeight() * 1.5f));
        }
        if (seekerActive) {
            renderableObjects.add(new RenderableObject(seekerCharacter.getTexture(), 
                seekerCharacter.getPosition(), seekerCharacter.getWidth() * 1.5f, seekerCharacter.getHeight() * 1.5f));
        }
        
        renderableObjects.sort((o1, o2) -> Float.compare(o2.position.y, o1.position.y));
        for (RenderableObject obj : renderableObjects) {
            batch.draw(obj.texture, obj.position.x, obj.position.y, obj.width, obj.height);
        }
    }
    
    private void renderUI() {
        if (currentState == GameState.HIDING) {
            font.draw(batch, "Hide time left: " + (int)hideTimer, 10, Gdx.graphics.getHeight() - 10);
        } else if (currentState == GameState.SEEKING) {
            font.draw(batch, "Seek time left: " + (int)seekTimer, 10, Gdx.graphics.getHeight() - 10);
        }
        font.draw(batch, "Temperature: " + temperature + "°F", 10, Gdx.graphics.getHeight() - 30);
    }
    
    private void renderEndGameUI() {
        String resultText = gameWon ? "Seeker Wins!" : "Hider Wins!";
        font.getData().setScale(2.5f);
        font.setColor(0, 0, 0, 0.5f);
        glyphLayout.setText(font, resultText);
        font.draw(batch, resultText, 
            Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2 + 3, 
            Gdx.graphics.getHeight() * 2/3 + 3);
        
        font.setColor(0.2f, 0.6f, 1f, 1);
        font.draw(batch, resultText, 
            Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, 
            Gdx.graphics.getHeight() * 2/3);
        font.getData().setScale(1);
        
        for (Button button : endGameButtons) {
            button.render(batch, font, backgroundTexture);
        }
        
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            for (Button button : endGameButtons) {
                if (button.handleClick(touchX, touchY)) {
                    AudioManager.getInstance().playSound("click");
                    break;
                }
            }
        }
    }

    private void resetGame() {
        currentState = GameState.HIDING;
        hideTimer = HIDE_TIME;
        seekTimer = SEEK_TIME;
        hiderVisible = true;
        seekerActive = false;
        gameWon = false;
        temperature = MathUtils.random(-20, 0);
        hiderCharacter.getPosition().set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        seekerCharacter.getPosition().set(100, 100);
    }
    
    private void executeScreenTransition() {
        com.jay.hideandseek.Main game = (com.jay.hideandseek.Main)Gdx.app.getApplicationListener();
        MapSelectionScreen mapSelectionScreen = new MapSelectionScreen(game);
        game.setScreen(mapSelectionScreen);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        thermometerTexture.dispose();
        font.dispose();
        hiderCharacter.dispose();
        seekerCharacter.dispose();
    }
    
    private static class Snowflake {
        float x, y;
        float size;
        float speed;
        float angle;
        
        public Snowflake(float x, float y) {
            this.x = x;
            this.y = y;
            this.size = MathUtils.random(1f, 3f);
            this.speed = 30f + this.size * 10f;
            this.angle = MathUtils.random(0f, 360f);
        }
        
        public void update(float delta) {
            y -= speed * delta;
            angle += MathUtils.random(0.1f, 0.5f);
            x += Math.sin(angle) * MathUtils.random(0.5f, 1.5f);
        }
        
        public void draw(ShapeRenderer renderer) {
            renderer.setColor(1, 1, 1, 0.7f); // Ensure snow stays white
            renderer.circle(x, y, size);
        }
    }
    
    private static class RenderableObject {
        Texture texture;
        Vector2 position;
        float width;
        float height;
        
        RenderableObject(Texture texture, Vector2 position, float width, float height) {
            this.texture = texture;
            this.position = position;
            this.width = width;
            this.height = height;
        }
    }
    
    private static class Button {
        private final Rectangle bounds;
        private final String text;
        private final Runnable action;
        
        public Button(float x, float y, float width, float height, String text, Runnable action) {
            this.bounds = new Rectangle(x, y, width, height);
            this.text = text;
            this.action = action;
        }
        
        public void render(SpriteBatch batch, BitmapFont font, Texture texture) {
            batch.setColor(0.2f, 0.2f, 0.3f, 0.8f);
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
            
            batch.setColor(0.5f, 0.7f, 1f, 0.5f);
            batch.draw(texture, bounds.x, bounds.y + bounds.height - 2, bounds.width, 2);
            batch.draw(texture, bounds.x, bounds.y, bounds.width, 2);
            batch.draw(texture, bounds.x, bounds.y, 2, bounds.height);
            batch.draw(texture, bounds.x + bounds.width - 2, bounds.y, 2, bounds.height);
            
            batch.setColor(1, 1, 1, 1);
            font.getData().setScale(1.2f);
            GlyphLayout layout = new GlyphLayout(font, text);
            font.draw(batch, text, 
                bounds.x + (bounds.width - layout.width) / 2, 
                bounds.y + (bounds.height + layout.height) / 2);
            font.getData().setScale(1f);
        }
        
        public boolean handleClick(float x, float y) {
            if (bounds.contains(x, y)) {
                action.run();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Convert screen coordinates to world coordinates
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);

        // Update hider or seeker position based on the current game state
        if (currentState == GameState.HIDING && hiderVisible) {
            hiderCharacter.getPosition().set(touchPos.x, touchPos.y);
        } else if (currentState == GameState.SEEKING && seekerActive) {
            seekerCharacter.getPosition().set(touchPos.x, touchPos.y);
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Convert screen coordinates to world coordinates
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);

        // Update hider or seeker position based on the current game state
        if (currentState == GameState.HIDING && hiderVisible) {
            hiderCharacter.getPosition().set(touchPos.x, touchPos.y);
        } else if (currentState == GameState.SEEKING && seekerActive) {
            seekerCharacter.getPosition().set(touchPos.x, touchPos.y);
        }

        return true;
    }

    // Implement other InputProcessor methods as no-op
    @Override
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}