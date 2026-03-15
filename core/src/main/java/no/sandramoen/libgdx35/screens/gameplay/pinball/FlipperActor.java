package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class FlipperActor extends Actor {

    private final Texture texture;
    private final Body anchorBody;
    private final Body body;
    private final RevoluteJoint joint;
    private final boolean left;
    private final float width;
    private final float height;

    public FlipperActor(World world, Texture texture, float anchorX, float y, boolean left, float width, float height, float restAngleDegrees, float lowerAngleDegrees, float upperAngleDegrees, float restSpeed, float torque) {
        this.texture = texture;
        this.left = left;
        this.width = width;
        this.height = height;

        anchorBody = PhysicsFactory.createAnchor(world, anchorX, y);
        body = PhysicsFactory.createFlipper(world, anchorX + (left ? width * 0.5f : -width * 0.5f), y, width, height, restAngleDegrees);

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.initialize(anchorBody, body, new Vector2(anchorX, y));
        jointDef.enableLimit = true;
        jointDef.lowerAngle = lowerAngleDegrees * MathUtils.degreesToRadians;
        jointDef.upperAngle = upperAngleDegrees * MathUtils.degreesToRadians;
        jointDef.enableMotor = true;
        jointDef.maxMotorTorque = torque;
        jointDef.motorSpeed = restSpeed;
        joint = (RevoluteJoint) world.createJoint(jointDef);

        syncVisual();
    }

    public void syncVisual() {
        float originX = left ? 0f : width;
        setPosition(body.getPosition().x - originX, body.getPosition().y - height * 0.5f);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float originX = left ? 0f : width;

        batch.draw(texture, getX(), getY(), originX, height * 0.5f, width, height, 1f, 1f, getRotation(), 0, 0, texture.getWidth(), texture.getHeight(), false, !left);
    }

    public void setPressed(boolean pressed, float activeSpeed, float restSpeed) {
        if (left) {
            joint.setMotorSpeed(pressed ? activeSpeed : restSpeed);
        } else {
            joint.setMotorSpeed(pressed ? -activeSpeed : -restSpeed);
        }
    }

    public void destroy(World world) {
        world.destroyJoint(joint);
        world.destroyBody(body);
        world.destroyBody(anchorBody);
    }
}
