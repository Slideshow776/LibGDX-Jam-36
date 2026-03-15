package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PlatformActor extends Actor {

    private final Texture texture;
    private final Body body;
    private final float platformWidth;
    private final float platformHeight;

    public PlatformActor(World world, Texture texture, float x, float y, float width, float height) {
        this.texture = texture;
        this.body = PhysicsFactory.createPlatform(world, x, y, width, height);
        this.platformWidth = width;
        this.platformHeight = height;
        setBounds(x - width * 0.5f, y - height * 0.5f, width, height);
    }

    public void syncVisual() {
        setPosition(body.getPosition().x - platformWidth * 0.5f, body.getPosition().y - platformHeight * 0.5f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), platformWidth * 0.5f, platformHeight * 0.5f, platformWidth, platformHeight, 1f, 1f, 0f, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    public void setHorizontalVelocity(float velocityX) {
        body.setLinearVelocity(velocityX, 0f);
    }

    public void clampX(float minX, float maxX) {
        Vector2 position = body.getPosition();
        float clampedX = Math.max(minX, Math.min(maxX, position.x));

        if (clampedX != position.x) {
            body.setTransform(clampedX, position.y, 0f);
            body.setLinearVelocity(0f, 0f);
        }
    }

    public Body getBody() {
        return body;
    }

    public float getPlatformWidth() {
        return platformWidth;
    }

    public void destroy(World world) {
        world.destroyBody(body);
    }
}
