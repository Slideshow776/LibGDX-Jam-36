package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import no.sandramoen.libgdx35.utils.BaseActor;
import no.sandramoen.libgdx35.utils.BaseGame;

public abstract class PhysicsActor extends BaseActor {

    protected final World world;
    private boolean syncingFromBody;
    protected Material material;

    private static ShaderProgram materialShader;

    public PhysicsActor(World world, float x, float y, Material material, Stage stage) {
        super(x, y, stage);
        this.world = world;
        this.material = material == null ? Material.METAL : material;
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        boolean positionChanged = getX() != x || getY() != y;
        boolean sizeChanged = getWidth() != width || getHeight() != height;

        super.setBounds(x, y, width, height);

        if (syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        if (sizeChanged) {
            updateBodyShape();
            applyMaterialToBody();
        }

        if (positionChanged || sizeChanged) {
            updateBodyTransform();
        }
    }

    @Override
    public void setPosition(float x, float y) {
        boolean changed = getX() != x || getY() != y;
        super.setPosition(x, y);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyTransform();
    }

    @Override
    public void setX(float x) {
        boolean changed = getX() != x;
        super.setX(x);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyTransform();
    }

    @Override
    public void setY(float y) {
        boolean changed = getY() != y;
        super.setY(y);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyTransform();
    }

    @Override
    public void moveBy(float x, float y) {
        boolean changed = x != 0f || y != 0f;
        super.moveBy(x, y);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyTransform();
    }

    @Override
    public void setWidth(float width) {
        boolean changed = getWidth() != width;
        super.setWidth(width);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyShape();
        applyMaterialToBody();
        updateBodyTransform();
    }

    @Override
    public void setHeight(float height) {
        boolean changed = getHeight() != height;
        super.setHeight(height);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyShape();
        applyMaterialToBody();
        updateBodyTransform();
    }

    @Override
    public void setSize(float width, float height) {
        boolean changed = getWidth() != width || getHeight() != height;
        super.setSize(width, height);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyShape();
        applyMaterialToBody();
        updateBodyTransform();
    }

    @Override
    public void sizeBy(float size) {
        boolean changed = size != 0f;
        super.sizeBy(size);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyShape();
        applyMaterialToBody();
        updateBodyTransform();
    }

    @Override
    public void sizeBy(float width, float height) {
        boolean changed = width != 0f || height != 0f;
        super.sizeBy(width, height);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyShape();
        applyMaterialToBody();
        updateBodyTransform();
    }

    @Override
    public void setRotation(float degrees) {
        boolean changed = getRotation() != degrees;
        super.setRotation(degrees);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyTransform();
    }

    @Override
    public void rotateBy(float amountInDegrees) {
        boolean changed = amountInDegrees != 0f;
        super.rotateBy(amountInDegrees);

        if (!changed || syncingFromBody) return;

        Body body = getBody();
        if (body == null) return;

        updateBodyTransform();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Body body = getBody();
        if (body == null) return;

        float ppm = BaseGame.PIXELS_PER_METER;
        float px = body.getPosition().x * ppm;
        float py = body.getPosition().y * ppm;
        float rotation = body.getAngle() * MathUtils.radiansToDegrees;

        syncingFromBody = true;
        super.setPosition(px - getWidth() * 0.5f, py - getHeight() * 0.5f);
        super.setRotation(rotation);
        syncingFromBody = false;
    }

    protected void updateBodyTransform() {
        Body body = getBody();
        if (body == null) return;

        float centerX = toMeters(getX() + getWidth() * 0.5f);
        float centerY = toMeters(getY() + getHeight() * 0.5f);
        float angle = getRotation() * MathUtils.degreesToRadians;

        body.setTransform(centerX, centerY, angle);
    }

    protected void applyMaterialToBody() {
        Body body = getBody();
        if (body == null || material == null) return;

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setDensity(material.getDensity());
            fixture.setFriction(material.getFriction());
            fixture.setRestitution(material.getRestitution());
        }

        body.resetMassData();
    }

    protected float toMeters(float pixels) {
        return pixels / BaseGame.PIXELS_PER_METER;
    }

    protected float toPixels(float meters) {
        return meters * BaseGame.PIXELS_PER_METER;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Texture texture = getTexture();
        if (texture == null) return;

        boolean flipX = getScaleX() < 0f;
        boolean flipY = getScaleY() < 0f;

        batch.draw(texture, getX(), getY(), getWidth() * 0.5f, getHeight() * 0.5f, getWidth(), getHeight(), Math.abs(getScaleX()), Math.abs(getScaleY()), getRotation(), 0, 0, texture.getWidth(), texture.getHeight(), flipX, flipY);
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        Material resolved = material == null ? Material.METAL : material;
        this.material = resolved;

        Body body = getBody();
        if (body != null) {
            applyMaterialToBody();
        }
    }

    protected abstract Texture getTexture();

    protected abstract void updateBodyShape();

    public abstract Body getBody();
}
