package com.deco2800.potatoes.entities.player;

import com.badlogic.gdx.Input;
import com.deco2800.potatoes.collisions.Circle2D;
import com.deco2800.potatoes.entities.AbstractEntity;
import com.deco2800.potatoes.entities.Direction;
import com.deco2800.potatoes.entities.Tickable;
import com.deco2800.potatoes.entities.animation.TimeAnimation;
import com.deco2800.potatoes.entities.animation.TimeTriggerAnimation;
import com.deco2800.potatoes.entities.health.*;
import com.deco2800.potatoes.entities.resources.*;
import com.deco2800.potatoes.entities.trees.*;
import com.deco2800.potatoes.gui.PauseMenuGui;
import com.deco2800.potatoes.gui.RespawnGui;
import com.deco2800.potatoes.gui.TreeShopGui;
import com.deco2800.potatoes.managers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * Entity for the playable character.
 * <p>
 * @author leggy, petercondoleon
 * <p>
 */
public class Player extends MortalEntity implements Tickable, HasProgressBar {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Player.class);
    private static final transient float HEALTH = 200f;
    private static final ProgressBarEntity PROGRESS_BAR = new ProgressBarEntity("healthbar", 4);


    protected int respawnTime = 5000; 	// Time until respawn in milliseconds
    private Inventory inventory;


    protected TimeAnimation currentAnimation;	// The current animation of the player
    protected PlayerState state;    	// The current states of the player, set to idle by default


    private boolean keyW = false;
    private boolean keyA = false;
    private boolean keyS = false;
    private boolean keyD = false;



    //TODO change this. -> super. in as many locations as possible

    // ----------     PlayerState class     ---------- //

    /* The states a player may take */
    public enum PlayerState { IDLE, WALK, ATTACK, DAMAGED, DEATH, INTERACT;
    		@Override
    		public String toString() {
    			return super.toString().toLowerCase();
    		}
    };

    // make usage of PlayerState less verbose for use in this class and subclasses
    static final PlayerState IDLE = PlayerState.IDLE;
    static final PlayerState WALK = PlayerState.WALK;
    static final PlayerState ATTACK = PlayerState.ATTACK;
    static final PlayerState DAMAGED = PlayerState.DAMAGED;
    static final PlayerState DEATH = PlayerState.DEATH;
    static final PlayerState INTERACT = PlayerState.INTERACT;




    // ----------     Initialisation     ---------- //

    /**
     * Default constructor for the purposes of serialization
     */
    public Player() {
        this(0, 0);
    }

    /**
     * Creates a new Player instance.
     *
     * @param posX The x-coordinate.
     * @param posY The y-coordinate.
     */
    public Player(float posX, float posY) {
        super(new Circle2D(posX, posY, 0.4f), 1f, 1f, "player_right", HEALTH);
        this.facing = Direction.SE;
        this.state = IDLE;
        this.setMoveSpeedModifier(0);
        this.setStatic(false);
        this.setSolid(true);
        addResources();	//Initialise the inventory with the valid resources
    }




    // ----------     Texture / Animation     ---------- //

    /**
     * Creates a map of player directions with player state animations. Uses
     * direction as a key to receive the respective animation.
     *
     * @param playerType
     * 			A string representing the type of player.
     * @param state
     * 			The state of the player.
     * @param frameCount
     * 			The number of frames in the animation.
     * @param animationTime
     * 			The time per animation cycle.
     * @return
     * 		A map of directions with animations for the specified state.
     */
    public static Map<Direction, TimeAnimation> makePlayerAnimation(String playerType,
            PlayerState state, int frameCount, int animationTime, Supplier<Void> completionHandler) {
        Map<Direction, TimeAnimation> animations = new HashMap<>();
        for (Direction direction : Direction.values()) {
            String[] frames = new String[frameCount];
            for (int i=1; i<=frameCount; i++) {
                frames[i-1] = playerType + "_" + state.toString() + "_" + direction.name() + "_" + i;
            }
            animations.put(direction, new TimeTriggerAnimation(animationTime, frames, completionHandler));
        }

        return animations;
    }

    /**
     * Sets the specified animation to be the player's current animation.
     *
     * @param animation
     * 			The time animation to be set to the player.
     */
    public void setAnimation(TimeAnimation animation) {

        EventManager em = GameManager.get().getManager(EventManager.class);

        em.unregisterEvent(this, this.currentAnimation);
        currentAnimation = animation;
        em.registerEvent(this, currentAnimation);

        LOGGER.info("Changed animation to " + facing);
    }

    @Override
    public String getTexture() {
        if (currentAnimation != null) {
            return currentAnimation.getFrame();
        } else {
            LOGGER.warn("Rendered player without texture.");
            return "";
        }
    }

    /**
     * A method for damaging the player's health. Allows the damaged
     * state to be enabled and respective animations to play.
     *
     * @param amount
     * 			The amount of damage to deal to the player.
     */
    @Override
    public boolean damage(float amount) {
        if (state != DAMAGED) {
            setState(DAMAGED);
            this.updateSprites();
        }
        return super.damage(amount);
    }

    @Override
    public ProgressBar getProgressBar() {
        return PROGRESS_BAR;
    }




    // ----------     Input handling / Movement setup     ---------- //


    /**
     * Set the player's state. For example, if the player is walking, then
     * set the 'walk' state to the player. The state can only be changed when
     * the player is in idle or is walking. The reason for this is to prevent
     * situations where the player tries to attack while being hurt.
     *
     * @param newState
     * 			The state to set.
     * @return true
     * 			if the state was successfully set. False otherwise.
     */
    public boolean setState(PlayerState newState) {

        //check already in state
        if (state == newState)
            return true;

        // only change state if IDLE or WALK-ing
        if (state == IDLE || state == WALK) {
            state = newState;

            // only move on WALK
            setMoveSpeedModifier( (state == WALK) ? 1 : 0);

            updateSprites();
            return true;
        } else {
            // state not changed
            return false;
        }
    }

    /**
     * Returns the current state of the player.
     *
     * @return
     * 		The current state of the player.
     */
    public PlayerState getState() {
        return this.state;
    }

    /**
     * Handle movement when keyboard keys are pressed down
     *
     * @param keycode
     * 			The key pressed
     */
    public void handleKeyDown(int keycode) {

        switch (keycode) {
            case Input.Keys.W:
                keyW = true;
                updateMovingAndFacing();
                break;
            case Input.Keys.S:
                keyS = true;
                updateMovingAndFacing();
                break;
            case Input.Keys.A:
                keyA = true;
                updateMovingAndFacing();
                break;
            case Input.Keys.D:
                keyD = true;
                updateMovingAndFacing();
                break;
            case Input.Keys.T:
                tossItem(new SeedResource());
                break;
            case Input.Keys.F:
                tossItem(new FoodResource());
                break;
            case Input.Keys.E:
                interact();
                harvestResources();
                break;
            case Input.Keys.SPACE:
                attack();
                break;
            default:
                break;
        }
    }

    /**
     * Handle movement when keyboard keys are released
     *
     * @param keycode
     * 			The key that was released
     */
    public void handleKeyUp(int keycode) {

        switch (keycode) {
            case Input.Keys.W:
                keyW = false;
                updateMovingAndFacing();
                break;
            case Input.Keys.S:
                keyS = false;
                updateMovingAndFacing();
                break;
            case Input.Keys.A:
                keyA = false;
                updateMovingAndFacing();
                break;
            case Input.Keys.D:
                keyD = false;
                updateMovingAndFacing();
                break;
            default:
                break;
        }
    }

    /**
     * Sets the direction of the player based on a current WASD keys pressed.
     */
    void updateMovingAndFacing() {
        Direction newFacing = null;

        //TODO releasing keys while travelling diagonal, not working, returning to cardinal directions

        // get direction based on current keys
        // considers if opposite keys are pressed

        int direcEnum = 4;   // default not moving

        // vertical keys
        if (keyW && !keyS) {
            direcEnum-=3;
        } else if (!keyW && keyS) {
            direcEnum+=3;
        }

        // at this point direcEnum = 1 or 4 or 7, North or Middle or South

        // horizontal keys
        if (!keyA && keyD) {
            direcEnum++;
        } else if (keyA && !keyD) {
            direcEnum--;
        }

        // get direction based on enumeration
        switch (direcEnum) {
            case 0:
                newFacing = Direction.NW;
                break;
            case 1:
                newFacing = Direction.N;
                break;
            case 2:
                newFacing = Direction.NE;
                break;
            case 3:
                newFacing = Direction.W;
                break;
            //case 4           not moving
            case 5:
                newFacing = Direction.E;
                break;
            case 6:
                newFacing = Direction.SW;
                break;
            case 7:
                newFacing = Direction.S;
                break;
            default:        //(case 8)
                newFacing = Direction.SE;
                break;
        }

        if (direcEnum == 4) {
            setState(IDLE);
            super.setMoveSpeedModifier(0);
        } else {
            setState(WALK);
            super.setMoveAngle(newFacing.getAngleRad());
            super.setMoveSpeedModifier(1);
            facing = newFacing;
        }

        updateSprites();
    }





    // ----------     OnTick     ---------- //

    @Override
    public void onTick(long arg0) {


        //Get terrainModifier of the current tile
        float myX = super.getPosX();
        float myY = super.getPosY();
        float length = GameManager.get().getWorld().getLength();
        float width = GameManager.get().getWorld().getWidth();

        float terrainModifier = GameManager.get().getWorld()
                .getTerrain(Math.round(Math.min(myX, width - 1)), Math.round(Math.min(myY, length - 1)))
                .getMoveScale();

        //TODO getting terrainModifier should be easier as multiple entities will use it
        //TODO is not using terrainModifier

        //super.moveSpeedModifier = terrainModifier;
        super.onTickMovement();



    }




    // ----------     Inventory Management     ---------- //

    /**
     * Initialises the inventory with all the resources in the game.
     */
    private void addResources() {

    	HashSet<Resource> startingResources = new HashSet<Resource>();
    	startingResources.add(new SeedResource());
        this.inventory = new Inventory(startingResources);
    }

    /**
     * Returns the player inventory.
     *
     * Returns the inventory specific to the player.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Handles removing an item from an inventory and placing it on the map.
     *
     * @param item The resource to be thrown.
     */
    private void tossItem(Resource item) {
        // Tosses a item in front of player
        float x = this.getPosX();
        float y = this.getPosY();

        x = facing == Direction.SW ? x - 1 : x + 1;
        y = facing == Direction.SW ? y - 2 : y + 2;

        // Only toss an item if there are items to toss
        if (this.getInventory().updateQuantity(item, -1) == 1) {
            GameManager.get().getWorld().addEntity(new ResourceEntity(x, y, item));
        }
    }

    /**
     * Returns true if the user can buy this tree
     */
    public boolean canAfford(AbstractTree tree){
        if (tree == null || inventory == null) {
            return false;
        }

        try {
            GameManager.get().getManager
                    (GuiManager.class).getGui(TreeShopGui.class).getTreeStateByTree(tree);
        } catch (Exception e) {
            return false;
        }


        TreeState treeState = GameManager.get().getManager
                (GuiManager.class).getGui(TreeShopGui.class).getTreeStateByTree(tree);
        if (treeState == null) {
            return false;
        }

        Inventory cost = treeState.getCost();
        for (Resource resource : cost.getInventoryResources()) {
            if (inventory.getQuantity(resource) < cost.getQuantity(resource)){
                return false;
            }

        }

        return true;
    }

    /**
     * Handles harvesting resources from resource tree that are in range. Resources
     * are added to the player's inventory.
     */
    private void harvestResources() {
        double interactRange = 3f; // TODO: Could this be a class variable?
        Collection<AbstractEntity> entities = GameManager.get().getWorld().getEntities().values();
        boolean didHarvest = false;
        for (AbstractEntity entitiy : entities) {
            if (entitiy instanceof ResourceTree && entitiy.distanceTo(this) <= interactRange
                    && ((ResourceTree) entitiy).getGatherCount() > 0) {
                didHarvest = true;
                ((ResourceTree) entitiy).transferResources(this.inventory);
            }
        }
        if (didHarvest) {
            GameManager.get().getManager(SoundManager.class).playSound("harvesting.mp3");
        }
    }




    // ----------     Death     ---------- //

    @Override
    public void deathHandler() {
        LOGGER.info(this + " is dead.");
        // destroy the player
        GameManager.get().getWorld().removeEntity(this);
        // play Wilhelm scream sound effect TODO Probably find something better for this...if you can ;)
        SoundManager soundManager = new SoundManager();
        soundManager.playSound("death.wav");
        // get the event manager
        EventManager eventManager = GameManager.get().getManager(EventManager.class);
        // add the respawn event
        eventManager.registerEvent(this, new RespawnEvent(respawnTime));

        GameManager.get().getManager(GuiManager.class).getGui(RespawnGui.class).show();
    }




    // ----------     Abstract Methods     ---------- //

    /**
     * A method for making the player attack based on the direction it
     * faces. Allows the attack state to be enabled and respective
     * animations to play.
     */
    protected void attack() {
        // Override in subclasses to allow attacking.
    }

    /**
     * A method for making the player interact based on the direction it
     * faces. Allows the interact state to be enabled and respective
     * animations to play.
     */
    protected void interact() {
        // Override in subclasses to allow interacting.
    }

    /**
     * A method allowing subclasses to handle the player entering and
     * exiting the walk state. The method is automatically called every time
     * this transition occurs.
     *
     * @param active
     * 			True if the player starts walking and false
     * 			if the player stops walking.
     */
    protected void walk(boolean active) {
        // Override in subclasses to allow handling of walking.
    }

    /**
     * Updates the player sprite based on it's state and direction. Must
     * handle setting the animations for every player state.
     */
    public void updateSprites() {
        // Override in subclasses to update the sprite based on state and direciton.
    }




    // ----------     Generic Object Methods    ---------- //

    @Override
    public String toString() {
        return "The player";
    }

}
