package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BallActor extends Actor {

    private final Texture texture;
    private final Body body;
    private final float radius;
    private final float size;

    public BallActor(World world, Texture texture, float x, float y, float radius, float size) {
        this.texture = texture;
        this.body = PhysicsFactory.createBall(world, x, y, radius);
        this.radius = radius;
        this.size = size;
        this.body.setUserData(this);
        setBounds(x - radius, y - radius, size, size);
    }

    public void syncVisual() {
        setPosition(body.getPosition().x - radius, body.getPosition().y - radius);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), radius, radius, size, size, 1f, 1f, getRotation(), 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    public Body getBody() {
        return body;
    }

    public void destroy(World world) {
        world.destroyBody(body);
    }
}
