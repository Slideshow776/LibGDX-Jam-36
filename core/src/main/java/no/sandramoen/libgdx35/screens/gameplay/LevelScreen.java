package no.sandramoen.libgdx35.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import no.sandramoen.libgdx35.actors.pinball.*;
import no.sandramoen.libgdx35.utils.BaseScreen;

public class LevelScreen extends BaseScreen {

    private static final int CHUNKS_TO_KEEP_BELOW_BALL = 4;
    private static final float BALL_LAUNCH_SPEED = 30f;
    private static final float BALL_MIN_UPWARD = 10.75f;

    private Ball ball;
    private World world;
    private Array<Chunk> chunks;

    private float chunkWidth;
    private float chunkHeight;
    private float generatedTopY;
    private float failureBottomY;
    private float startBallY;
    private float highestBallY;

    private Label statsLabel;
    private int coinCount;

    @Override
    public void initialize() {
        chunks = new Array<>();
        world = new World(new Vector2(0f, -24f), true);
        world.setContactListener(new PlatformContactListener());

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont();
        labelStyle.font.getData().scale(2f);
        labelStyle.fontColor = Color.WHITE;

        statsLabel = new Label("", labelStyle);
        statsLabel.setPosition(20f, Gdx.graphics.getHeight() - 70f);
        uiStage.addActor(statsLabel);

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

        mainStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            mainStage.screenToStageCoordinates(mouse);

            ball.setPosition(mouse.x, mouse.y);
            ball.getBody().setLinearVelocity(0f, 0f);
            ball.getBody().setAngularVelocity(0f);

            highestBallY = Math.max(highestBallY, ball.getY());
        }

        Chunk activeChunk = getChunkForBallY(ball.getY());
        if (activeChunk != null) {
            for (PlatformBumper bumper : activeChunk.getBumpers()) {
                if (bumper.isLeft()) {
                    bumper.setRaised(Gdx.input.isKeyPressed(Input.Keys.LEFT));
                } else {
                    bumper.setRaised(Gdx.input.isKeyPressed(Input.Keys.RIGHT));
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

        for (int i = 0; i < chunks.size; i++) {
            chunks.get(i).extendWallsToCamera(cam, viewport.getWorldWidth());
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

        OrthographicCamera cam = (OrthographicCamera) mainStage.getCamera();
        cam.position.y = spawnY;
        cam.update();

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
        world.dispose();
    }
}
