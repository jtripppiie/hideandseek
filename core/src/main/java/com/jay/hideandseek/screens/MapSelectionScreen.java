package com.jay.hideandseek.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.jay.hideandseek.Main;

public class MapSelectionScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture; // Texture for the background image

    public MapSelectionScreen(Main game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load the background image from the images folder
        backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Create buttons for map selection
        TextButton map1Button = new TextButton("Soldotna", skin);
        TextButton map2Button = new TextButton("Forest", skin);
        TextButton doNotPressButton = new TextButton("Do not press", skin);

        // Scale the button font based on screen height
        float fontScale = Gdx.graphics.getHeight() / 320f; // Adjusted base height for better scaling
        map1Button.getLabel().setFontScale(fontScale);
        map2Button.getLabel().setFontScale(fontScale);
        doNotPressButton.getLabel().setFontScale(fontScale);

        // Add listeners to buttons
        map1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SoldotnaMap());
            }
        });

        map2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ForestMap());
            }
        });

        doNotPressButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("vnd.youtube://ruX6oQeLC3M");
            }
        });

        // Create placeholders for thumbnails
        Texture map1Texture = new Texture(Gdx.files.internal("thumbnails/map1.png"));
        Texture map2Texture = new Texture(Gdx.files.internal("thumbnails/map2.png"));
        Texture doNotPressTexture = new Texture(Gdx.files.internal("thumbnails/do_not_press.png"));

        Image map1Thumbnail = new Image(new TextureRegionDrawable(new TextureRegion(map1Texture)));
        Image map2Thumbnail = new Image(new TextureRegionDrawable(new TextureRegion(map2Texture)));
        Image doNotPressThumbnail = new Image(new TextureRegionDrawable(new TextureRegion(doNotPressTexture)));

        // Maintain aspect ratio
        map1Thumbnail.setScaling(Scaling.fit);
        map2Thumbnail.setScaling(Scaling.fit);
        doNotPressThumbnail.setScaling(Scaling.fit);

        // Calculate relative sizes and padding
        float thumbnailWidth = Gdx.graphics.getWidth() * 0.3f; // Increased size
        float thumbnailHeight = Gdx.graphics.getHeight() * 0.2f; // Increased size
        float buttonWidth = Gdx.graphics.getWidth() * 0.25f;
        float buttonHeight = Gdx.graphics.getHeight() * 0.1f;
        float padding = Gdx.graphics.getWidth() * 0.02f;

        // Add thumbnails, buttons, and labels to table
        table.add(map1Thumbnail).pad(padding).width(thumbnailWidth).height(thumbnailHeight);
        table.add(map2Thumbnail).pad(padding).width(thumbnailWidth).height(thumbnailHeight);
        table.add(doNotPressThumbnail).pad(padding).width(thumbnailWidth).height(thumbnailHeight).row();

        table.add(map1Button).pad(padding).width(buttonWidth).height(buttonHeight);
        table.add(map2Button).pad(padding).width(buttonWidth).height(buttonHeight);
        table.add(doNotPressButton).pad(padding).width(buttonWidth).height(buttonHeight);
    }

    private Drawable createBorderDrawable(Color color) {
        int borderWidth = 5; // Adjust the border width as needed
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the background image
        stage.getBatch().begin();
        stage.getBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose(); // Dispose of the background texture
    }
}