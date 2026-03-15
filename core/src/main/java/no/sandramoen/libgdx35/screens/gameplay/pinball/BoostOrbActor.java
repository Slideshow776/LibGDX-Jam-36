package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BoostOrbActor extends Actor {

    private final Texture texture;
    private final Body body;
    private final float radius;
    private final float boostImpulse;
    private boolean consumed;

    public BoostOrbActor(World world, Texture texture, float x, float y, float radius, float boostImpulse) {
        this.texture = texture;
        this.body = PhysicsFactory.createBoostOrb(world, x, y, radius);
        this.radius = radius;
        this.boostImpulse = boostImpulse;
        this.body.setUserData(this);
        setBounds(x - radius, y - radius, radius * 2f, radius * 2f);
    }

    public void syncVisual() {
        setPosition(body.getPosition().x - radius, body.getPosition().y - radius);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!consumed) {
            batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        }
    }

    public void consume(BallActor ballActor) {
        if (consumed) {
            return;
        }

        consumed = true;
        ballActor.getBody().applyLinearImpulse(0f, boostImpulse, ballActor.getBody().getWorldCenter().x, ballActor.getBody().getWorldCenter().y, true);
    }

    public boolean isConsumed() {
        return consumed;
    }

    public Body getBody() {
        return body;
    }

    public void destroy(World world) {
        world.destroyBody(body);
    }
}
