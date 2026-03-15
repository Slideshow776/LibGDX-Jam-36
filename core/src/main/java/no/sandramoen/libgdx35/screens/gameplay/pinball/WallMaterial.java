package no.sandramoen.libgdx35.screens.gameplay.pinball;

public enum WallMaterial {

    STONE("images/wall_stone.png", 0.88f, 0.2f),
    METAL("images/wall_metal.png", 1.05f, 0.15f),
    RUBBER("images/wall_rubber.png", 1.25f, 0.05f);

    private final String texturePath;
    private final float restitution;
    private final float friction;

    WallMaterial(String texturePath, float restitution, float friction) {
        this.texturePath = texturePath;
        this.restitution = restitution;
        this.friction = friction;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public float getRestitution() {
        return restitution;
    }

    public float getFriction() {
        return friction;
    }
}
