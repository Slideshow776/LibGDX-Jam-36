package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class Chunk {

    private static final float MINIMUM_BUMPER_WALL_SPACING = 72f;
    private static final float MINIMUM_BUMPER_CENTER_GAP = 0f;
    private static final float MINIMUM_BUMPER_CLIFF_VERTICAL_GAP = 256f;
    private static final float MINIMUM_BUMPER_CLIFF_HORIZONTAL_GAP = 96f;
    private static final float BUMPER_RANDOM_SCALE_MIN = 0.9f;
    private static final float BUMPER_RANDOM_SCALE_MAX = 1.1f;
    private static final float CLIFF_WALL_INSET = 0f;
    private static final float WALL_CHUNK_OVERLAP = 36;

    private final World world;
    private final Stage stage;
    private final Material material;

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float wallThickness;

    private final Wall leftWall;
    private final Wall rightWall;
    private final Wall bottomWall;

    private final Array<Cliff> cliffs = new Array<>();
    private final Array<PlatformBumper> bumpers = new Array<>();

    private boolean disposed;

    public Chunk(World world, Stage stage, float x, float y, float width, float height, float wallThickness, Material material, boolean withBottomWall) {
        this.world = world;
        this.stage = stage;
        this.material = material == null ? Material.GUM : material;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.wallThickness = wallThickness;

        leftWall = new Wall(world, x - 15, y - WALL_CHUNK_OVERLAP, wallThickness, height + WALL_CHUNK_OVERLAP * 2f, this.material, stage);
        rightWall = new Wall(world, x + width - wallThickness + 15, y - WALL_CHUNK_OVERLAP, wallThickness, height + WALL_CHUNK_OVERLAP * 2f, this.material, stage);
        rightWall.setOrigin(rightWall.getWidth() * 0.5f, rightWall.getHeight() * 0.5f);
        rightWall.setScaleX(-1f);

        bottomWall = withBottomWall ? new Wall(world, x - WALL_CHUNK_OVERLAP, y, width + WALL_CHUNK_OVERLAP * 2f, wallThickness, this.material, stage) : null;

        generateBumpersAndCliffs();
    }

    private void generateBumpersAndCliffs() {
        float baseBumperWidth = 280f;
        float baseBumperHeight = 64f;
        float cliffWidth = 128f;
        float cliffHeight = 256f;
        float cliffOffsetY = 256f;

        float centerX = x + width * 0.5f;
        float leftMinX = x + wallThickness + MINIMUM_BUMPER_WALL_SPACING;
        float rightMaxXBase = x + width - wallThickness - MINIMUM_BUMPER_WALL_SPACING;

        float startY = y + wallThickness + 180f;
        float endY = y + height - cliffOffsetY - cliffHeight - 72f;
        float rowSpacing = 260f;

        if (endY <= startY) {
            return;
        }

        int rows = Math.max(2, (int) ((endY - startY) / rowSpacing) + 1);

        int leftLaneCount = 5;
        int rightLaneCount = 5;
        int lastLeftLane = -1;
        int lastRightLane = -1;

        for (int row = 0; row < rows; row++) {
            float bumperY = Math.min(endY, startY + row * rowSpacing + MathUtils.random(-20f, 20f));

            boolean spawnLeft;
            boolean spawnRight;

            switch (row % 3) {
                case 0:
                    spawnLeft = true;
                    spawnRight = true;
                    break;
                case 1:
                    spawnLeft = true;
                    spawnRight = MathUtils.randomBoolean(0.65f);
                    break;
                default:
                    spawnLeft = MathUtils.randomBoolean(0.65f);
                    spawnRight = true;
                    break;
            }

            if (spawnLeft) {
                float scale = MathUtils.random(BUMPER_RANDOM_SCALE_MIN, BUMPER_RANDOM_SCALE_MAX);
                float bumperWidth = baseBumperWidth * scale;
                float bumperHeight = baseBumperHeight * scale;

                float leftMaxX = centerX - bumperWidth - MINIMUM_BUMPER_CENTER_GAP;
                if (leftMaxX >= leftMinX) {
                    float[] leftLanes = buildLanes(leftMinX, leftMaxX, leftLaneCount);
                    int lane = chooseLane(leftLanes.length, lastLeftLane);
                    lastLeftLane = lane;
                    float bumperX = jitterLane(leftLanes[lane], leftMinX, leftMaxX, 26f);

                    if (canPlaceBumper(bumperX, bumperY, bumperWidth, bumperHeight)) {
                        bumpers.add(new PlatformBumper(world, bumperX, bumperY, bumperWidth, bumperHeight, true, material, stage));

                        if (row % 2 == 0) {
                            float cliffY = bumperY + cliffOffsetY;
                            if (cliffY + cliffHeight <= y + height - 16f) {
                                cliffs.add(new Cliff(world, getLeftInnerWallX() - CLIFF_WALL_INSET - 30, cliffY, cliffWidth, cliffHeight, Orientation.RIGHT, material, stage));
                            }
                        }
                    }
                }
            }

            if (spawnRight) {
                float scale = MathUtils.random(BUMPER_RANDOM_SCALE_MIN, BUMPER_RANDOM_SCALE_MAX);
                float bumperWidth = baseBumperWidth * scale;
                float bumperHeight = baseBumperHeight * scale;

                float rightMinX = centerX + MINIMUM_BUMPER_CENTER_GAP;
                float rightMaxX = rightMaxXBase - bumperWidth;
                if (rightMaxX >= rightMinX) {
                    float[] rightLanes = buildLanes(rightMinX, rightMaxX, rightLaneCount);
                    int lane = chooseLane(rightLanes.length, lastRightLane);
                    lastRightLane = lane;
                    float bumperX = jitterLane(rightLanes[lane], rightMinX, rightMaxX, 26f);

                    if (canPlaceBumper(bumperX, bumperY, bumperWidth, bumperHeight)) {
                        bumpers.add(new PlatformBumper(world, bumperX, bumperY, bumperWidth, bumperHeight, false, material, stage));

                        if (row % 2 == 0) {
                            float cliffY = bumperY + cliffOffsetY;
                            if (cliffY + cliffHeight <= y + height - 16f) {
                                cliffs.add(new Cliff(world, getRightInnerWallX() - cliffWidth + CLIFF_WALL_INSET + 30, cliffY, cliffWidth, cliffHeight, Orientation.LEFT, material, stage));
                            }
                        }
                    }
                }
            }
        }
    }

    private float getLeftInnerWallX() {
        return leftWall.getX() + leftWall.getWidth();
    }

    private float getRightInnerWallX() {
        return rightWall.getX();
    }

    private boolean canPlaceBumper(float bumperX, float bumperY, float bumperWidth, float bumperHeight) {
        float bumperLeft = bumperX;
        float bumperRight = bumperX + bumperWidth;
        float bumperBottom = bumperY;
        float bumperTop = bumperY + bumperHeight;

        for (int i = 0; i < cliffs.size; i++) {
            Cliff cliff = cliffs.get(i);

            float cliffLeft = cliff.getX();
            float cliffRight = cliff.getX() + cliff.getWidth();
            float cliffBottom = cliff.getY();
            float cliffTop = cliff.getY() + cliff.getHeight();

            boolean horizontalTooClose = bumperRight + MINIMUM_BUMPER_CLIFF_HORIZONTAL_GAP > cliffLeft && bumperLeft - MINIMUM_BUMPER_CLIFF_HORIZONTAL_GAP < cliffRight;

            boolean verticalTooClose = bumperTop + MINIMUM_BUMPER_CLIFF_VERTICAL_GAP > cliffBottom && bumperBottom - MINIMUM_BUMPER_CLIFF_VERTICAL_GAP < cliffTop;

            if (horizontalTooClose && verticalTooClose) {
                return false;
            }
        }

        return true;
    }

    private float[] buildLanes(float minX, float maxX, int laneCount) {
        if (maxX < minX) {
            return new float[0];
        }

        if (laneCount <= 1) {
            return new float[]{(minX + maxX) * 0.5f};
        }

        float span = maxX - minX;
        if (span < 80f) {
            return new float[]{minX, (minX + maxX) * 0.5f, maxX};
        }

        float[] lanes = new float[laneCount];
        for (int i = 0; i < laneCount; i++) {
            float t = laneCount == 1 ? 0.5f : (float) i / (float) (laneCount - 1);
            lanes[i] = minX + span * t;
        }
        return lanes;
    }

    private int chooseLane(int laneCount, int lastLane) {
        if (laneCount <= 1) {
            return 0;
        }

        int lane = MathUtils.random(laneCount - 1);
        if (lane == lastLane) {
            lane = (lane + 1 + MathUtils.random(laneCount - 2)) % laneCount;
        }
        return lane;
    }

    private float jitterLane(float laneX, float minX, float maxX, float amount) {
        return MathUtils.clamp(laneX + MathUtils.random(-amount, amount), minX, maxX);
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;

        disposeActor(leftWall);
        disposeActor(rightWall);
        disposeActor(bottomWall);

        for (int i = 0; i < cliffs.size; i++) {
            disposeActor(cliffs.get(i));
        }

        for (int i = 0; i < bumpers.size; i++) {
            disposeActor(bumpers.get(i));
        }

        cliffs.clear();
        bumpers.clear();
    }

    private void disposeActor(PhysicsActor actor) {
        if (actor == null) return;

        if (actor.getBody() != null) {
            world.destroyBody(actor.getBody());
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

    public boolean isDisposed() {
        return disposed;
    }
}
