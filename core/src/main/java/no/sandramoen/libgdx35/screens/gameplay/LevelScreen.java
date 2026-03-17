package no.sandramoen.libgdx35.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import no.sandramoen.libgdx35.actors.pinball.*;
import no.sandramoen.libgdx35.utils.BaseScreen;

public class LevelScreen extends BaseScreen {

    private static final int CHUNKS_TO_KEEP_BELOW_BALL = 4;
    private static final float BALL_LAUNCH_SPEED = 20f;
    private static final float BALL_MIN_UPWARD = 5.75f;

    private Label label;
    private Ball ball;
    private World world;
    private Array<Chunk> chunks;

    private float chunkWidth;
    private float chunkHeight;
    private float wallThickness;
    private float generatedTopY;
    private float failureBottomY;
    private float startBallY;
    private float highestBallY;

    @Override
    public void initialize() {
        chunks = new Array<>();
        world = new World(new Vector2(0f, -9.8f), true);
        world.setContactListener(new PlatformContactListener());

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont();
        labelStyle.font.getData().scale(2f);
        labelStyle.fontColor = Color.WHITE;

        label = new Label("Meters Traveled: 0", labelStyle);
        label.setPosition(10f, Gdx.graphics.getHeight() - label.getPrefHeight() - 10f);
        uiStage.addActor(label);

        chunkWidth = Gdx.graphics.getWidth();
        chunkHeight = 1000;
        wallThickness = 96;

        generateChunk(0f, true);
        generateChunk(chunkHeight, false);
        generateChunk(chunkHeight * 2f, false);

        failureBottomY = chunks.first().getBottomY();

        ball = new Ball(world, 500f, 500f, 64f, 64f, Material.GUM, mainStage);
        startBallY = ball.getY();
        highestBallY = startBallY;

        launchBallDiagonally();

        mainStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }

    private void generateChunk(float y, boolean withBottomWall) {
        Chunk chunk = new Chunk(world, mainStage, 0f, y, chunkWidth, chunkHeight, wallThickness, randomMaterial(), withBottomWall);
        chunks.add(chunk);
        generatedTopY = Math.max(generatedTopY, chunk.getTopY());
    }

    private Material randomMaterial() {
        Material[] values = Material.values();
        return values[MathUtils.random(values.length - 1)];
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
            generateChunk(generatedTopY, false);
        }

        unloadChunksBelowBall();

        world.step(1f / 120f, 12, 6);

        highestBallY = Math.max(highestBallY, ball.getY());
        float metersTraveled = Math.max(0f, highestBallY - startBallY);
        label.setText("Meters Traveled: " + MathUtils.floor(metersTraveled));

        if (ball.getY() + ball.getHeight() < failureBottomY) {
            failBall();
            return;
        }

        OrthographicCamera cam = (OrthographicCamera) mainStage.getCamera();
        cam.position.y += (ball.getY() - cam.position.y) * 0.08f;
        cam.update();
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
        label.setText("Meters Traveled: 0");

        launchBallDiagonally();

        OrthographicCamera cam = (OrthographicCamera) mainStage.getCamera();
        cam.position.y = spawnY;
        cam.update();
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
