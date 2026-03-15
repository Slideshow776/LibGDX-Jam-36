package no.sandramoen.libgdx35.screens.gameplay.pinball;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import no.sandramoen.libgdx35.utils.BaseGame;

public class BackgroundActor extends Actor {

    private final Texture texture;

    public BackgroundActor(Texture texture) {
        this.texture = texture;
        setBounds(0f, -100000f, BaseGame.WORLD_WIDTH, 200000f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
