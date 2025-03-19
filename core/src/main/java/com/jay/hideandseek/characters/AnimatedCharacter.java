package com.jay.hideandseek.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public abstract class AnimatedCharacter implements Disposable {
    protected Texture spriteSheet;
    protected Animation<TextureRegion> walkAnimation;
    protected Animation<TextureRegion> idleAnimation;
    protected Vector2 position;
    protected float stateTime;
    protected boolean isMoving;
    protected float speed;
    protected float lastX, lastY;
    
    public AnimatedCharacter(String spriteSheetPath, int frameRows, int frameCols, float frameDuration) {
        spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
        position = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        stateTime = 0;
        isMoving = false;
        speed = 200;
        lastX = position.x;
        lastY = position.y;
        
        // Create animations from sprite sheet
        initializeAnimations(frameRows, frameCols, frameDuration);
    }
    
    protected void initializeAnimations(int frameRows, int frameCols, float frameDuration) {
        // Split sprite sheet into frames
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, 
            spriteSheet.getWidth() / frameCols, 
            spriteSheet.getHeight() / frameRows);
        
        // Create walk animation frames
        TextureRegion[] walkFrames = new TextureRegion[frameCols];
        for (int i = 0; i < frameCols; i++) {
            walkFrames[i] = tmp[0][i]; // First row for walking
        }
        walkAnimation = new Animation<>(frameDuration, walkFrames);
        
        // Create idle animation frames (can be just one frame)
        TextureRegion[] idleFrames = new TextureRegion[1];
        idleFrames[0] = tmp[0][0]; // First frame for idle
        idleAnimation = new Animation<>(frameDuration, idleFrames);
    }
    
    public void update(float delta) {
        stateTime += delta;
        
        // Check if the character has moved
        isMoving = position.x != lastX || position.y != lastY;
        lastX = position.x;
        lastY = position.y;
    }
    
    public void render(SpriteBatch batch) {
        // Get current frame
        TextureRegion currentFrame;
        if (isMoving) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }
        
        // Draw the character
        batch.draw(currentFrame, position.x, position.y, 
                  getWidth(), getHeight());
    }
    
    public Vector2 getPosition() {
        return position;
    }
    
    public float getWidth() {
        return walkAnimation.getKeyFrame(0).getRegionWidth() * (Gdx.graphics.getWidth() / 1920f);
    }
    
    public float getHeight() {
        return walkAnimation.getKeyFrame(0).getRegionHeight() * (Gdx.graphics.getHeight() / 1080f);
    }
    
    @Override
    public void dispose() {
        spriteSheet.dispose();
    }
}