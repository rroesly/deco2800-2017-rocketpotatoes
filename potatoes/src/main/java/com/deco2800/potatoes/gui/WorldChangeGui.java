package com.deco2800.potatoes.gui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.deco2800.potatoes.entities.*;
import com.deco2800.potatoes.managers.*;
import com.deco2800.potatoes.screens.GameScreen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.deco2800.potatoes.worlds.WorldType;

public class WorldChangeGui extends Gui {
    // logger for error/warning/info messages
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldChangeGui.class);

    private GameScreen screen;
    private Stage stage;
    private Skin uiSkin;
    private VerticalGroup worldsButtonGroup;

    // buttons
    private TextButton worldOneButton;
    private TextButton worldTwoButton;
    private TextButton worldThreeButton;
    private TextButton worldFourButton;
    private TextButton exitButton;

    // table to hold buttons
    private Table table;

    // create a player manager
    private PlayerManager playerManager = GameManager.get().getManager(PlayerManager.class);

    /**
     * Creates an instance of the World Change Gui
     *
     * @param stage
     * @param screen
     */
    public WorldChangeGui(Stage stage, GameScreen screen){

        this.screen = screen;
        this.stage = stage;
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        table = new Table(uiSkin);

        // actors initialisation
        worldOneButton = new TextButton("Desert World", uiSkin);
        worldTwoButton = new TextButton("Ice World", uiSkin);
        worldThreeButton = new TextButton("Volcano World", uiSkin);
        worldFourButton = new TextButton("Sea World", uiSkin);
        exitButton = new TextButton("Exit", uiSkin);

        // adding actors
        worldsButtonGroup = new VerticalGroup();
        worldsButtonGroup.addActor(worldOneButton);
        worldsButtonGroup.addActor(worldTwoButton);
        worldsButtonGroup.addActor(worldThreeButton);
        worldsButtonGroup.addActor(worldFourButton);
        worldsButtonGroup.addActor(exitButton);
        table.add(worldsButtonGroup);

        // create listeners for each button
        setupListeners();

        // set position of the gui
        resetGui(stage);
        // set the gui to initially be hidden
        table.setVisible(false);
        // add the table/buttons to the stage
        stage.addActor(table);
    }

    /**
     * Sets the position of the gui.
     *
     * @param stage
     * 			The game screen
     */
    private void resetGui(Stage stage) {
        table.reset();
        table.center();
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());
        table.setPosition(0, 0);
        table.add(worldsButtonGroup);
    }

    /**
     * Creates listeners for each of the buttons.
     */
    private void setupListeners() {

        // Listener for the exit button.
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // hide the gui
                hide();

                // the current player
                Player player = playerManager.getPlayer();
                // straight offset, can be done better
                player.setPosition(player.getPosX() - 3, player.getPosY() - 3, 0);
                // add player back into the world
                GameManager.get().getWorld().addEntity(player);
            }

        });

        // Listener to change to world 4
        worldFourButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //change to world 4
                changeWorld(WorldType.OCEAN_WORLD);
            }

        });

        // Listener to change to world 3
        worldThreeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //change to world 3
                changeWorld(WorldType.VOLCANO_WORLD);
            }

        });


        // Listener to change to world 2
        worldTwoButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //change to world 2
                changeWorld(WorldType.DESERT_WORLD);
            }

        });

        // Listener to change to world 1
        worldOneButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //change to world 1
                changeWorld(WorldType.ICE_WORLD);
            }

        });

    }

    /**
     * Teleports the player to a different world.
     *
     * @param world
     * 			The key of the world to change to
     */
    private void changeWorld(WorldType world) {
        // change to new world
        GameManager.get().getManager(WorldManager.class).setWorld(world);
        // add player to new world
        GameManager.get().getWorld().addEntity(playerManager.getPlayer());
        // set player to be next to the portal
        playerManager.getPlayer().setPosition(9, 4, 0);

        // hide the world change gui
        hide();
    }

    @Override
    public void show() {
        table.setVisible(true);
        stage.addActor(table);
    }

    @Override
    public void hide() {
        table.setVisible(false);
    }
}
