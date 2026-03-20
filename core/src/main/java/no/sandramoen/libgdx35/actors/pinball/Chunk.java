package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import no.sandramoen.libgdx35.utils.BaseActor;

public class Chunk {

    private static final float CLIFF_WALL_INSET = 17f;
    private static final float WALL_CHUNK_OVERLAP = 24f;
    private static final float WALL_VISIBLE_WIDTH = 5f;
    private static final float START_Y_OFFSET = 180f;
    private static final float INITIAL_BUMPER_WIDTH = 304f;
    private static final float INITIAL_BUMPER_HEIGHT = 96f;
    private static final float INITIAL_CLIFF_WIDTH = 128f;
    private static final float INITIAL_CLIFF_HEIGHT = 304f;

    private static final float COIN_WIDTH = 48f;
    private static final float COIN_HEIGHT = 48f;
    private static final float COIN_PADDING = 18f;
    private static final int MIN_COINS = 5;
    private static final int MAX_COINS = 15;
    private static final int MAX_COIN_ATTEMPTS = 200;

    private final World world;
    private final Stage stage;

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float wallThickness = 12;

    private final float leftInnerEdgeX;
    private final float rightInnerEdgeX;

    private final Wall leftWall;
    private final Wall rightWall;

    private final Array<Cliff> cliffs = new Array<>();
    private final Array<PlatformBumper> bumpers = new Array<>();
    private final Array<Coin> coins = new Array<>();

    private PlacementState state = PlacementState.values()[MathUtils.random(2)];

    private boolean disposed;

    public Chunk(World world, Stage stage, float x, float y, float width, float height) {
        this.world = world;
        this.stage = stage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        leftInnerEdgeX = x + WALL_VISIBLE_WIDTH;
        rightInnerEdgeX = x + width - WALL_VISIBLE_WIDTH;

        leftWall = new Wall(world, x - wallThickness + WALL_VISIBLE_WIDTH, y - WALL_CHUNK_OVERLAP, wallThickness, height + WALL_CHUNK_OVERLAP * 2f, Material.getRandomMaterial(), stage);
        rightWall = new Wall(world, x + width - WALL_VISIBLE_WIDTH, y - WALL_CHUNK_OVERLAP, wallThickness, height + WALL_CHUNK_OVERLAP * 2f, Material.getRandomMaterial(), stage);
        rightWall.setOrigin(rightWall.getWidth() * 0.5f, rightWall.getHeight() * 0.5f);
        rightWall.setScaleX(-1f);

        generateBumpersAndCliffs();
        generateCoins();
    }

    public void extendWallsToCamera(OrthographicCamera camera, float viewportWorldWidth) {
        float halfViewWidth = viewportWorldWidth * camera.zoom * 0.5f;
        float cameraLeft = camera.position.x - halfViewWidth;
        float cameraRight = camera.position.x + halfViewWidth;

        float leftX = Math.min(cameraLeft, leftInnerEdgeX - wallThickness);
        float leftWidth = leftInnerEdgeX - leftX;

        float rightWidth = Math.max(wallThickness, cameraRight - rightInnerEdgeX);
        float rightX = rightInnerEdgeX;

        leftWall.setBounds(leftX, y - WALL_CHUNK_OVERLAP, leftWidth, height + WALL_CHUNK_OVERLAP * 2f);
        rightWall.setBounds(rightX, y - WALL_CHUNK_OVERLAP, rightWidth, height + WALL_CHUNK_OVERLAP * 2f);
        rightWall.setOrigin(rightWall.getWidth() * 0.5f, rightWall.getHeight() * 0.5f);
        rightWall.setScaleX(-1f);

        float scaledInset = CLIFF_WALL_INSET * camera.zoom;
        float extraOffset = 30f * camera.zoom;

        for (int i = 0; i < cliffs.size; i++) {
            Cliff cliff = cliffs.get(i);

            if (cliff.getOrientation() == Orientation.RIGHT) {
                cliff.setX(getLeftInnerWallX() - scaledInset - extraOffset);
            } else {
                cliff.setX(getRightInnerWallX() - cliff.getWidth() + scaledInset + extraOffset);
            }
        }

    }

