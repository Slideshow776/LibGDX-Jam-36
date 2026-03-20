package no.sandramoen.libgdx35.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class BaseScreen implements Screen, InputProcessor {

    protected Stage mainStage;
    protected Stage uiStage;
    protected Table uiTable;
    protected ShapeRenderer shape_renderer;
    private boolean pause;
    protected FitViewport viewport;
    protected OrthographicCamera camera;

    public BaseScreen() {
        viewport = new FitViewport(960, 960);
        camera = new OrthographicCamera();
        viewport.setCamera(camera);
        mainStage = new Stage(viewport);
        camera.zoom = 1.3f;

        uiTable = new Table();
        uiTable.setFillParent(true);

        uiStage = new Stage(new ScreenViewport());
        uiStage.addActor(uiTable);

        shape_renderer = new ShapeRenderer();

        initialize();
    }

    public abstract void initialize();

    public abstract void update(float delta);

    @Override
    public void render(float delta) {
        if (!pause) {
            mainStage.act(delta);
            update(delta);
        }

        ScreenUtils.clear(Color.BLACK);

        mainStage.getViewport().apply();
        mainStage.draw();

        uiStage.act(delta);
        uiStage.getViewport().apply();
        uiStage.draw();
    }

    @Override
    public void show() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(this);
        im.addProcessor(uiStage);
        im.addProcessor(mainStage);
    }

    @Override
    public void hide() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        im.removeProcessor(uiStage);
        im.removeProcessor(mainStage);
    }

    @Override
    public void resize(int width, int height) {
        mainStage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        pause = true;
    }

    @Override
    public void resume() {
        pause = false;
    }

    @Override
    public void dispose() {
        shape_renderer.dispose();
        mainStage.dispose();
        uiStage.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom = MathUtils.clamp(camera.zoom + amountY * 0.05f, 0.5f, 3f);
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
