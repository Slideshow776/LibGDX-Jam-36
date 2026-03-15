package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;

public final class PhysicsFactory {

    public static final short CATEGORY_BALL = 0x0001;
    public static final short CATEGORY_PLATFORM = 0x0002;
    public static final short CATEGORY_FLIPPER = 0x0004;
    public static final short CATEGORY_WALL = 0x0008;
    public static final short CATEGORY_ORB = 0x0010;

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
        fixtureDef.filter.categoryBits = CATEGORY_WALL;
        fixtureDef.filter.maskBits = CATEGORY_BALL;

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
        bodyDef.bullet = true;
        bodyDef.fixedRotation = false;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f, height * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 5f;
        fixtureDef.restitution = 0.05f;
        fixtureDef.friction = 0.9f;
        fixtureDef.filter.categoryBits = CATEGORY_FLIPPER;
        fixtureDef.filter.maskBits = CATEGORY_BALL;

        body.createFixture(fixtureDef);
        body.setGravityScale(0f);
        body.setLinearDamping(0f);
        body.setAngularDamping(0f);
        body.setSleepingAllowed(false);
        body.setTransform(x, y, angleDegrees * MathUtils.degreesToRadians);

        shape.dispose();
        return body;
    }

    public static Body createPlatform(World world, float x, float y, float width, float height, PlatformMaterial material) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f, height * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.restitution = material.getRestitution();
        fixtureDef.friction = material.getFriction();
        fixtureDef.filter.categoryBits = CATEGORY_PLATFORM;
        fixtureDef.filter.maskBits = CATEGORY_BALL;

        body.createFixture(fixtureDef);
        body.setGravityScale(0f);
        body.setSleepingAllowed(false);

        shape.dispose();
        return body;
    }

    public static Body createVerticalWall(World world, float x, PlatformMaterial material) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        Body body = world.createBody(bodyDef);

        EdgeShape shape = new EdgeShape();
        shape.set(x, -100000f, x, 100000f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.restitution = material.getRestitution();
        fixtureDef.friction = material.getFriction();
        fixtureDef.filter.categoryBits = CATEGORY_WALL;
        fixtureDef.filter.maskBits = CATEGORY_BALL | CATEGORY_FLIPPER;

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
        fixtureDef.density = 1.4f;
        fixtureDef.restitution = 0.55f;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = CATEGORY_BALL;
        fixtureDef.filter.maskBits = CATEGORY_PLATFORM | CATEGORY_FLIPPER | CATEGORY_WALL | CATEGORY_ORB;

        body.createFixture(fixtureDef);
        body.setLinearDamping(0.02f);
        body.setAngularDamping(0.02f);
        body.setSleepingAllowed(false);

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
        fixtureDef.filter.categoryBits = CATEGORY_ORB;
        fixtureDef.filter.maskBits = CATEGORY_BALL;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }
}