    private void generateBumpersAndCliffs() {
        int rows = MathUtils.random(8, 16);
        float currentY = y + START_Y_OFFSET;

        for (int i = 0; i < rows; i++) {
            float cliffWidth = INITIAL_CLIFF_WIDTH * MathUtils.random(1.0f, 1.5f);
            float cliffHeight = INITIAL_CLIFF_HEIGHT * MathUtils.random(1.0f, 1.5f);
            float bumperWidth = INITIAL_BUMPER_WIDTH * MathUtils.random(0.9f, 1.1f);
            float bumperHeight = INITIAL_BUMPER_HEIGHT * MathUtils.random(0.9f, 1.1f);
            float leftPadding = getLeftInnerWallX() + INITIAL_CLIFF_WIDTH;
            float rightPadding = getRightInnerWallX() - INITIAL_CLIFF_WIDTH - INITIAL_BUMPER_WIDTH;
            float degreeOffset = MathUtils.random(-10f, 10f);
            float minReduction = -(INITIAL_BUMPER_WIDTH * 0.25f);
            float maxIncrease = INITIAL_BUMPER_WIDTH * 0.5f;

            switch (state) {
                case LEFT_BUMPER:
                    float bumperX = leftPadding + MathUtils.random(minReduction, maxIncrease);
                    bumpers.add(new PlatformBumper(world, bumperX, currentY, bumperWidth, bumperHeight, true, Material.getRandomMaterial(), stage));
                    currentY += bumperHeight * 2f;
                    break;

                case RIGHT_BUMPER:
                    bumperX = rightPadding - MathUtils.random(minReduction, maxIncrease);
                    bumpers.add(new PlatformBumper(world, bumperX, currentY, bumperWidth, bumperHeight, false, Material.getRandomMaterial(), stage));
                    currentY += bumperHeight * 2f;
                    break;

                case DOUBLE_BUMPER:
                    bumperX = leftPadding + MathUtils.random(minReduction, maxIncrease);
                    bumpers.add(new PlatformBumper(world, bumperX, currentY, bumperWidth, bumperHeight, true, Material.getRandomMaterial(), stage));

                    bumperX = rightPadding - MathUtils.random(minReduction, maxIncrease);
                    bumpers.add(new PlatformBumper(world, bumperX, currentY, bumperWidth, bumperHeight, false, Material.getRandomMaterial(), stage));
                    currentY += bumperHeight * 2f;
                    state = MathUtils.randomBoolean() ? PlacementState.LEFT_CLIFF : PlacementState.RIGHT_CLIFF;
                    continue;

                case LEFT_CLIFF:
                    Cliff cliff = new Cliff(world, getLeftInnerWallX() - CLIFF_WALL_INSET - 30f, currentY, cliffWidth, cliffHeight, Orientation.RIGHT, Material.getRandomMaterial(), stage);
                    cliff.setRotation(cliff.getRotation() + degreeOffset);
                    cliffs.add(cliff);
                    currentY += cliffHeight * 2f;
                    break;

                case RIGHT_CLIFF:
                    cliff = new Cliff(world, getRightInnerWallX() - cliffWidth + CLIFF_WALL_INSET + 30f, currentY, cliffWidth, cliffHeight, Orientation.LEFT, Material.getRandomMaterial(), stage);
                    cliff.setRotation(cliff.getRotation() + degreeOffset);
                    cliffs.add(cliff);
                    currentY += cliffHeight * 2f;
                    break;
            }

            if (currentY >= getTopY() - 100f) {
                break;
            }

            PlacementRule rule = PlacementRule.getRule(state);
            if (rule == null || rule.getAllowed() == null || rule.getAllowed().length == 0) {
                break;
            }

            PlacementState[] allowed = rule.getAllowed();
            state = allowed[MathUtils.random(allowed.length - 1)];
        }
    }

