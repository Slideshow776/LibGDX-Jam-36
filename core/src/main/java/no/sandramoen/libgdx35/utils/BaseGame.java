package no.sandramoen.libgdx35.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

public abstract class BaseGame extends Game implements AssetErrorListener {

    private static BaseGame game;
    public static AssetManager assetManager;

    public static Preferences preferences;
    public static boolean loadPersonalParameters;
    public static boolean isCustomShadersEnabled = true;
    public static boolean isHideUI = false;
    public static float voiceVolume = 1f;
    public static float soundVolume = .5f;
    public static float musicVolume = .1f;
    public static float vibrationStrength = 1f;
    public static final float UNIT_SCALE = 1 / 16f;
    public static final float WORLD_WIDTH = 14f;
    public static final float WORLD_HEIGHT = 14f;
    public static final float PIXELS_PER_METER = 100f;

    public static int high_score = 0;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public static Difficulty current_difficulty;

    public BaseGame() {
        game = this;
    }

    @Override
    public void create() {
        Gdx.input.setInputProcessor(new InputMultiplexer());
        loadGameState();
        new AssetLoader();

        Pixmap pixmap = new Pixmap(Gdx.files.internal("images/excluded/cursor.png"));
        Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap.dispose();
        Gdx.graphics.setCursor(cursor);
    }

    public static void setActiveScreen(BaseScreen screen) {
        game.setScreen(screen);
    }

    public static float toPixels(float meters) {
        return meters * PIXELS_PER_METER;
    }

    public static float toMeters(float pixels) {
        return pixels / PIXELS_PER_METER;
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            assetManager.dispose();
        } catch (Error error) {
            Gdx.app.error(this.getClass().getSimpleName(), error.toString());
        }
    }

    @Override
    public void error(AssetDescriptor asset, Throwable throwable) {
        Gdx.app.error(this.getClass().getSimpleName(), "Could not load asset: " + asset.fileName, throwable);
    }

    private void loadGameState() {
        GameUtils.loadGameState();
        if (!loadPersonalParameters) {
            soundVolume = .75f;
            musicVolume = .5f;
            voiceVolume = 1f;
        }
    }
}
