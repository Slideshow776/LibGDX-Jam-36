package no.sandramoen.libgdx35.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TextraLabel;
import no.sandramoen.libgdx35.actors.pinball.*;
import no.sandramoen.libgdx35.utils.AssetLoader;
import no.sandramoen.libgdx35.utils.BaseGame;
import no.sandramoen.libgdx35.utils.BaseScreen;

public class LevelScreen extends BaseScreen {

    private static final int CHUNKS_TO_KEEP_BELOW_BALL = 4;
    private static final float BALL_LAUNCH_SPEED = 40f;
    private static final float BALL_MIN_UPWARD = 20.75f;
    private static final int BACKGROUND_TILE_COUNT = 6;

    private static final float FLIP_WINDOW = 1.0f;
    private static final int FLIP_LIMIT = 6;
    private static final float FLIPPER_COOLDOWN_DURATION = 2.0f;

    private float bumperFlipCount;
    private float bumperFlipTime;
    private float flipperCooldownTimer;
    private float cooldownFlashTimer;

    private Texture backgroundTexture;
    private Array<Image> backgrounds;

    private Ball ball;
    private World world;
    private Array<Chunk> chunks;

    private float chunkWidth;
    private float chunkHeight;
    private float generatedTopY;
    private float failureBottomY;
    private float startBallY;
    private float highestBallY;

    private TextraLabel statsLabel;
    private TextraLabel cooldownLabel;
    private int coinCount;

    @Override
    public void initialize() {
        this.backgrounds = new Array<>();
        this.backgroundTexture = new Texture(Gdx.files.internal("images/included/background.png"));

        chunks = new Array<>();
        world = new World(new Vector2(0f, -24f), true);
        world.setContactListener(new PlatformContactListener());

        for (int i = 0; i < BACKGROUND_TILE_COUNT; i++) {
            Image bg = new Image(backgroundTexture);
            backgrounds.add(bg);
            mainStage.addActor(bg);
            bg.toBack();
        }

        statsLabel = new TextraLabel("0", AssetLoader.getLabelStyle("Fredoka20white"));
        statsLabel.setPosition(20f, Gdx.graphics.getHeight() - 70f);
        uiTable.add(statsLabel).expand().center().top();

        cooldownLabel = new TextraLabel("FLIPPERS ON COOLDOWN", AssetLoader.getLabelStyle("Fredoka20white"));
        cooldownLabel.setColor(Color.RED);
        cooldownLabel.setVisible(false);
        uiStage.addActor(cooldownLabel);

        chunkWidth = Gdx.graphics.getWidth();
        chunkHeight = 2000f;

        generateChunk(0f);
        generateChunk(chunkHeight);
        generateChunk(chunkHeight * 2f);

        failureBottomY = chunks.first().getBottomY();

        ball = new Ball(world, chunkWidth * .5f, 1000f, 96, 96, Material.GUM, mainStage);
        startBallY = ball.getY();
        highestBallY = startBallY;
        coinCount = 0;

        launchBallDiagonally();
        updateStatsLabel();

        this.camera.zoom = 1.8f;
        mainStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        AssetLoader.levelMusic.setLooping(true);
        AssetLoader.levelMusic.setVolume(0f);
        AssetLoader.levelMusic.play();

        AssetLoader.introMusic.setVolume(BaseGame.musicVolume);
        AssetLoader.introMusic.play();

        AssetLoader.endMusic.setVolume(BaseGame.musicVolume);

        updateBackgrounds();
        updateCooldownLabel();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }

    private void generateChunk(float y) {
        Chunk chunk = new Chunk(world, mainStage, 0f, y, chunkWidth, chunkHeight);
        chunks.add(chunk);
        generatedTopY = Math.max(generatedTopY, chunk.getTopY());
    }

    @Override
    public void update(float delta) {
        bumperFlipTime += delta;
        if (bumperFlipTime > FLIP_WINDOW) {
            bumperFlipTime = 0f;
            bumperFlipCount = 0f;
        }

        if (flipperCooldownTimer > 0f) {
            flipperCooldownTimer -= delta;
            if (flipperCooldownTimer < 0f) {
                flipperCooldownTimer = 0f;
            }
            cooldownFlashTimer += delta;
        } else {
            cooldownFlashTimer = 0f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (flipperCooldownTimer <= 0f) {
                bumperFlipCount++;
                if (bumperFlipCount >= FLIP_LIMIT) {
                    flipperCooldownTimer = FLIPPER_COOLDOWN_DURATION;
                    bumperFlipCount = 0f;
                    bumperFlipTime = 0f;
                }
            }
        }

        updateCooldownLabel();

        Chunk activeChunk = getChunkForBallY(ball.getY());
        if (activeChunk != null) {
            boolean allowFlippers = flipperCooldownTimer <= 0f;

            for (PlatformBumper bumper : activeChunk.getBumpers()) {
                if (bumper.isLeft()) {
                    bumper.setRaised(allowFlippers && Gdx.input.isKeyPressed(Input.Keys.LEFT));
                } else {
                    bumper.setRaised(allowFlippers && Gdx.input.isKeyPressed(Input.Keys.RIGHT));
                }
            }
        }

        while (ball.getY() + Gdx.graphics.getHeight() * 2f > generatedTopY) {
            generateChunk(generatedTopY);
        }

        unloadChunksBelowBall();

        world.step(1f / 120f, 12, 6);

        collectCoins();

        highestBallY = Math.max(highestBallY, ball.getY());
        updateStatsLabel();

        if (ball.getY() + ball.getHeight() < failureBottomY) {
            failBall();
            return;
        }

        OrthographicCamera cam = (OrthographicCamera) mainStage.getCamera();
        cam.position.y += (ball.getY() - cam.position.y) * 0.08f;
        cam.update();

        updateBackgrounds();

        for (int i = 0; i < chunks.size; i++) {
            chunks.get(i).extendWallsToCamera(cam, viewport.getWorldWidth());
        }

        if (!AssetLoader.introMusic.isPlaying() && !AssetLoader.endMusic.isPlaying() && AssetLoader.levelMusic.getVolume() == 0) {
            AssetLoader.levelMusic.setVolume(BaseGame.musicVolume);
        }
    }

