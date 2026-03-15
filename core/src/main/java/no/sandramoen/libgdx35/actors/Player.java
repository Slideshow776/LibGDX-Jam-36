package no.sandramoen.libgdx35.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import no.sandramoen.libgdx35.utils.BaseActor;
import no.sandramoen.libgdx35.utils.BaseGame;


public class Player extends BaseActor {

    public Player(float x, float y, Stage s) {
        super(x, y, s);

    }

    @Override
    public void act(float delta) {
        super.act(delta);

    }


    public boolean isMoving() {
        return getSpeed() > 0.1f; // small threshold to avoid tiny jitter counts as moving
    }


    public void kill() {
        setColor(Color.BLACK);

    }

}
