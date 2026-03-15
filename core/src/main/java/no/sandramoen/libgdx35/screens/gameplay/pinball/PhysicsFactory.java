package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;

public final class PhysicsFactory {

    private PhysicsFactory() {
    }

    public static Body createVerticalWall(World world, float x) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        EdgeShape shape = new EdgeShape();
        shape.set(x, -100000f, x, 100000f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.restitution = 0.88f;
        fixtureDef.friction = 0.2f;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }

    public static Body createAnchor(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        return world.createBody(bodyDef);
    }

    public static Body createFlipper(World world, float x, float y, float width, float height, float angleDegrees) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f, height * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 2.5f;
        fixtureDef.restitution = 0.2f;
        fixtureDef.friction = 0.4f;

        body.createFixture(fixtureDef);
        body.setGravityScale(1f);
        body.setTransform(x, y, angleDegrees * MathUtils.degreesToRadians);

        shape.dispose();
        return body;
    }

    public static Body createPlatform(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f, height * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.restitution = 0.75f;
        fixtureDef.friction = 0.35f;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }

    public static Body createBall(World world, float x, float y, float radius) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.bullet = true;

        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.restitution = 0.92f;
        fixtureDef.friction = 0.1f;

        body.createFixture(fixtureDef);
        body.setLinearDamping(0.02f);
        body.setAngularDamping(0.02f);

        shape.dispose();
        return body;
    }

    public static Body createBoostOrb(World world, float x, float y, float radius) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }
}
