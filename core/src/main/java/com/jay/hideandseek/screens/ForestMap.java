package com.jay.hideandseek.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.jay.hideandseek.characters.Hider;
import com.jay.hideandseek.characters.Seeker;
import com.jay.hideandseek.utils.AudioManager;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class ForestMap implements Screen, InputProcessor {
    private static final float HIDE_TIME = 20f;
    private static final float SEEK_TIME = 20f;

    private enum GameState {
        HIDING, SEEKING, FINISHED
    }

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Texture backgroundTexture;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final Hider hiderCharacter;
    private final Seeker seekerCharacter;
    private final OrthographicCamera camera;

    private GameState currentState = GameState.HIDING;
    private float hideTimer = HIDE_TIME;
    private float seekTimer = SEEK_TIME;
    private boolean hiderVisible = true;
    private boolean seekerActive = false;
    private boolean gameWon = false;

    public ForestMap() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture(Gdx.files.internal("images/forest_background.png"));
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camera.update();
        
        hiderCharacter = new Hider();
        seekerCharacter = new Seeker();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void show() {
        AudioManager.getInstance().playSound("forest_ambience");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateGameState(delta);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderCharacters();
        renderUI();
        batch.end();
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

    private void renderCharacters() {
        if (hiderVisible) {
            batch.draw(hiderCharacter.getTexture(), hiderCharacter.getPosition().x, hiderCharacter.getPosition().y, 
                hiderCharacter.getWidth(), hiderCharacter.getHeight());
        }
        if (seekerActive) {
            batch.draw(seekerCharacter.getTexture(), seekerCharacter.getPosition().x, seekerCharacter.getPosition().y, 
                seekerCharacter.getWidth(), seekerCharacter.getHeight());
        }
    }

    private void renderUI() {
        if (currentState == GameState.HIDING) {
            font.draw(batch, "Hide time left: " + (int)hideTimer, 10, Gdx.graphics.getHeight() - 10);
        } else if (currentState == GameState.SEEKING) {
            font.draw(batch, "Seek time left: " + (int)seekTimer, 10, Gdx.graphics.getHeight() - 10);
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);

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
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);

        if (currentState == GameState.HIDING && hiderVisible) {
            hiderCharacter.getPosition().set(touchPos.x, touchPos.y);
        } else if (currentState == GameState.SEEKING && seekerActive) {
            seekerCharacter.getPosition().set(touchPos.x, touchPos.y);
        }
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

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
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        font.dispose();
        hiderCharacter.dispose();
        seekerCharacter.dispose();
    }
}