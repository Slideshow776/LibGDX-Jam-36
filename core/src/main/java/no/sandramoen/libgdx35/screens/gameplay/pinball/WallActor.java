package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class WallActor extends Actor {

    private final Texture texture;
    private final Body body;

    public WallActor(World world, PlatformMaterial material, float x, float width, float height) {
        this.texture = new Texture(Gdx.files.internal(material.getTexturePath()));
        this.body = PhysicsFactory.createVerticalWall(world, x, material);

        setBounds(x - width * 0.5f, -height * 0.5f, width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    public void destroy(World world) {
        world.destroyBody(body);
        texture.dispose();
    }
}
