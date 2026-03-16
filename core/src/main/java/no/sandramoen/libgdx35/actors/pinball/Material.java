package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public enum Material {
    GUM(0.8f, 0.2f, 0.6f, "images/gum.png"),
    METAL(5f, 0.2f, 0f, "images/metal.png"),
    GLASS(4f, 0.01f, 0.05f, "images/glass.png");

    private final float density;
    private final float friction;
    private final float restitution;
    private final String texturePath;

    private Texture texture;

    Material(float density, float friction, float restitution, String texturePath) {
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
        this.texturePath = texturePath;
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

    public String getTexturePath() {
        return texturePath;
    }

    public Texture getTexture() {
        if (texture == null) {
            texture = new Texture(Gdx.files.internal(texturePath));
        }
        return texture;
    }

    public void disposeTexture() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
