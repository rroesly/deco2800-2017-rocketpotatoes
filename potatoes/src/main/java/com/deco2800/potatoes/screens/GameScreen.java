package com.deco2800.potatoes.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.deco2800.potatoes.RocketPotatoes;
import com.deco2800.potatoes.entities.*;
import com.deco2800.potatoes.gui.ChatGui;
import com.deco2800.potatoes.gui.GameMenuGui;
import com.deco2800.potatoes.handlers.MouseHandler;
import com.deco2800.potatoes.managers.*;
import com.deco2800.potatoes.observers.KeyDownObserver;
import com.deco2800.potatoes.observers.ScrollObserver;
import com.deco2800.potatoes.renderering.Render3D;
import com.deco2800.potatoes.renderering.Renderable;
import com.deco2800.potatoes.renderering.Renderer;
import com.deco2800.potatoes.worlds.InitialWorld;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Handles the creation of the world and rendering.
 *
 */
public class GameScreen implements Screen {

    // References to the 'game' object which handles our screens/
    private RocketPotatoes game;
    /**
     * Set the renderer.
     * 3D is for Isometric worlds
     * 2D is for Side Scrolling worlds
     * Check the documentation for each renderer to see how it handles WorldEntity coordinates
     */
    private Renderer renderer = new Render3D();

    // Managers tracked here for ease of use. Should be initialized from the GameManager.get().getManager(...) though!
    private SoundManager soundManager;
    private MouseHandler mouseHandler;
    private PlayerManager playerManager;
    private MultiplayerManager multiplayerManager;
    private GuiManager guiManager;
    private CameraManager cameraManager;

    private long lastGameTick = 0;
    private boolean playing = true;

    /**
     * Start's a multiplayer game
     * @param game game instance
     * @param name name to join with
     * @param IP IP to connect to, (ignored if isHost is true (will connect to 127.0.0.1))
     * @param port port to connect/host on
     * @param isHost is this client a host (i.e. start a server then connect to it)
     */
    public GameScreen(RocketPotatoes game, String name, String IP, int port, boolean isHost)
            throws IllegalStateException, IllegalArgumentException, IOException {
        this.game = game;
        setupGame();

        // setup multiplayer
        if (isHost) {
            multiplayerManager.createHost(port);
            multiplayerManager.joinGame(name, "127.0.0.1", port);
        } else {
            multiplayerManager.joinGame(name, IP, port);
        }

        initializeGame();
    }

    /**
     * Start's a singleplayer game
     * @param game game instance
     */
    public GameScreen(RocketPotatoes game) {
        this.game = game;
        setupGame();
        initializeGame();
    }

