package no.sandramoen.libgdx35.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import no.sandramoen.libgdx35.screens.gameplay.pinball.*;
import no.sandramoen.libgdx35.utils.BaseGame;
import no.sandramoen.libgdx35.utils.BaseScreen;

public class LevelScreen extends BaseScreen {

    private static final float TIME_STEP = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;

    private static final float BALL_RADIUS = 0.28f;
    private static final float BALL_SIZE = BALL_RADIUS * 2f;
    private static final float BALL_LAUNCH_SPEED = 10.5f;

    private static final float FLIPPER_WIDTH = 2.2f;
    private static final float FLIPPER_HEIGHT = 0.32f;
    private static final float LEFT_FLIPPER_X = 5.2f;
    private static final float RIGHT_FLIPPER_X = 10.8f;
    private static final float FLIPPER_Y = 2.0f;
    private static final float LEFT_FLIPPER_REST_ANGLE_DEGREES = -12f;
    private static final float RIGHT_FLIPPER_REST_ANGLE_DEGREES = 12f;
    private static final float FLIPPER_REST_SPEED = -10f;
    private static final float FLIPPER_ACTIVE_SPEED = 22f;
    private static final float FLIPPER_TORQUE = 5000f;

    private static final float PLATFORM_MIN_WIDTH = 1.8f;
    private static final float PLATFORM_MAX_WIDTH = 3.1f;
    private static final float PLATFORM_HEIGHT = 0.38f;
    private static final float PLATFORM_MOVE_SPEED = 4.5f;
    private static final float ROW_START_Y = 4.5f;
    private static final float ROW_MIN_GAP = 2.2f;
    private static final float ROW_MAX_GAP = 3.6f;
    private static final float ROW_SPAWN_BUFFER = 10f;
    private static final float ROW_DELETE_BUFFER = 8f;

    private static final String BACKGROUND_PATH = "images/background.png";
    private static final String BALL_PATH = "images/ball.png";
    private static final String FLIPPER_PATH = "images/flipper.png";
    private static final String PLATFORM_PATH = "images/platform.png";
    private static final String BOOST_ORB_PATH = "images/boost_orb.png";

    private World world;
    private Texture backgroundTexture;
    private Texture ballTexture;
    private Texture flipperTexture;
    private Texture platformTexture;
    private Texture boostOrbTexture;

    private BackgroundActor backgroundActor;
    private BallActor ballActor;
    private FlipperActor leftFlipperActor;
    private FlipperActor rightFlipperActor;

    private Array<PlatformActor> platformActors;
    private Array<BoostOrbActor> boostOrbActors;

    private float accumulator;
    private float nextRowY;
    private float highestHeight;

    @Override
    public void initialize() {
        platformActors = new Array<>();
        boostOrbActors = new Array<>();

        Box2D.init();
        world = new World(new Vector2(0f, -22f), true);
        world.setContactListener(new GameplayContactListener());

        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        ballTexture = new Texture(Gdx.files.internal(BALL_PATH));
        flipperTexture = new Texture(Gdx.files.internal(FLIPPER_PATH));
        platformTexture = new Texture(Gdx.files.internal(PLATFORM_PATH));
        boostOrbTexture = new Texture(Gdx.files.internal(BOOST_ORB_PATH));

        createInvisibleWalls();

        backgroundActor = new BackgroundActor(backgroundTexture);
        mainStage.addActor(backgroundActor);

        leftFlipperActor = new FlipperActor(world, flipperTexture, LEFT_FLIPPER_X, FLIPPER_Y, true, FLIPPER_WIDTH, FLIPPER_HEIGHT, LEFT_FLIPPER_REST_ANGLE_DEGREES, -25f, 30f, FLIPPER_REST_SPEED, FLIPPER_TORQUE);

        rightFlipperActor = new FlipperActor(world, flipperTexture, RIGHT_FLIPPER_X, FLIPPER_Y, false, FLIPPER_WIDTH, FLIPPER_HEIGHT, RIGHT_FLIPPER_REST_ANGLE_DEGREES, -30f, 25f, -FLIPPER_REST_SPEED, FLIPPER_TORQUE);

        mainStage.addActor(leftFlipperActor);
        mainStage.addActor(rightFlipperActor);

        nextRowY = ROW_START_Y;
        ensureRowsAboveCamera();
        spawnBall();
    }

    @Override
    public void update(float delta) {
        handleInput();

        accumulator += delta;
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= TIME_STEP;
        }

        syncActors();
        clampPlatforms();
        updateCamera();
        updateScore();
        ensureRowsAboveCamera();
        removeRowsBelowCamera();
        cleanupConsumedOrbs();

