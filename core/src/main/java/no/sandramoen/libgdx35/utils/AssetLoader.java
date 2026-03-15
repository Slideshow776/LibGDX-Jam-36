package no.sandramoen.libgdx35.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.FWSkin;
import com.github.tommyettinger.textra.FWSkinLoader;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles;

public class AssetLoader implements AssetErrorListener {

    public static TextureAtlas textureAtlas;
    public static FWSkin mySkin;

    public static String defaultShader;
    public static String shockwaveShader;
    public static String backgroundShader;

    public static Sound new_letters_sound;
    public static Sound move_sound;
    public static Sound error_sound;
    public static Sound game_over_sound;
    public static Sound game_start_sound;
    public static Sound key_sound;
    public static Sound locked_sound;
    public static Sound door_open_sound;

    public static Array<Music> music;
    //public static Music levelMusic;


    static {
        long time = System.currentTimeMillis();
        no.sandramoen.libgdx35.utils.BaseGame.assetManager = new AssetManager();
        no.sandramoen.libgdx35.utils.BaseGame.assetManager. setLoader(Skin. class, new FWSkinLoader(no.sandramoen.libgdx35.utils.BaseGame.assetManager. getFileHandleResolver()));
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.setErrorListener(new AssetLoader());

        loadAssets();
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.finishLoading();
        assignAssets();

        Gdx.app.log(AssetLoader.class.getSimpleName(), "Asset manager took " + (System.currentTimeMillis() - time) + " ms to load all game assets.");
    }

    @Override
    public void error(AssetDescriptor asset, Throwable throwable) {
        Gdx.app.error(AssetLoader.class.getSimpleName(), "Could not load asset: " + asset.fileName, throwable);
    }


    public static Styles.LabelStyle getLabelStyle(String fontName) {
        return new Styles.LabelStyle(
            new Font(
                AssetLoader.mySkin.get(fontName, Font.class)
            ), Color.WHITE);
    }

    private static void loadAssets() {
        // images
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.setLoader(no.sandramoen.libgdx35.utils.Text.class, new no.sandramoen.libgdx35.utils.TextLoader(new InternalFileHandleResolver()));
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load("images/included/packed/images.pack.atlas", TextureAtlas.class);

        // music
        //BaseGame.assetManager.load("audio/music/744138__thelastoneonearth__epic-middle-east-theme.ogg", Music.class);

        // sounds
        //no.sandramoen.libgdx35.utils.BaseGame.assetManager.load("audio/sounds/191511__hitrison__quick-chain-drops.wav", Sound.class);

        // i18n

        // shaders
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load(new AssetDescriptor("shaders/default.vs", no.sandramoen.libgdx35.utils.Text.class, new no.sandramoen.libgdx35.utils.TextLoader.TextParameter()));
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load(new AssetDescriptor("shaders/shockwave.fs", no.sandramoen.libgdx35.utils.Text.class, new no.sandramoen.libgdx35.utils.TextLoader.TextParameter()));
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load(new AssetDescriptor("shaders/voronoi.fs", no.sandramoen.libgdx35.utils.Text.class, new no.sandramoen.libgdx35.utils.TextLoader.TextParameter()));

        // skins

        // fonts

        // tiled maps
        //BaseGame.assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        //BaseGame.assetManager.load("maps/test.tmx", TiledMap.class);

        // other
        // BaseGame.assetManager.load(AssetDescriptor("other/jentenavn.csv", Text::class.java, TextLoader.TextParameter()))
    }

    private static void assignAssets() {
        // images
        textureAtlas = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("images/included/packed/images.pack.atlas");

        // music
        //music = new Array();
        //levelMusic = BaseGame.assetManager.get("audio/music/744138__thelastoneonearth__epic-middle-east-theme.ogg", Music.class);
        //music.add(levelMusic);

        // sounds
        //new_letters_sound = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("audio/sounds/191511__hitrison__quick-chain-drops.wav", Sound.class);

        // i18n

        // shaders
        defaultShader = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("shaders/default.vs", no.sandramoen.libgdx35.utils.Text.class).getString();
        shockwaveShader = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("shaders/shockwave.fs", no.sandramoen.libgdx35.utils.Text.class).getString();
        backgroundShader = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("shaders/voronoi.fs", no.sandramoen.libgdx35.utils.Text.class).getString();

        // skins
        mySkin = new FWSkin(Gdx.files.internal("skins/mySkin/mySkin.json"));

        // fonts
        loadFonts();

        // tiled maps
        //loadTiledMap();

        // other
    }

    private static void loadFonts() {
        float scale = Gdx.graphics.getWidth() * .05f; // magic number ensures scale ~= 1, based on screen width
        scale *= 1.01f; // make x percent bigger, bigger = more fuzzy

        mySkin.get("Alegreya20white", Font.class).scale(scale);
        mySkin.get("Alegreya40white", Font.class).scale(scale);
        mySkin.get("Alegreya59white", Font.class).scale(scale);
    }

    private static void loadTiledMap() {
        /*testMap = BaseGame.assetManager.get("maps/test.tmx", TiledMap.class);
        level1 = BaseGame.assetManager.get("maps/level1.tmx", TiledMap.class);
        level2 = BaseGame.assetManager.get("maps/level2.tmx", TiledMap.class);

        maps = new Array();
        maps.add(testMap);
        maps.add(level1);
        maps.add(level2);*/
    }
}