    private void updateCooldownLabel() {
        boolean onCooldown = flipperCooldownTimer > 0f;
        cooldownLabel.setVisible(onCooldown);

        if (!onCooldown) {
            return;
        }

        float alpha = 0.35f + 0.65f * Math.abs(MathUtils.sin(cooldownFlashTimer * 10f));
        cooldownLabel.setColor(1f, 0f, 0f, alpha);
        cooldownLabel.setText("FLIPPERS ON COOLDOWN, STOP SPAMMING!");
        cooldownLabel.pack();
        cooldownLabel.setPosition((uiStage.getWidth() - cooldownLabel.getWidth()) * 0.5f, (uiStage.getHeight() - cooldownLabel.getHeight()) * 0.5f);
    }

    private void updateBackgrounds() {
        OrthographicCamera cam = (OrthographicCamera) mainStage.getCamera();

        float worldWidth = viewport.getWorldWidth() * cam.zoom;
        float worldHeight = viewport.getWorldHeight() * cam.zoom;

        float scale = worldWidth / backgroundTexture.getWidth();
        float tileHeight = backgroundTexture.getHeight() * scale;

        float startY = cam.position.y - worldHeight * 0.5f;
        float baseY = (float) Math.floor(startY / tileHeight) * tileHeight;

        for (int i = 0; i < backgrounds.size; i++) {
            Image bg = backgrounds.get(i);
            bg.setSize(worldWidth, tileHeight);
            bg.setPosition(cam.position.x - worldWidth * 0.5f, baseY + i * tileHeight);
            bg.toBack();
        }
    }

    private void collectCoins() {
        Rectangle ballBounds = new Rectangle(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());

        for (int i = 0; i < chunks.size; i++) {
            Chunk chunk = chunks.get(i);
            Array<Coin> coins = chunk.getCoins();

            for (int j = coins.size - 1; j >= 0; j--) {
                Coin coin = coins.get(j);
                Rectangle coinBounds = new Rectangle(coin.getX(), coin.getY(), coin.getWidth(), coin.getHeight());

                if (ballBounds.overlaps(coinBounds)) {
                    chunk.removeCoin(coin);
                    coinCount++;
                    AssetLoader.coin_sound.play(BaseGame.soundVolume, MathUtils.random(0.9f, 1.1f), 0f);
                }
            }
        }
    }

    private void updateStatsLabel() {
        float metersTraveled = Math.max(0f, highestBallY - startBallY);
        statsLabel.setText("Meters: " + MathUtils.floor(metersTraveled) + "  Coins: " + coinCount);
    }

    private void unloadChunksBelowBall() {
        int activeIndex = getChunkIndexForBallY(ball.getY());
        if (activeIndex == -1) {
            return;
        }

        while (activeIndex >= CHUNKS_TO_KEEP_BELOW_BALL && chunks.size > CHUNKS_TO_KEEP_BELOW_BALL) {
            Chunk removed = chunks.removeIndex(0);
            removed.dispose();
            failureBottomY = chunks.first().getBottomY();
            activeIndex--;
        }
    }

    private void failBall() {
        float spawnX = chunkWidth * 0.5f - 32f;
        float spawnY = failureBottomY + chunkHeight * 0.5f;

        ball.setPosition(spawnX, spawnY);
        ball.getBody().setLinearVelocity(0f, 0f);
        ball.getBody().setAngularVelocity(0f);

        startBallY = spawnY;
        highestBallY = spawnY;

        launchBallDiagonally();
        updateStatsLabel();

        AssetLoader.introMusic.stop();
        AssetLoader.levelMusic.setVolume(0f);
        AssetLoader.endMusic.play();

        flipperCooldownTimer = 0f;
        bumperFlipCount = 0f;
        bumperFlipTime = 0f;
        cooldownFlashTimer = 0f;
        updateCooldownLabel();

        OrthographicCamera cam = (OrthographicCamera) mainStage.getCamera();
        cam.position.y = spawnY;
        cam.update();

        updateBackgrounds();

        for (int i = 0; i < chunks.size; i++) {
            chunks.get(i).extendWallsToCamera(cam, viewport.getWorldWidth());
        }
    }

    private void launchBallDiagonally() {
        float horizontal = MathUtils.randomBoolean() ? MathUtils.random(0.45f, 0.85f) : -MathUtils.random(0.45f, 0.85f);
        float vertical = MathUtils.random(BALL_MIN_UPWARD, 1f);

        Vector2 velocity = new Vector2(horizontal, vertical).nor().scl(BALL_LAUNCH_SPEED);
        ball.getBody().setLinearVelocity(velocity);
        ball.getBody().setAngularVelocity(MathUtils.random(-8f, 8f));
    }

    private Chunk getChunkForBallY(float ballY) {
        for (Chunk chunk : chunks) {
            if (chunk.containsY(ballY)) {
                return chunk;
            }
        }
        return null;
    }

    private int getChunkIndexForBallY(float ballY) {
        for (int i = 0; i < chunks.size; i++) {
            if (chunks.get(i).containsY(ballY)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        world.dispose();
    }
}
