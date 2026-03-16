package no.sandramoen.libgdx35.actors.pinball;

public enum Orientation {
    RIGHT(0f),
    UP(90f),
    LEFT(180f),
    DOWN(270f);

    private final float rotationDeg;

    Orientation(float rotationDeg) {
        this.rotationDeg = rotationDeg;
    }

    public float getRotationDeg() {
        return rotationDeg;
    }
}
