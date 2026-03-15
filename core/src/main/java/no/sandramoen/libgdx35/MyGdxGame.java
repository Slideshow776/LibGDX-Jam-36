package no.sandramoen.libgdx35;

import no.sandramoen.libgdx35.screens.gameplay.LevelScreen;
import no.sandramoen.libgdx35.utils.BaseGame;

public class MyGdxGame extends BaseGame {

    @Override
    public void create() {
        super.create();
        levelScreen = new LevelScreen();
        setActiveScreen(levelScreen);
    }

    @Override
    public void render() {
        super.render();
    }
}
