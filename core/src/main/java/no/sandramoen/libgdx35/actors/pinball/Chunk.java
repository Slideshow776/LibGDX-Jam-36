package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class Chunk {

    private static final float MINIMUM_BUMPER_WALL_SPACING = 72f;
    private static final float MINIMUM_BUMPER_CENTER_GAP = 8f;
    private static final float MINIMUM_BUMPER_CLIFF_VERTICAL_GAP = 256f;
    private static final float MINIMUM_BUMPER_CLIFF_HORIZONTAL_GAP = 96f;

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

        leftWall = new Wall(world, x, y, wallThickness, height, this.material, stage);
        rightWall = new Wall(world, x + width - wallThickness, y, wallThickness, height, this.material, stage);
        bottomWall = withBottomWall ? new Wall(world, x, y, width, wallThickness, this.material, stage) : null;

        generateBumpersAndCliffs();
    }

    private void generateBumpersAndCliffs() {
        float bumperWidth = 280f;
        float bumperHeight = 64f;
        float cliffWidth = 128f;
        float cliffHeight = 256f;
        float cliffOffsetY = 256f;

        float centerX = x + width * 0.5f;
        float leftMinX = x + wallThickness + MINIMUM_BUMPER_WALL_SPACING;
        float leftMaxX = centerX - bumperWidth - MINIMUM_BUMPER_CENTER_GAP;

        float rightMinX = centerX + MINIMUM_BUMPER_CENTER_GAP;
        float rightMaxX = x + width - wallThickness - bumperWidth - MINIMUM_BUMPER_WALL_SPACING;

        if (leftMaxX < leftMinX && rightMaxX < rightMinX) {
            return;
        }

        float startY = y + wallThickness + 200f;
        float endY = y + height - cliffOffsetY - cliffHeight - 96f;
        float rowSpacing = 360f;

        if (endY <= startY) {
            return;
        }

        float[] leftLanes = buildLanes(leftMinX, leftMaxX);
        float[] rightLanes = buildLanes(rightMinX, rightMaxX);

        int rows = Math.max(1, (int) ((endY - startY) / rowSpacing) + 1);

        int lastLeftLane = -1;
        int lastRightLane = -1;

        for (int row = 0; row < rows; row++) {
            float bumperY = Math.min(endY, startY + row * rowSpacing + MathUtils.random(-28f, 28f));

            boolean canSpawnLeft = leftLanes.length > 0;
            boolean canSpawnRight = rightLanes.length > 0;

            boolean spawnLeft = false;
            boolean spawnRight = false;

            switch (row % 4) {
                case 0:
                    spawnLeft = canSpawnLeft;
                    spawnRight = canSpawnRight;
                    break;
                case 1:
                    spawnLeft = canSpawnLeft;
                    spawnRight = canSpawnRight && MathUtils.randomBoolean(0.35f);
                    break;
                case 2:
                    spawnLeft = canSpawnLeft && MathUtils.randomBoolean(0.35f);
                    spawnRight = canSpawnRight;
                    break;
                default:
                    spawnLeft = canSpawnLeft && MathUtils.randomBoolean();
                    spawnRight = canSpawnRight && !spawnLeft;
                    break;
            }

            if (!spawnLeft && !spawnRight) {
                if (canSpawnLeft && canSpawnRight) {
                    if (MathUtils.randomBoolean()) spawnLeft = true;
                    else spawnRight = true;
                } else if (canSpawnLeft) {
                    spawnLeft = true;
                } else if (canSpawnRight) {
                    spawnRight = true;
                }
            }

            if (spawnLeft) {
                int lane = chooseLane(leftLanes.length, lastLeftLane);
                lastLeftLane = lane;
                float bumperX = jitterLane(leftLanes[lane], leftMinX, leftMaxX, 18f);

                if (canPlaceBumper(bumperX, bumperY, bumperWidth, bumperHeight)) {
                    bumpers.add(new PlatformBumper(world, bumperX, bumperY, bumperWidth, bumperHeight, true, material, stage));

                    float cliffY = bumperY + cliffOffsetY;
                    if (cliffY + cliffHeight <= y + height - 16f) {
                        cliffs.add(new Cliff(
                            world,
                            leftWall.getX() + leftWall.getWidth(),
                            cliffY,
                            cliffWidth,
                            cliffHeight,
                            Orientation.RIGHT,
                            material,
                            stage
                        ));
                    }
                }
            }

            if (spawnRight) {
                int lane = chooseLane(rightLanes.length, lastRightLane);
                lastRightLane = lane;
                float bumperX = jitterLane(rightLanes[lane], rightMinX, rightMaxX, 18f);

                if (canPlaceBumper(bumperX, bumperY, bumperWidth, bumperHeight)) {
                    bumpers.add(new PlatformBumper(world, bumperX, bumperY, bumperWidth, bumperHeight, false, material, stage));

                    float cliffY = bumperY + cliffOffsetY;
                    if (cliffY + cliffHeight <= y + height - 16f) {
                        cliffs.add(new Cliff(
                            world,
                            rightWall.getX() - cliffWidth,
                            cliffY,
                            cliffWidth,
                            cliffHeight,
                            Orientation.LEFT,
                            material,
                            stage
                        ));
                    }
                }
            }
        }
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

            boolean horizontalTooClose =
                bumperRight + MINIMUM_BUMPER_CLIFF_HORIZONTAL_GAP > cliffLeft &&
                    bumperLeft - MINIMUM_BUMPER_CLIFF_HORIZONTAL_GAP < cliffRight;

            boolean verticalTooClose =
                bumperTop + MINIMUM_BUMPER_CLIFF_VERTICAL_GAP > cliffBottom &&
                    bumperBottom - MINIMUM_BUMPER_CLIFF_VERTICAL_GAP < cliffTop;

            if (horizontalTooClose && verticalTooClose) {
                return false;
            }
        }

        return true;
    }

    private float[] buildLanes(float minX, float maxX) {
        if (maxX < minX) {
            return new float[0];
        }

        float span = maxX - minX;

        if (span < 120f) {
            return new float[]{(minX + maxX) * 0.5f};
        }

        return new float[]{
            minX + span * 0.18f,
            minX + span * 0.50f,
            minX + span * 0.82f
        };
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
