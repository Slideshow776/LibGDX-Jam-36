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

    public static Sound coin_sound;
    public static Sound metal_sound;
    public static Sound glass_sound;
    public static Sound gum_sound;

    public static Array<Music> music;
    public static Music levelMusic;
    public static Music introMusic;
    public static Music endMusic;


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
        BaseGame.assetManager.load("audio/music/818285__xantherock__flash-animation-intro.mp3", Music.class);
        BaseGame.assetManager.load("audio/music/648562__xantherock__goofy-type-beat.mp3", Music.class);
        BaseGame.assetManager.load("audio/music/649935__xantherock__uh-oh.mp3", Music.class);

        // sounds
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load("audio/sounds/535890__jerimee__coin-jump.wav", Sound.class);
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load("audio/sounds/171246__oddworld__metalclank2.wav", Sound.class);
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load("audio/sounds/255658__spectral9__wine-glass-hit-b4.wav", Sound.class);
        no.sandramoen.libgdx35.utils.BaseGame.assetManager.load("audio/sounds/536765__egomassive__squish.wav", Sound.class);

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
        music = new Array();
        introMusic = BaseGame.assetManager.get("audio/music/818285__xantherock__flash-animation-intro.mp3", Music.class);
        levelMusic = BaseGame.assetManager.get("audio/music/648562__xantherock__goofy-type-beat.mp3", Music.class);
        endMusic = BaseGame.assetManager.get("audio/music/649935__xantherock__uh-oh.mp3", Music.class);
        music.add(introMusic);
        music.add(levelMusic);
        music.add(endMusic);

        // sounds
        coin_sound = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("audio/sounds/535890__jerimee__coin-jump.wav", Sound.class);
        metal_sound = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("audio/sounds/171246__oddworld__metalclank2.wav", Sound.class);
        glass_sound = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("audio/sounds/255658__spectral9__wine-glass-hit-b4.wav", Sound.class);
        gum_sound = no.sandramoen.libgdx35.utils.BaseGame.assetManager.get("audio/sounds/536765__egomassive__squish.wav", Sound.class);

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
        float scale = Gdx.graphics.getWidth() * .025f; // magic number ensures scale ~= 1, based on screen width
        scale *= 1.01f; // make x percent bigger, bigger = more fuzzy

        mySkin.get("Fredoka20white", Font.class).scale(scale);
        mySkin.get("Fredoka40white", Font.class).scale(scale);
        mySkin.get("Fredoka59white", Font.class).scale(scale);
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
