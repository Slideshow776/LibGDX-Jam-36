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
import no.sandramoen.libgdx35.utils.BaseScreen;

public class LevelScreen extends BaseScreen {

    private static final float TIME_STEP = 1f / 120f;
    private static final int VELOCITY_ITERATIONS = 12;
    private static final int POSITION_ITERATIONS = 6;

    private static final float BALL_RADIUS = 0.28f;
    private static final float BALL_SIZE = BALL_RADIUS * 2f;
    private static final float BALL_LAUNCH_SPEED = 2.5f;

    private static final float WALL_INSET = 0.25f;
    private static final float WALL_DRAW_WIDTH = 0.5f;
    private static final float WALL_DRAW_HEIGHT = 10000f;

    private static final float FLIPPER_WIDTH_RATIO = 0.4f;
    private static final float FLIPPER_HEIGHT = 0.32f;
    private static final float FLIPPER_Y = 3.0f;
    private static final float LEFT_FLIPPER_REST_ANGLE_DEGREES = -12f;
    private static final float RIGHT_FLIPPER_REST_ANGLE_DEGREES = 12f;
    private static final float FLIPPER_REST_SPEED = -10f;
    private static final float FLIPPER_ACTIVE_SPEED = 30f;
    private static final float FLIPPER_TORQUE = 9000f;

    private static final float PLATFORM_MIN_WIDTH = 1.8f;
    private static final float PLATFORM_MAX_WIDTH = 3.1f;
    private static final float PLATFORM_HEIGHT = 0.38f;
    private static final float PLATFORM_MOVE_SPEED = 4.5f;
    private static final float ROW_START_Y = 6.5f;
    private static final float ROW_MIN_GAP = 2.2f;
    private static final float ROW_MAX_GAP = 3.6f;
    private static final float ROW_SPAWN_BUFFER = 10f;

    private static final String BACKGROUND_PATH = "images/background.png";
    private static final String BALL_PATH = "images/ball.png";
    private static final String FLIPPER_PATH = "images/flipper.png";
    private static final String BOOST_ORB_PATH = "images/boost_orb.png";

    private World world;
    private Texture backgroundTexture;
    private Texture ballTexture;
    private Texture flipperTexture;
    private Texture boostOrbTexture;

    private BackgroundActor backgroundActor;
    private WallActor leftWallActor;
    private WallActor rightWallActor;
    private BallActor ballActor;
    private FlipperActor leftFlipperActor;
    private FlipperActor rightFlipperActor;

    private Array<PlatformActor> platformActors;
    private Array<BoostOrbActor> boostOrbActors;

    private float accumulator;
    private float nextRowY;
    private float highestHeight;
    private float lastFlipperScreenBottom = Float.NaN;
    private float worldBottomY;

    private float leftBoundaryX;
    private float rightBoundaryX;
    private float playableWidth;
    private float flipperWidth;

    @Override
    public void initialize() {
        platformActors = new Array<>();
        boostOrbActors = new Array<>();

        mainStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        playableWidth = mainStage.getViewport().getWorldWidth();
        leftBoundaryX = WALL_INSET;
        rightBoundaryX = playableWidth - WALL_INSET;
        flipperWidth = playableWidth * FLIPPER_WIDTH_RATIO;

        Box2D.init();
        world = new World(new Vector2(0f, -22f), true);
        world.setContactListener(new GameplayContactListener());

        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        ballTexture = new Texture(Gdx.files.internal(BALL_PATH));
        flipperTexture = new Texture(Gdx.files.internal(FLIPPER_PATH));
        boostOrbTexture = new Texture(Gdx.files.internal(BOOST_ORB_PATH));

        backgroundActor = new BackgroundActor(backgroundTexture);
        mainStage.addActor(backgroundActor);

        createWalls();

        leftFlipperActor = new FlipperActor(
            world,
            flipperTexture,
            leftBoundaryX,
            FLIPPER_Y,
            true,
            flipperWidth,
            FLIPPER_HEIGHT,
            LEFT_FLIPPER_REST_ANGLE_DEGREES,
            -25f,
            30f,
            FLIPPER_REST_SPEED,
            FLIPPER_TORQUE
        );

        rightFlipperActor = new FlipperActor(
            world,
            flipperTexture,
            rightBoundaryX,
            FLIPPER_Y,
            false,
            flipperWidth,
            FLIPPER_HEIGHT,
            RIGHT_FLIPPER_REST_ANGLE_DEGREES,
            -30f,
            25f,
            -FLIPPER_REST_SPEED,
            FLIPPER_TORQUE
        );

        mainStage.addActor(leftFlipperActor);
        mainStage.addActor(rightFlipperActor);

        nextRowY = ROW_START_Y;
        ensureRowsAboveCamera();
        spawnBall();

        worldBottomY = getCameraBottom();
        clampFlippersToScreenBottom();
        syncActors();
    }

    @Override
    public void update(float delta) {
        handleInput();

        accumulator += delta;
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= TIME_STEP;
        }

        clampPlatforms();
        updateCamera();
        updateWorldBottom();
        clampFlippersToScreenBottom();
        syncActors();
        updateScore();
        ensureRowsAboveCamera();
        removeRowsBelowWorldBottom();
        cleanupConsumedOrbs();