    private void generateCoins() {
        float minX = getLeftInnerWallX() + COIN_PADDING;
        float maxX = getRightInnerWallX() - COIN_WIDTH - COIN_PADDING;
        float minY = getBottomY() + COIN_PADDING;
        float maxY = getTopY() - COIN_HEIGHT - COIN_PADDING;

        if (maxX <= minX || maxY <= minY) {
            return;
        }

        int targetCoins = MathUtils.random(MIN_COINS, MAX_COINS);
        int attempts = 0;

        while (coins.size < targetCoins && attempts < MAX_COIN_ATTEMPTS) {
            attempts++;

            float coinX = MathUtils.random(minX, maxX);
            float coinY = MathUtils.random(minY, maxY);

            if (!canPlaceCoin(coinX, coinY)) {
                continue;
            }

            coins.add(new Coin(coinX, coinY, COIN_WIDTH, COIN_HEIGHT, stage));
        }
    }

    private boolean canPlaceCoin(float x, float y) {
        if (overlapsRotated(leftWall, x, y, COIN_WIDTH, COIN_HEIGHT, COIN_PADDING)) return false;
        if (overlapsRotated(rightWall, x, y, COIN_WIDTH, COIN_HEIGHT, COIN_PADDING)) return false;

        for (int i = 0; i < bumpers.size; i++) {
            if (overlapsRotated(bumpers.get(i), x, y, COIN_WIDTH, COIN_HEIGHT, COIN_PADDING + 14f)) return false;
        }

        for (int i = 0; i < cliffs.size; i++) {
            if (overlapsRotated(cliffs.get(i), x, y, COIN_WIDTH, COIN_HEIGHT, COIN_PADDING + 10f)) return false;
        }

        for (int i = 0; i < coins.size; i++) {
            if (overlapsRotated(coins.get(i), x, y, COIN_WIDTH, COIN_HEIGHT, COIN_PADDING)) return false;
        }

        return true;
    }

    private boolean overlapsRotated(BaseActor actor, float x, float y, float width, float height, float padding) {
        if (actor == null) return false;

        float actorCenterX = actor.getX() + actor.getWidth() * 0.5f;
        float actorCenterY = actor.getY() + actor.getHeight() * 0.5f;

        float coinCenterX = x + width * 0.5f;
        float coinCenterY = y + height * 0.5f;

        float radians = -actor.getRotation() * MathUtils.degreesToRadians;
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        float dx = coinCenterX - actorCenterX;
        float dy = coinCenterY - actorCenterY;

        float localX = dx * cos - dy * sin;
        float localY = dx * sin + dy * cos;

        float halfW = actor.getWidth() * 0.5f + padding;
        float halfH = actor.getHeight() * 0.5f + padding;

        float closestX = MathUtils.clamp(localX, -halfW, halfW);
        float closestY = MathUtils.clamp(localY, -halfH, halfH);

        float diffX = localX - closestX;
        float diffY = localY - closestY;

        float coinRadius = Math.max(width, height) * 0.5f;

        return diffX * diffX + diffY * diffY < coinRadius * coinRadius;
    }

    private float getLeftInnerWallX() {
        return leftInnerEdgeX;
    }

    private float getRightInnerWallX() {
        return rightInnerEdgeX;
    }

    public void removeCoin(Coin coin) {
        if (coin == null) return;
        coins.removeValue(coin, true);
        coin.remove();
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;

        disposeActor(leftWall);
        disposeActor(rightWall);

        for (int i = 0; i < cliffs.size; i++) {
            disposeActor(cliffs.get(i));
        }

        for (int i = 0; i < bumpers.size; i++) {
            disposeActor(bumpers.get(i));
        }

        for (int i = 0; i < coins.size; i++) {
            disposeActor(coins.get(i));
        }

        cliffs.clear();
        bumpers.clear();
        coins.clear();
    }

    private void disposeActor(BaseActor actor) {
        if (actor == null) return;

        if (actor instanceof PhysicsActor) {
            PhysicsActor physicsActor = (PhysicsActor) actor;
            if (physicsActor.getBody() != null) {
                world.destroyBody(physicsActor.getBody());
            }
        }

        actor.remove();
    }

    public float getBottomY() {
        return y;
    }

    public float getTopY() {
        return y + height;
    }

    public boolean containsY(float worldY) {
        return worldY >= y && worldY < y + height;
    }

    public Array<PlatformBumper> getBumpers() {
        return bumpers;
    }

    public Array<Coin> getCoins() {
        return coins;
    }

    public boolean isDisposed() {
        return disposed;
    }
}
