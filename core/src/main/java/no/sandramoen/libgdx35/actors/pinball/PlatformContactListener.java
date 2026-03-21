package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import no.sandramoen.libgdx35.utils.AssetLoader;
import no.sandramoen.libgdx35.utils.BaseGame;

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
        Object other = null;
        if (ua instanceof Ball) {
            ball = (Ball) ua;
            ball.setMaterial(((PhysicsActor) ub).getMaterial());
            other = ub;
        } else if (ub instanceof Ball) {
            ball = (Ball) ub;
            ball.setMaterial(((PhysicsActor) ua).getMaterial());
            other = ua;
        }

        if (ball == null)
            return;

        if (other != null) {
            Material material = null;
            if (other instanceof Wall) {
                material = ((Wall) other).getMaterial();
            }
            else if (other instanceof PlatformBumper) {
                material = ((PlatformBumper) other).getMaterial();
            }
            else if (other instanceof Cliff) {
                material = ((Cliff) other).getMaterial();
            }

            if (material != null) {
                if (material == Material.METAL)
                    AssetLoader.metal_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                else if (material == Material.GLASS)
                    AssetLoader.glass_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                else if (material == Material.GUM)
                    AssetLoader.gum_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
            }
        }

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