        float respawnY = worldBottomY;
        if (ballActor != null && ballActor.getBody().getPosition().y < respawnY) {
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
            float minX = leftBoundaryX + platformActor.getPlatformWidth() * 0.5f;
            float maxX = rightBoundaryX - platformActor.getPlatformWidth() * 0.5f;
            platformActor.clampX(minX, maxX);
        }
    }

    private PlatformMaterial randomPlatformMaterial() {
        float roll = MathUtils.random();

        if (roll < 0.60f) {
            return PlatformMaterial.WOOD;
        }
        if (roll < 0.80f) {
            return PlatformMaterial.GUM;
        }
        if (roll < 0.95f) {
            return PlatformMaterial.METAL;
        }
        return PlatformMaterial.GLASS;
    }

    private void updateCamera() {
        if (ballActor == null) {
            return;
        }

        OrthographicCamera camera = (OrthographicCamera) mainStage.getCamera();

        float halfViewportWidth = mainStage.getViewport().getWorldWidth() * 0.5f;
        float halfViewportHeight = mainStage.getViewport().getWorldHeight() * 0.5f;
        float targetY = Math.max(halfViewportHeight, ballActor.getBody().getPosition().y);

        camera.position.set(halfViewportWidth, targetY, 0f);
        camera.update();
    }

    private void updateWorldBottom() {
        if (ballActor == null) {
            return;
        }

        float targetBottom = ballActor.getBody().getPosition().y - mainStage.getViewport().getWorldHeight() * 0.5f;
        worldBottomY = Math.max(worldBottomY, targetBottom);
    }

    private void clampFlippersToScreenBottom() {
        float targetY = worldBottomY + FLIPPER_Y;

        if (!Float.isNaN(lastFlipperScreenBottom) && Math.abs(targetY - lastFlipperScreenBottom) < 0.001f) {
            return;
        }

        leftFlipperActor.setAnchorY(targetY);
        rightFlipperActor.setAnchorY(targetY);
        lastFlipperScreenBottom = targetY;
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
        int platformCount = MathUtils.randomBoolean(0.75f) ? 1 : 2;
        Array<float[]> placed = new Array<>();

        for (int i = 0; i < platformCount; i++) {
            int attempts = 0;
            boolean placedPlatform = false;

            while (!placedPlatform && attempts < 30) {
                attempts++;

                float width = MathUtils.random(PLATFORM_MIN_WIDTH, PLATFORM_MAX_WIDTH);
                float x = MathUtils.random(
                    leftBoundaryX + 0.5f + width * 0.5f,
                    rightBoundaryX - 0.5f - width * 0.5f
                );

                if (overlapsRowPlacement(placed, x, width)) {
                    continue;
                }

                PlatformActor platformActor = new PlatformActor(world, x, rowY, width, PLATFORM_HEIGHT, randomPlatformMaterial());
                platformActors.add(platformActor);
                mainStage.addActor(platformActor);
                placed.add(new float[]{x, width});
                placedPlatform = true;
            }
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

    private void removeRowsBelowWorldBottom() {
        float deleteBelowY = worldBottomY;

        for (int i = platformActors.size - 1; i >= 0; i--) {
            PlatformActor platformActor = platformActors.get(i);
            if (platformActor.isBroken() || platformActor.getBody().getPosition().y < deleteBelowY) {
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

    private void createWalls() {
        leftWallActor = new WallActor(world, PlatformMaterial.METAL, leftBoundaryX, WALL_DRAW_WIDTH, WALL_DRAW_HEIGHT);
        rightWallActor = new WallActor(world, PlatformMaterial.METAL, rightBoundaryX, WALL_DRAW_WIDTH, WALL_DRAW_HEIGHT);

        mainStage.addActor(leftWallActor);
        mainStage.addActor(rightWallActor);
    }

    private void spawnBall() {
        ballActor = new BallActor(
            world,
            ballTexture,
            randomSpawnX(),
            Math.max(getCameraTop() + 3f, 14.2f),
            BALL_RADIUS,
            BALL_SIZE
        );

        mainStage.addActor(ballActor);
        launchBallUpward();

        lastFlipperScreenBottom = Float.NaN;
        clampFlippersToScreenBottom();
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
        return MathUtils.random(leftBoundaryX + 1.3f, rightBoundaryX - 1.3f);
    }

    private void launchBallUpward() {
        float targetX = MathUtils.random(leftBoundaryX + flipperWidth, rightBoundaryX - flipperWidth);
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

        if (leftWallActor != null) {
            leftWallActor.destroy(world);
        }

        if (rightWallActor != null) {
            rightWallActor.destroy(world);
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

            BallActor ball = null;
            PlatformActor platform = null;

            if (a instanceof BallActor && b instanceof PlatformActor) {
                ball = (BallActor) a;
                platform = (PlatformActor) b;
            } else if (a instanceof PlatformActor && b instanceof BallActor) {
                ball = (BallActor) b;
                platform = (PlatformActor) a;
            }

            if (ball != null && platform != null) {
                platform.getMaterial().onBeginContact(ball, platform);
            }
        }

        @Override
        public void endContact(Contact contact) {
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();

            Object a = fixtureA.getBody().getUserData();
            Object b = fixtureB.getBody().getUserData();

            if ((a instanceof FlipperActor && b instanceof PlatformActor) || (a instanceof PlatformActor && b instanceof FlipperActor)) {
                contact.setEnabled(false);
                return;
            }

            BallActor ball = null;
            PlatformActor platform = null;

            if (a instanceof BallActor && b instanceof PlatformActor) {
                ball = (BallActor) a;
                platform = (PlatformActor) b;
            } else if (a instanceof PlatformActor && b instanceof BallActor) {
                ball = (BallActor) b;
                platform = (PlatformActor) a;
            }

            if (ball == null || platform == null) {
                return;
            }

            contact.setEnabled(!platform.getMaterial().shouldDisableContact(ball, platform, BALL_RADIUS));
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }
}
