package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Cliff extends PhysicsActor {

    private Texture texture;
    private final Body body;
    private Material material;
    private Orientation orientation;

    public Cliff(World world, float x, float y, float width, float height, Orientation orientation, Material material, Stage stage) {
        super(world, x, y, material, stage);

        this.orientation = orientation == null ? Orientation.RIGHT : orientation;
        this.material = material == null ? Material.GUM : material;
        this.texture = material.getCliffTexture();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(toMeters(x + width * 0.5f), toMeters(y + height * 0.5f));
        bodyDef.angle = this.orientation.getRotationDeg() * MathUtils.degreesToRadians;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        super.setBounds(x, y, width, height);
        super.setOrigin(width * 0.5f, height * 0.5f);
        super.setRotation(this.orientation.getRotationDeg());

        updateBodyShape();
        updateBodyTransform();
    }


    @Override
    protected Texture getTexture() {
        return texture;
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        super.setOrigin(width * 0.5f, height * 0.5f);
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
        super.setOrigin(width * 0.5f, height * 0.5f);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        super.setOrigin(width * 0.5f, getHeight() * 0.5f);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        super.setOrigin(getWidth() * 0.5f, height * 0.5f);
        updateBodyShape();
        updateBodyTransform();
    }

    @Override
    protected void updateBodyShape() {
        if (body == null) return;

        while (body.getFixtureList().size > 0) {
            body.destroyFixture(body.getFixtureList().first());
        }

        float w = toMeters(getWidth());
        float h = toMeters(getHeight());

        float halfW = w * 0.5f;
        float halfH = h * 0.5f;

        PolygonShape shape = new PolygonShape();

        shape.set(new float[]{-halfW, -halfH, -halfW, halfH, halfW, 0f, -halfW * 0.1f, 0f});

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = material.getDensity();
        fix.friction = material.getFriction();
        fix.restitution = material.getRestitution();

        body.createFixture(fix);

        shape.dispose();
    }

    @Override
    protected void updateBodyTransform() {
        if (body == null) return;

        body.setTransform(toMeters(getX() + getWidth() * 0.5f), toMeters(getY() + getHeight() * 0.5f), getRotation() * MathUtils.degreesToRadians);
    }

    @Override
    public Body getBody() {
        return body;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null || this.orientation == orientation) return;

        this.orientation = orientation;
        super.setRotation(orientation.getRotationDeg());
        updateBodyTransform();
    }

    private String getTexturePath(Material material) {
        switch (material) {
            case METAL:
                return "images/cliff_metal.png";
            case GLASS:
                return "images/cliff_glass.png";
            case GUM:
            default:
                return "images/cliff_gum.png";
        }
    }
}