    /**
     * Sets up everything needed for the game, but doesn't initialize any game specific things just yet
     */
    private void setupGame() {
        this.game = game;

		/*
		 * Forces the GameManager to load the TextureManager, and load textures.
		 */
        GameManager.get().getManager(TextureManager.class);

        /**
         *	Setup managers etc.
         */

		/* Create a sound manager for the whole game */
        soundManager = (SoundManager) GameManager.get().getManager(SoundManager.class);

		/* Create a mouse handler for the game */
        mouseHandler = new MouseHandler();

		/* Create a multiplayer manager for the game */
        multiplayerManager = (MultiplayerManager)GameManager.get().getManager(MultiplayerManager.class);

		/* Create a player manager. */
        playerManager = (PlayerManager)GameManager.get().getManager(PlayerManager.class);

		/* Setup camera */
        cameraManager = (CameraManager)GameManager.get().getManager(CameraManager.class);
        cameraManager.setCamera(new OrthographicCamera(1920, 1080));

        /**
         * GuiManager, which contains all our Gui specific properties/logic. Creates our stage etc.
         */

        guiManager = (GuiManager)GameManager.get().getManager(GuiManager.class);
        guiManager.setStage(new Stage(new ScreenViewport()));

        // Deselect all gui elements if we click anywhere in the game world
        guiManager.getStage().getRoot().addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!(event.getTarget() instanceof TextField)) {
                    guiManager.getStage().setKeyboardFocus(null);
                }
                return false;
            }
        });

        // Make our GameMenuGui
        guiManager.addGui(new GameMenuGui(guiManager.getStage(), this));

        // Make our chat window
        guiManager.addGui(new ChatGui(guiManager.getStage()));

		/* Setup inputs */
        setupInputHandling();

        /* Create an example world for the engine */
        GameManager.get().setWorld(new InitialWorld());

		/* Move camera to center */
        cameraManager.getCamera().position.x = GameManager.get().getWorld().getWidth() * 32;
        cameraManager.getCamera().position.y = 0;
    }

    private void setupInputHandling() {

        /**
         * Setup inputs for the buttons and the game itself
         */
		/* Setup an Input Multiplexer so that input can be handled by both the UI and the game */
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(guiManager.getStage()); // Add the UI as a processor

        InputManager input = (InputManager) GameManager.get().getManager(InputManager.class);
        input.addKeyDownListener(new CameraHandler());
        input.addScrollListener(new ScrollTester());

        MouseHandler mouseHandler = new MouseHandler();
        input.addTouchDownListener(mouseHandler);
        input.addTouchDraggedListener(mouseHandler);
        inputMultiplexer.addProcessor(input);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * Initializes everything needed to actually play the game
     * Can be used to `reset` the state of the game
     *
     * TODO this logic should be state-machined'd (i.e. Main Menu <-> Playing <-> Paused. With every state having
     * TODO it's own menu(s), initialization etc. And when we setup custom transition logic.
     */
    private void initializeGame() {

        if (!multiplayerManager.isMultiplayer()) {
            guiManager.getGui(ChatGui.class).hide();
        }

        Random random = new Random();

        MultiplayerManager m = multiplayerManager;
        if (m.isMaster() || !m.isMultiplayer()) {
            for (int i = 0; i < 5; i++) {
                GameManager.get().getWorld().addEntity(new Squirrel(
                        10 + random.nextFloat() * 10, 10 + random.nextFloat() * 10, 0));
            }

            GameManager.get().getWorld().addEntity(new Peon(7, 7, 0));
            GameManager.get().getWorld().addEntity(new Tower(8, 8, 0));
            GameManager.get().getWorld().addEntity(new GoalPotate(15, 10, 0));
            
            SeedResource seedResource = new SeedResource();
			FoodResource foodResource = new FoodResource();
			
			GameManager.get().getWorld().addEntity(new ResourceEntity(18, 18, 0, seedResource));
			GameManager.get().getWorld().addEntity(new ResourceEntity(17, 18, 0, seedResource));
			GameManager.get().getWorld().addEntity(new ResourceEntity(17, 17, 0, seedResource));
			GameManager.get().getWorld().addEntity(new ResourceEntity(18, 17, 0, seedResource));
			
			GameManager.get().getWorld().addEntity(new ResourceEntity(0, 18, 0, foodResource));
			GameManager.get().getWorld().addEntity(new ResourceEntity(1, 18, 0, foodResource));
			GameManager.get().getWorld().addEntity(new ResourceEntity(0, 17, 0, foodResource));
			GameManager.get().getWorld().addEntity(new ResourceEntity(1, 17, 0, foodResource));
        }



        if (!multiplayerManager.isMultiplayer()) {
			/* TODO bug! currently reseting the game while having a key held down will then notify the new player with the keyUp
		   TODO event, which will result it in moving without pressing a key. This is something a bit difficult to fix as
		   TODO so I'm just going to leave it for now since fixing it is a bit of a hassle
		 	*/

            // Make our player
            playerManager.setPlayer(new Player(5, 10, 0));
            GameManager.get().getWorld().addEntity(playerManager.getPlayer());
        }
    }

    private void tickGame(long timeDelta) {
		/* broken!
		window.removeActor(peonButton);
		boolean somethingSelected = false;
		*/

        // Tick our player
        if (multiplayerManager.isMultiplayer() && !multiplayerManager.isMaster()) {
            playerManager.getPlayer().onTick(timeDelta);
        }

        // Tick other stuff maybe
        for (Renderable e : GameManager.get().getWorld().getEntities().values()) {
            if (e instanceof Tickable) {
                // Only tick elements if we're singleplayer or master
                if (!multiplayerManager.isMultiplayer() || multiplayerManager.isMaster()) {
                    ((Tickable) e).onTick(timeDelta);
                }
            }


			/* broken!
			if (e instanceof Selectable) {
				if (((Selectable) e).isSelected()) {
					peonButton = ((Selectable) e).getButton();
					somethingSelected = true;
				}
			}
			*/

        }

        // Tick Events
        ((EventManager) GameManager.get().getManager(EventManager.class)).tickAll(timeDelta);;

        // Broadcast updates if we're master TODO only when needed.
        if (multiplayerManager.isMultiplayer() && multiplayerManager.isMaster()) {
            for (Map.Entry<Integer, AbstractEntity> e : GameManager.get().getWorld().getEntities().entrySet()) {
                // But don't broadcast our player yet
                if (e.getKey() != multiplayerManager.getID()) {
                    multiplayerManager.broadcastEntityUpdatePosition(e.getKey());

                    // TODO only when needed Maybe attach to the HasProgress interface itself?
                    if (e.getValue() instanceof HasProgress) {
                        multiplayerManager.broadcastEntityUpdateProgress(e.getKey());
                    }
                }
            }
        }

        // Broadcast our player updating pos TODO only when needed.
        multiplayerManager.broadcastPlayerUpdatePosition();


		/* broken!
		if (!somethingSelected) {
			peonButton = uiPeonButton;
		}
		window.add(peonButton);
		*/

        // Tick CameraManager, maybe want to make managers tickable??
        cameraManager.centerOnTarget(timeDelta);
    }

    private void renderGUI(SpriteBatch batch) {

        // Update window title
        Gdx.graphics.setTitle("DECO2800 " + this.getClass().getCanonicalName() +  " - FPS: "+ Gdx.graphics.getFramesPerSecond());

        // Render GUI elements
        guiManager.getStage().act();
        guiManager.getStage().draw();
    }

    private void renderGame(SpriteBatch batch) {

        /* Render the tiles first */
        BatchTiledMapRenderer tileRenderer = renderer.getTileRenderer(batch);
        tileRenderer.setView(cameraManager.getCamera());
        tileRenderer.render();

        // Render entities etc.
        renderer.render(batch);

        //TODO: add render for projectile's separately
    }

    /**
     * Renderer thread
     * Must update all displayed elements using a Renderer
     */
    @Override
    public void render(float delta) {
        /**
         * We only tick/render the game if we're actually playing. Lets us seperate main menu and such from the game
         * TODO We may lose/gain a tick or part of a tick when we pause/unpause?
         */
		/*
		 * Tickrate = 100Hz
		 */
        if (playing) {
            // Stop the first tick lasting years
            if (lastGameTick != 0) {
                long timeDelta = TimeUtils.millis() - lastGameTick;
                if (timeDelta > 10) {

                    // Tick game, a bit a weird place to have it though.
                    tickGame(timeDelta);
                    lastGameTick = TimeUtils.millis();
                }
            } else {
                lastGameTick = TimeUtils.millis();
            }
        }

        /*
         * Create a new render batch.
         * At this stage we only want one but perhaps we need more for HUDs etc
         */
        SpriteBatch batch = new SpriteBatch();

		        /*
         * Update the input handlers
         */
        //handleInput();

        /*
         * Update the camera
         */
        cameraManager.getCamera().update();
        batch.setProjectionMatrix(cameraManager.getCamera().combined);

        /*
         * Clear the entire display as we are using lazy rendering
         */
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // TODO way to render game so it appears paused (could just be a flag)
        if (playing) {
            renderGame(batch);
        }

        renderGUI(batch);

        batch.dispose();
    }


    /**
     * Resizes the viewport
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        cameraManager.getCamera().viewportWidth = width;
        cameraManager.getCamera().viewportHeight = height;
        cameraManager.getCamera().update();

        // Tell the gui manager to resize appropriately
        guiManager.resize(width, height);
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
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    @Override
    public void show() {

    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {

    }

    /**
     * Disposes of assets etc when the rendering system is stopped.
     */
    @Override
    public void dispose () {
        // Don't need this at the moment
    }

    public void exitToMenu() {
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }


    private class CameraHandler implements KeyDownObserver {

        @Override
        public void notifyKeyDown(int keycode) {
            OrthographicCamera c = cameraManager.getCamera();
            int speed = 10;

            if (keycode == Input.Keys.EQUALS) {
                if (c.zoom > 0.1) {
                    c.zoom -= 0.1;
                }
            } else if (keycode == Input.Keys.MINUS) {
                c.zoom += 0.1;

            } else if (keycode == Input.Keys.UP) {
                c.translate(0, 1 * speed * c.zoom, 0);
            } else if (keycode == Input.Keys.DOWN) {
                c.translate(0, -1 * speed * c.zoom, 0);
            } else if (keycode == Input.Keys.LEFT) {
                c.translate(-1 * speed * c.zoom, 0, 0);
            } else if (keycode == Input.Keys.RIGHT) {
                c.translate(1 * speed * c.zoom, 0, 0);
            }
        }
    }


    private class ScrollTester implements ScrollObserver {

        @Override
        public void notifyScrolled(int amount) {
            //System.out.println(amount);
        }

    }


}