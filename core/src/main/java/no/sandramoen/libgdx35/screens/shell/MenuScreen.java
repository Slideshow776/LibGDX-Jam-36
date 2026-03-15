package no.sandramoen.libgdx35.screens.shell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import no.sandramoen.libgdx35.actors.Background;
import no.sandramoen.libgdx35.actors.Overlay;
import no.sandramoen.libgdx35.screens.gameplay.LevelScreen;
import no.sandramoen.libgdx35.utils.BaseActor;
import no.sandramoen.libgdx35.utils.BaseGame;
import no.sandramoen.libgdx35.utils.BaseScreen;


public class MenuScreen extends BaseScreen {

    private Background background;
    private BaseActor overlay;

    @Override
    public void initialize() {}


    @Override
    public void update(float delta) {}


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE) {
            Gdx.app.exit();
        } else if (keycode == Keys.E)
            start(BaseGame.Difficulty.EASY);
        else if (keycode == Keys.M)
            start(BaseGame.Difficulty.MEDIUM);
        else if (keycode == Keys.H)
            start(BaseGame.Difficulty.HARD);
        return super.keyDown(keycode);
    }

    private void start(BaseGame.Difficulty difficulty) {
        BaseGame.current_difficulty = difficulty;

        overlay.addAction(Actions.sequence(
            Actions.alpha(1.0f, Overlay.DURATION),
            Actions.run(() -> BaseGame.setActiveScreen(new LevelScreen()))
        ));
    }
}
