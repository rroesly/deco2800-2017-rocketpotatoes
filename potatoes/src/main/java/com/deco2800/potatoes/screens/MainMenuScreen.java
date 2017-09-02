package com.deco2800.potatoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.deco2800.potatoes.RocketPotatoes;
import com.deco2800.potatoes.gui.MainMenuGui;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.SoundManager;
import com.deco2800.potatoes.managers.TextureManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Main menu screen implemetation. Handles the logic/display for the main menu, and other adjacent menus (e.g. options).
 * Also holds the logic for starting a game, (e.g. singleplayer, multiplayer, loaded, etc.)
 *
 * TODO make this nicer (i.e. use dispose) Probably has tiny memory leaks
 */
public class MainMenuScreen implements Screen {
    private RocketPotatoes game;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuScreen.class);

    private SpriteBatch batch;
    private Stage stage;

    private MainMenuGui mainMenuGui;
    private OrthographicCamera camera;
    private TextureManager textureManager;


    public MainMenuScreen(RocketPotatoes game) {
        this.game = game;
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        // TODO config?
        camera.setToOrtho(false, 1920, 1080);
        // game screen background

        textureManager = GameManager.get().getManager(TextureManager.class);

        stage = new Stage(new ScreenViewport());

        setupGui();

        // Setup input handling
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * Creates GUI.
     */
    private void setupGui() {
        mainMenuGui = new MainMenuGui(stage, this);
    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.graphics.setTitle("DECO2800 " + this.getClass().getCanonicalName() +  " - FPS: " +
                Gdx.graphics.getFramesPerSecond());

        // Draw/update gui
        stage.act();
        stage.getBatch().begin();
        stage.getBatch().draw(textureManager.getTexture("screen_background"), 0, 0, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());


        stage.getBatch().end();
        
        stage.draw();



    }

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        stage.getViewport().update(width, height, true);

        mainMenuGui.resize(stage);
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    @Override
    public void show() {


    }


    /**
     * @see ApplicationListener#pause()
     */
    @Override
    public void pause() {

    }

    /**
     * @see ApplicationListener#resume()
     */
    @Override
    public void resume() {

    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {

    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {

    }

    /**
     * Start a singleplayer game.
     */
    public void startSinglePlayer() {
        game.setScreen(new GameScreen(game));
    }

    /**
     * Start / join a multiplayer game.
     * @param name name of player
     * @param ip IP address to connect to
     * @param port Port to connect to
     * @param isHost whether starting multiplayer as host
     */
    public void startMultiplayer(String name, String ip, int port, boolean isHost) {
        try {
            game.setScreen(new GameScreen(game, name, ip, port, isHost));
        }
        catch (Exception ex) {
            // TODO handle a failed connection.
            LOGGER.warn("Failed to get connect to host.", ex);
            System.exit(-1);
        }
    }

    /**
     * Gets the string of the host's LocalHost IP address.
     */
    public static String multiplayerHostAddress() {
        // TODO: Get Actual IP Addresses, not just the local host
        try {
            InetAddress ip = InetAddress.getLoopbackAddress();
            return ip.getHostAddress();
        } catch (Exception ex) {
            LOGGER.warn("Failed to get host IP address.", ex);
        }
        return "Couldn't find IP address";

    }

    /**
     * Sets the sound effects volume (v) in SoundManager. (from 0 to 1)
     * @param v
     */
    public void setEffectsVolume(float v){
        GameManager.get().getManager(SoundManager.class).setEffectsVolume(v);
    }

    /**
     * Returns the current sound effects volume from SoundManager.
     * @return float from 0 to 1.
     */
    public float getEffectsVolume(){
        return GameManager.get().getManager(SoundManager.class).getEffectsVolume();
    }

    /**
     * Sets the music volume (v) in SoundManager. (from 0 to 1)
     * @param v
     */
    public void setMusicVolume(float v){
        GameManager.get().getManager(SoundManager.class).setMusicVolume(v);
    }

    /**
     * Returns the current music volume from SoundManager.
     * @return float from 0 to 1.
     */
    public float getMusicVolume(){
        return GameManager.get().getManager(SoundManager.class).getMusicVolume();
    }

    /**
     * Plays a blip sound.
     */
    public void menuBlipSound(){
        GameManager.get().getManager(SoundManager.class).playSound("menu_blip.wav");
    }

}
