package com.jay.hideandseek.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input.Keys;

public class Seeker {
    private Texture texture;
    private Vector2 position;
    private float speed;
    private float speedBonus = 0f;

    public Seeker() {
        texture = new Texture(Gdx.files.internal("images/seeker.png"));
        position = new Vector2(100, 100); // Start position
        speed = 180; // Speed in pixels per second
    }

    public void setSpeedBonus(float bonus) {
        this.speedBonus = bonus;
    }

    public void update(float delta) {
        float moveSpeed = speed + speedBonus;

        // Handle movement input with arrow keys
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            position.x -= moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            position.x += moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            position.y += moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            position.y -= moveSpeed * delta;
        }

        // Ensure the seeker stays within the screen bounds
        position.x = Math.max(0, Math.min(position.x, Gdx.graphics.getWidth() - texture.getWidth()));
        position.y = Math.max(0, Math.min(position.y, Gdx.graphics.getHeight() - texture.getHeight()));
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
    }

    public void dispose() {
        texture.dispose();
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getWidth() {
        return texture.getWidth() * (Gdx.graphics.getWidth() / 1920f);
    }

    public float getHeight() {
        return texture.getHeight() * (Gdx.graphics.getHeight() / 1080f);
    }
}
