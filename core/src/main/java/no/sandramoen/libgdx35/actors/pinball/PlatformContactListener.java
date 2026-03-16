package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class PlatformContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {}

    @Override
    public void endContact(Contact contact) {}

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        Object ua = a.getBody().getUserData();
        Object ub = b.getBody().getUserData();

        Ball ball = null;
        if (ua instanceof Ball) {
            ball = (Ball) ua;
            ball.setMaterial(((PhysicsActor) ub).getMaterial());
        } else if (ub instanceof Ball) {
            ball = (Ball) ub;
            ball.setMaterial(((PhysicsActor) ua).getMaterial());
        }

        if (ball == null)
            return;

        WorldManifold manifold = contact.getWorldManifold();
        Vector2[] points = manifold.getPoints();
        if (points == null || points.length == 0 || points[0] == null) {
            return;
        }

        float ballCenterY = ball.getBody().getPosition().y;
        float contactY = points[0].y;

        boolean hitFromBelow = contactY > ballCenterY;

        if (hitFromBelow) {
            contact.setEnabled(false);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }
}
