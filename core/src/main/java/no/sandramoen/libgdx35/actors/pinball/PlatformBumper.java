package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class PlatformBumper extends PhysicsActor {

    private Texture texture;
    private final Body body;
    private final Body anchor;
    private final RevoluteJoint joint;
    private final boolean left;
    private Material material;

    private boolean raised;

    private float restAngleDeg = -45f;
    private float raisedAngleDeg = 45f;
    private float motorSpeedDeg = 3000f;
    private float maxMotorTorque = 3000f;

    public PlatformBumper(World world, float x, float y, float width, float height, boolean left, Material material, Stage stage) {
        super(world, x, y, material, stage);

        this.left = left;
        this.material = material == null ? Material.METAL : material;
        this.texture = material.getPlatformTexture();

        if (!left) {
            restAngleDeg = 45f;
            raisedAngleDeg = -45f;
        }

        super.setBounds(x, y, width, height);
        updateOrigin();

        float hingeX = left ? x : x + width;
        float hingeY = y + height * 0.5f;

        BodyDef anchorDef = new BodyDef();
        anchorDef.type = BodyDef.BodyType.StaticBody;
        anchorDef.position.set(toMeters(hingeX), toMeters(hingeY));
        anchor = world.createBody(anchorDef);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(anchorDef.position);
        body = world.createBody(bodyDef);
        body.setBullet(true);
        body.setSleepingAllowed(false);
        body.setUserData(this);

        createFixture();

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.initialize(anchor, body, anchor.getWorldCenter());
        jointDef.enableLimit = true;
        jointDef.enableMotor = true;
        jointDef.maxMotorTorque = maxMotorTorque;
        jointDef.motorSpeed = 0f;
        jointDef.lowerAngle = Math.min(restAngleDeg, raisedAngleDeg) * MathUtils.degreesToRadians;
        jointDef.upperAngle = Math.max(restAngleDeg, raisedAngleDeg) * MathUtils.degreesToRadians;

        joint = (RevoluteJoint) world.createJoint(jointDef);

        body.setTransform(body.getPosition(), restAngleDeg * MathUtils.degreesToRadians);
        syncFromBody();
    }

    @Override
    public void act(float delta) {
        float speed = raised ? motorSpeedDeg : -motorSpeedDeg;
        if (!left) speed = -speed;

        joint.setMotorSpeed(speed * MathUtils.degreesToRadians);
        syncFromBody();
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        updateOrigin();
        rebuildBodiesFromActorBounds();
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        rebuildBodiesFromActorBounds();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        updateOrigin();
        rebuildBodiesFromActorBounds();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        updateOrigin();
        rebuildBodiesFromActorBounds();
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        updateOrigin();
        rebuildBodiesFromActorBounds();
    }

    private void rebuildBodiesFromActorBounds() {
        if (body == null || anchor == null) return;

        float hingeX = left ? getX() : getX() + getWidth();
        float hingeY = getY() + getHeight() * 0.5f;

        anchor.setTransform(toMeters(hingeX), toMeters(hingeY), 0f);
        body.setTransform(toMeters(hingeX), toMeters(hingeY), body.getAngle());

        createFixture();
        syncFromBody();
    }

    private void createFixture() {
        while (body.getFixtureList().size > 0) {
            body.destroyFixture(body.getFixtureList().first());
        }

        float halfW = toMeters(getWidth() * 0.5f);
        float halfH = toMeters(getHeight() * 0.5f);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfW, halfH, new Vector2(left ? halfW : -halfW, 0f), 0f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = material.getDensity();
        fix.friction = material.getFriction();
        fix.restitution = material.getRestitution();

        body.createFixture(fix);
        body.resetMassData();
        shape.dispose();
    }

    private void syncFromBody() {
        float hingeX = toPixels(body.getPosition().x);
        float hingeY = toPixels(body.getPosition().y);

        float drawX = left ? hingeX : hingeX - getWidth();
        float drawY = hingeY - getHeight() * 0.5f;

        super.setPosition(drawX, drawY);
        super.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    private void updateOrigin() {
        if (left) {
            setOrigin(0f, getHeight() * 0.5f);
        } else {
            setOrigin(getWidth(), getHeight() * 0.5f);
        }
    }

    @Override
    protected Texture getTexture() {
        return texture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1f, 1f, getRotation(), 0, 0, texture.getWidth(), texture.getHeight(), !left, false);
    }

    @Override
    protected void updateBodyShape() {
    }

    @Override
    protected void updateBodyTransform() {
    }

    @Override
    public Body getBody() {
        return body;
    }

    public void setRaised(boolean raised) {
        this.raised = raised;
    }

    public boolean isRaised() {
        return raised;
    }

    public boolean isLeft() {
        return left;
    }
}