        if (ballActor != null && ballActor.getBody().getPosition().y < getCameraBottom() - 4f) {
            respawnBall();
        }
    }

    private void handleInput() {
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D);

        float platformVelocityX = 0f;
        if (moveLeft) {
            platformVelocityX -= PLATFORM_MOVE_SPEED;
        }
        if (moveRight) {
            platformVelocityX += PLATFORM_MOVE_SPEED;
        }

        for (PlatformActor platformActor : platformActors) {
            platformActor.setHorizontalVelocity(platformVelocityX);
        }

        leftFlipperActor.setPressed(Gdx.input.isKeyPressed(Input.Keys.LEFT), FLIPPER_ACTIVE_SPEED, FLIPPER_REST_SPEED);
        rightFlipperActor.setPressed(Gdx.input.isKeyPressed(Input.Keys.RIGHT), FLIPPER_ACTIVE_SPEED, FLIPPER_REST_SPEED);

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            respawnBall();
        }
    }

    private void syncActors() {
        leftFlipperActor.syncVisual();
        rightFlipperActor.syncVisual();

        for (PlatformActor platformActor : platformActors) {
            platformActor.syncVisual();
        }

        for (BoostOrbActor boostOrbActor : boostOrbActors) {
            boostOrbActor.syncVisual();
        }

        if (ballActor != null) {
            ballActor.syncVisual();
        }
    }

    private void clampPlatforms() {
        for (PlatformActor platformActor : platformActors) {
            float minX = 0.7f + platformActor.getPlatformWidth() * 0.5f;
            float maxX = BaseGame.WORLD_WIDTH - 0.7f - platformActor.getPlatformWidth() * 0.5f;
            platformActor.clampX(minX, maxX);
        }
    }

    private void updateCamera() {
        if (ballActor == null) {
            return;
        }

        OrthographicCamera camera = (OrthographicCamera) mainStage.getCamera();

        float halfViewportWidth = mainStage.getViewport().getWorldWidth() * 0.5f;
        float halfViewportHeight = mainStage.getViewport().getWorldHeight() * 0.5f;

        float minX = halfViewportWidth;
        float maxX = BaseGame.WORLD_WIDTH - halfViewportWidth;

        float targetX = ballActor.getBody().getPosition().x;
        float targetY = Math.max(halfViewportHeight, ballActor.getBody().getPosition().y);

        if (maxX < minX) {
            targetX = BaseGame.WORLD_WIDTH * 0.5f;
        } else {
            targetX = MathUtils.clamp(targetX, minX, maxX);
        }

        camera.position.set(targetX, targetY, 0f);
        camera.update();
    }

    private void updateScore() {
        if (ballActor == null) {
            return;
        }

        highestHeight = Math.max(highestHeight, ballActor.getBody().getPosition().y);
    }

    private void ensureRowsAboveCamera() {
        float targetTopY = getCameraTop() + ROW_SPAWN_BUFFER;

        while (nextRowY <= targetTopY) {
            spawnRow(nextRowY);
            nextRowY += MathUtils.random(ROW_MIN_GAP, ROW_MAX_GAP);
        }
    }

    private void spawnRow(float rowY) {
        int platformCount = MathUtils.random(1, 3);
        Array<float[]> placed = new Array<>();

        for (int i = 0; i < platformCount; i++) {
            int attempts = 0;
            boolean placedPlatform = false;

            while (!placedPlatform && attempts < 30) {
                attempts++;

                float width = MathUtils.random(PLATFORM_MIN_WIDTH, PLATFORM_MAX_WIDTH);
                float x = MathUtils.random(1.2f + width * 0.5f, BaseGame.WORLD_WIDTH - 1.2f - width * 0.5f);

                if (overlapsRowPlacement(placed, x, width)) {
                    continue;
                }

                PlatformActor platformActor = new PlatformActor(world, platformTexture, x, rowY, width, PLATFORM_HEIGHT);
                platformActors.add(platformActor);
                mainStage.addActor(platformActor);
                placed.add(new float[]{x, width});
                placedPlatform = true;
            }
        }

        float specialChance = Math.min(0.08f + rowY * 0.004f, 0.35f);
        if (MathUtils.random() < specialChance) {
            float x = MathUtils.random(1.4f, BaseGame.WORLD_WIDTH - 1.4f);
            float y = rowY + MathUtils.random(0.7f, 1.5f);

            BoostOrbActor boostOrbActor = new BoostOrbActor(world, boostOrbTexture, x, y, 0.38f, 13f + rowY * 0.02f);
            boostOrbActors.add(boostOrbActor);
            mainStage.addActor(boostOrbActor);
        }
    }

    private boolean overlapsRowPlacement(Array<float[]> placed, float x, float width) {
        for (float[] item : placed) {
            float otherX = item[0];
            float otherWidth = item[1];
            float minDistance = width * 0.5f + otherWidth * 0.5f + 0.9f;

            if (Math.abs(otherX - x) < minDistance) {
                return true;
            }
        }

        return false;
    }

    private void removeRowsBelowCamera() {
        float deleteBelowY = getCameraBottom() - ROW_DELETE_BUFFER;

        for (int i = platformActors.size - 1; i >= 0; i--) {
            PlatformActor platformActor = platformActors.get(i);
            if (platformActor.getBody().getPosition().y < deleteBelowY) {
                platformActor.destroy(world);
                platformActor.remove();
                platformActors.removeIndex(i);
            }
        }

        for (int i = boostOrbActors.size - 1; i >= 0; i--) {
            BoostOrbActor boostOrbActor = boostOrbActors.get(i);
            if (boostOrbActor.getBody().getPosition().y < deleteBelowY) {
                boostOrbActor.destroy(world);
                boostOrbActor.remove();
                boostOrbActors.removeIndex(i);
            }
        }
    }

    private void cleanupConsumedOrbs() {
        for (int i = boostOrbActors.size - 1; i >= 0; i--) {
            BoostOrbActor boostOrbActor = boostOrbActors.get(i);
            if (boostOrbActor.isConsumed()) {
                boostOrbActor.destroy(world);
                boostOrbActor.remove();
                boostOrbActors.removeIndex(i);
            }
        }
    }

    private float getCameraTop() {
        OrthographicCamera camera = (OrthographicCamera) mainStage.getCamera();
        return camera.position.y + mainStage.getViewport().getWorldHeight() * 0.5f;
    }

    private float getCameraBottom() {
        OrthographicCamera camera = (OrthographicCamera) mainStage.getCamera();
        return camera.position.y - mainStage.getViewport().getWorldHeight() * 0.5f;
    }

    private void createInvisibleWalls() {
        PhysicsFactory.createVerticalWall(world, 0.7f);
        PhysicsFactory.createVerticalWall(world, BaseGame.WORLD_WIDTH - 0.7f);
    }

    private void spawnBall() {
        ballActor = new BallActor(world, ballTexture, randomSpawnX(), Math.max(getCameraTop() + 3f, 14.2f), BALL_RADIUS, BALL_SIZE);
        mainStage.addActor(ballActor);
        launchBallUpward();
        syncActors();
        updateCamera();
    }

    private void respawnBall() {
        if (ballActor != null) {
            ballActor.destroy(world);
            ballActor.remove();
        }

        spawnBall();
    }

    private float randomSpawnX() {
        return MathUtils.random(2.0f, 14.0f);
    }

    private void launchBallUpward() {
        float targetX = MathUtils.random(4.0f, 12.0f);
        float targetY = ballActor.getBody().getPosition().y + MathUtils.random(6f, 10f);

        Vector2 direction = new Vector2(targetX - ballActor.getBody().getPosition().x, targetY - ballActor.getBody().getPosition().y);

        direction.x += MathUtils.random(-0.7f, 0.7f);
        direction.y += MathUtils.random(0.1f, 0.8f);
        direction.nor().scl(BALL_LAUNCH_SPEED);

        ballActor.getBody().setLinearVelocity(direction);
        ballActor.getBody().setAngularVelocity(MathUtils.random(-8f, 8f));
    }

    @Override
    public void dispose() {
        super.dispose();

        if (ballActor != null) {
            ballActor.destroy(world);
        }

        if (leftFlipperActor != null) {
            leftFlipperActor.destroy(world);
        }

        if (rightFlipperActor != null) {
            rightFlipperActor.destroy(world);
        }

        for (PlatformActor platformActor : platformActors) {
            platformActor.destroy(world);
        }

        for (BoostOrbActor boostOrbActor : boostOrbActors) {
            boostOrbActor.destroy(world);
        }

        backgroundTexture.dispose();
        ballTexture.dispose();
        flipperTexture.dispose();
        platformTexture.dispose();
        boostOrbTexture.dispose();
        world.dispose();
    }

    private final class GameplayContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            Object a = contact.getFixtureA().getBody().getUserData();
            Object b = contact.getFixtureB().getBody().getUserData();

            if (a instanceof BoostOrbActor && b instanceof BallActor) {
                ((BoostOrbActor) a).consume((BallActor) b);
            } else if (a instanceof BallActor && b instanceof BoostOrbActor) {
                ((BoostOrbActor) b).consume((BallActor) a);
            }
        }

        @Override
        public void endContact(Contact contact) {
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }
}
