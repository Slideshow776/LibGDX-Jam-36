package no.sandramoen.libgdx35.actors.pinball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import no.sandramoen.libgdx35.utils.BaseActor;

public class Coin extends BaseActor {

    private Texture texture;

    public Coin(float x, float y, float width, float height, Stage stage) {
        super(x, y, stage);
        this.setBounds(x, y, width, height);
        this.texture = new Texture(Gdx.files.internal("images/included/coin.png"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
