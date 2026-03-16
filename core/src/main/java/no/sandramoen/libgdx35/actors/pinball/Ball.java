package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Ball extends PhysicsActor {

    private final Texture texture;
    private final Body body;

    public Ball(World world, float x, float y, float width, float height, Material material, Stage stage) {
        super(world, x, y, material, stage);

        this.material = material == null ? Material.METAL : material;
        this.texture = new Texture(Gdx.files.internal("images/ball.png"));

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(toMeters(x + width * 0.5f), toMeters(y + height * 0.5f));

        body = world.createBody(def);
        body.setBullet(true);
        body.setSleepingAllowed(false);
        body.setUserData(this);

        super.setBounds(x, y, width, height);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    protected Texture getTexture() {
        return texture;
    }

    @Override
    protected void updateBodyShape() {
        if (body == null) return;

        while (body.getFixtureList().size > 0) {
            body.destroyFixture(body.getFixtureList().first());
        }

        CircleShape shape = new CircleShape();
        shape.setRadius(toMeters(Math.min(getWidth(), getHeight()) * 0.5f));

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = material.getDensity();
        fix.friction = material.getFriction();
        fix.restitution = material.getRestitution();

        body.createFixture(fix);
        shape.dispose();

        body.resetMassData();
    }

    @Override
    public Body getBody() {
        return body;
    }
}
