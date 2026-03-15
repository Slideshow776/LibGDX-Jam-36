package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PlatformActor extends Actor {

    private final Texture texture;
    private final Body body;
    private final float platformWidth;
    private final float platformHeight;
    private final PlatformMaterial material;
    private boolean broken;

    public PlatformActor(World world, float x, float y, float width, float height, PlatformMaterial material) {
        this.texture = new Texture(Gdx.files.internal(material.getTexturePath()));
        this.body = PhysicsFactory.createPlatform(world, x, y, width, height, material);
        this.platformWidth = width;
        this.platformHeight = height;
        this.material = material;
        this.body.setUserData(this);
        setBounds(x - width * 0.5f, y - height * 0.5f, width, height);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        setPosition(body.getPosition().x - platformWidth * 0.5f, body.getPosition().y - platformHeight * 0.5f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), platformWidth * 0.5f, platformHeight * 0.5f, platformWidth, platformHeight, 1f, 1f, 0f, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    public void syncVisual() {
        setPosition(body.getPosition().x - platformWidth * 0.5f, body.getPosition().y - platformHeight * 0.5f);
    }

    public void setHorizontalVelocity(float velocityX) {
        body.setLinearVelocity(velocityX, 0f);
    }

    public void clampX(float minX, float maxX) {
        float clampedX = Math.max(minX, Math.min(maxX, body.getPosition().x));
        if (clampedX != body.getPosition().x) {
            body.setTransform(clampedX, body.getPosition().y, body.getAngle());
            body.setLinearVelocity(0f, 0f);
        }
    }

    public void breakPlatform() {
        broken = true;
    }

    public boolean isBroken() {
        return broken;
    }

    public PlatformMaterial getMaterial() {
        return material;
    }

    public Body getBody() {
        return body;
    }

    public float getPlatformWidth() {
        return platformWidth;
    }

    public float getPlatformHeight() {
        return platformHeight;
    }

    public void destroy(World world) {
        world.destroyBody(body);
    }
}
