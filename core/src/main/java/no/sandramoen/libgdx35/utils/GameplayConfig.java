package no.sandramoen.libgdx35.utils;

public final class GameplayConfig {

    public static final float TIME_STEP = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final int POSITION_ITERATIONS = 3;

    public static final float BALL_RADIUS = 0.28f;
    public static final float BALL_SIZE = BALL_RADIUS * 2f;
    public static final float BALL_LAUNCH_SPEED = 10.5f;

    public static final float FLIPPER_WIDTH = 2.2f;
    public static final float FLIPPER_HEIGHT = 0.32f;

    public static final float LEFT_FLIPPER_X = 5.2f;
    public static final float RIGHT_FLIPPER_X = 10.8f;
    public static final float FLIPPER_Y = 2.0f;

    public static final float LEFT_FLIPPER_REST_ANGLE_DEGREES = -12f;
    public static final float RIGHT_FLIPPER_REST_ANGLE_DEGREES = 12f;

    public static final float FLIPPER_REST_SPEED = -10f;
    public static final float FLIPPER_ACTIVE_SPEED = 22f;
    public static final float FLIPPER_TORQUE = 5000f;

    public static final int PLATFORM_COUNT = 10;
    public static final float PLATFORM_MIN_WIDTH = 1.8f;
    public static final float PLATFORM_MAX_WIDTH = 3.1f;
    public static final float PLATFORM_HEIGHT = 0.38f;
    public static final float PLATFORM_MIN_Y = 4.2f;
    public static final float PLATFORM_MAX_Y = 13.0f;
    public static final float PLATFORM_MOVE_SPEED = 4.5f;

    public static final String BACKGROUND_PATH = "images/background.png";
    public static final String BALL_PATH = "images/ball.png";
    public static final String FLIPPER_PATH = "images/flipper.png";
    public static final String PLATFORM_PATH = "images/platform.png";

    private GameplayConfig() {
    }
}
