package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public enum Material {
    GUM(
        0.9f,
        0.08f,
        0.92f,
        "images/included/flipper/gum.png",
        "images/included/ball/gum.png",
        "images/included/wall/gum.png",
        "images/included/cliff/gum.png"
    ),

    METAL(
        6.5f,
        0.35f,
        0.08f,
        "images/included/flipper/metal.png",
        "images/included/ball/metal.png",
        "images/included/wall/metal.png",
        "images/included/cliff/gum.png"
    ),

    GLASS(
        3.2f,
        0.005f,
        0.02f,
        "images/included/flipper/glass.png",
        "images/included/ball/glass.png",
        "images/included/wall/glass.png",
        "images/included/cliff/gum.png"
    );

    private final float density;
    private final float friction;
    private final float restitution;

    private final String platformPath;
    private final String ballPath;
    private final String wallPath;
    private final String cliffPath;

    private Texture platformTexture;
    private Texture ballTexture;
    private Texture wallTexture;
    private Texture cliffTexture;

    Material(float density, float friction, float restitution, String platformPath, String ballPath, String wallPath, String cliffPath) {
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
        this.platformPath = platformPath;
        this.ballPath = ballPath;
        this.wallPath = wallPath;
        this.cliffPath = cliffPath;
    }

    public static Material getRandomMaterial() {
        return Material.values()[(int) (Math.random() * Material.values().length)];
    }

    public float getDensity() {
        return density;
    }

    public float getFriction() {
        return friction;
    }

    public float getRestitution() {
        return restitution;
    }

    public String getPlatformPath() {
        return platformPath;
    }

    public String getBallPath() {
        return ballPath;
    }

    public String getWallPath() {
        return wallPath;
    }

    public String getCliffPath() {
        return cliffPath;
    }

    public Texture getPlatformTexture() {
        if (platformTexture == null) {
            platformTexture = new Texture(Gdx.files.internal(platformPath));
        }
        return platformTexture;
    }

    public Texture getBallTexture() {
        if (ballTexture == null) {
            ballTexture = new Texture(Gdx.files.internal(ballPath));
        }
        return ballTexture;
    }

    public Texture getWallTexture() {
        if (wallTexture == null) {
            wallTexture = new Texture(Gdx.files.internal(wallPath));
        }
        return wallTexture;
    }

    public Texture getCliffTexture() {
        if (cliffTexture == null) {
            cliffTexture = new Texture(Gdx.files.internal(cliffPath));
        }
        return cliffTexture;
    }

    public void disposeTextures() {
        if (platformTexture != null) {
            platformTexture.dispose();
            platformTexture = null;
        }

        if (ballTexture != null) {
            ballTexture.dispose();
            ballTexture = null;
        }

        if (wallTexture != null) {
            wallTexture.dispose();
            wallTexture = null;
        }

        if (cliffTexture != null) {
            cliffTexture.dispose();
            cliffTexture = null;
        }
    }
}
