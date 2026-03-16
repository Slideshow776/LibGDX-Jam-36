package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Wall extends PhysicsActor {

    private Texture texture;
    private final Body body;
    private Material material;

    public Wall(World world, float x, float y, float width, float height, Material material, Stage stage) {
        super(world, x, y, material, stage);

        this.material = material == null ? Material.METAL : material;
        this.texture = new Texture(Gdx.files.internal(this.material.getTexturePath()));

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(toMeters(x + width * 0.5f), toMeters(y + height * 0.5f));

        body = world.createBody(bodyDef);
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
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        updateBodyTransform();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    protected void updateBodyShape() {
        if (body == null) return;

        while (body.getFixtureList().size > 0) {
            body.destroyFixture(body.getFixtureList().first());
        }

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(toMeters(getWidth() * 0.5f), toMeters(getHeight() * 0.5f));

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = material.getDensity();
        fix.friction = material.getFriction();
        fix.restitution = material.getRestitution();

        body.createFixture(fix);
        body.resetMassData();
        shape.dispose();
    }

    @Override
    protected void updateBodyTransform() {
        if (body == null) return;

        body.setTransform(toMeters(getX() + getWidth() * 0.5f), toMeters(getY() + getHeight() * 0.5f), 0f);
    }

    @Override
    public Body getBody() {
        return body;
    }
}
