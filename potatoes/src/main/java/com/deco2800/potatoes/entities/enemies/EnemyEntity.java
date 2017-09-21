package com.deco2800.potatoes.entities.enemies;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.deco2800.potatoes.entities.*;
import com.deco2800.potatoes.entities.effects.StompedGroundEffect;
import com.deco2800.potatoes.entities.health.HasProgressBar;
import com.deco2800.potatoes.entities.health.MortalEntity;
import com.deco2800.potatoes.entities.health.ProgressBarEntity;
import com.deco2800.potatoes.entities.health.RespawnEvent;

import com.deco2800.potatoes.managers.*;
import com.deco2800.potatoes.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.deco2800.potatoes.entities.effects.Effect;
import com.deco2800.potatoes.entities.projectiles.Projectile;

import com.deco2800.potatoes.util.Box3D;
import com.deco2800.potatoes.util.WorldUtil;

public abstract class EnemyEntity extends MortalEntity implements HasProgressBar, Tickable {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Player.class);

	private float speed;
	private Path path;
	private Box3D target = null;
	private Class<?> goal;
	
	private int respawnTime = 15000; // milliseconds

	private static final SoundManager enemySoundManager = new SoundManager();

	private static final List<Color> COLOURS = Arrays.asList(Color.RED);
	private static final ProgressBarEntity PROGRESS_BAR = new ProgressBarEntity("progress_bar", COLOURS, 0, 1);

	/**
	 * Default constructor for serialization
	 */
	public EnemyEntity() {
		// empty for serialization
		getBasicStats().registerEvents(this);
	}

	/**
	 * Constructs a new AbstractEntity. The entity will be rendered at the same size
	 * used for collision between entities.
	 * 
	 * @param posX
	 *            The x-coordinate of the entity.
	 * @param posY
	 *            The y-coordinate of the entity.
	 * @param posZ
	 *            The z-coordinate of the entity.
	 * @param xLength
	 *            The length of the entity, in x. Used in rendering and collision
	 *            detection.
	 * @param yLength
	 *            The length of the entity, in y. Used in rendering and collision
	 *            detection.
	 * @param zLength
	 *            The length of the entity, in z. Used in rendering and collision
	 *            detection.
	 * @param texture
	 *            The id of the texture for this entity.
	 * @param maxHealth
	 *            The initial maximum health of the enemy
	 * @param speed
	 * 			  The speed of the enemy
	 * @param goal
	 * 			  The attacking goal of the enemy
	 */
	public EnemyEntity(float posX, float posY, float posZ, float xLength, float yLength, float zLength,
			String texture, float maxHealth, float speed, Class<?> goal) {
		super(posX, posY, posZ, xLength, yLength, zLength, xLength, yLength, false, texture, maxHealth);
		getBasicStats().registerEvents(this);
		this.speed = speed;
		this.goal = goal;
	}

	/**
	 * Constructs a new AbstractEntity with specific render lengths. Allows
	 * specification of rendering dimensions different to those used for collision.
	 * For example, could be used to have collision on the trunk of a tree but not
	 * the leaves/branches.
	 * 
	 * @param posX
	 *            The x-coordinate of the entity.
	 * @param posY
	 *            The y-coordinate of the entity.
	 * @param posZ
	 *            The z-coordinate of the entity.
	 * @param xLength
	 *            The length of the entity, in x. Used in collision detection.
	 * @param yLength
	 *            The length of the entity, in y. Used in collision detection.
	 * @param zLength
	 *            The length of the entity, in z. Used in collision detection.
	 * @param xRenderLength
	 *            The length of the entity, in x. Used in collision detection.
	 * @param yRenderLength
	 *            The length of the entity, in y. Used in collision detection.
	 * @param texture
	 *            The id of the texture for this entity.
	 * @param maxHealth
	 *            The initial maximum health of the enemy
	 * @param speed
	 * 			  The speed of the enemy
	 * @param goal
	 * 			  The attacking goal of the enemy
	 */
	public EnemyEntity(float posX, float posY, float posZ, float xLength, float yLength, float zLength,
			float xRenderLength, float yRenderLength, String texture, float maxHealth, float speed, Class<?> goal) {
		super(posX, posY, posZ, xLength, yLength, zLength, xRenderLength, yRenderLength, texture, maxHealth);
		getBasicStats().registerEvents(this);
		this.speed = speed;
		this.goal = goal;
	}

	/**
	 * Constructs a new AbstractEntity with specific render lengths. Allows
	 * specification of rendering dimensions different to those used for collision.
	 * For example, could be used to have collision on the trunk of a tree but not
	 * the leaves/branches. Allows rendering of entities to be centered on their
	 * coordinates if centered is true.
	 * 
	 * @param posX
	 *            The x-coordinate of the entity.
	 * @param posY
	 *            The y-coordinate of the entity.
	 * @param posZ
	 *            The z-coordinate of the entity.
	 * @param xLength
	 *            The length of the entity, in x. Used in collision detection.
	 * @param yLength
	 *            The length of the entity, in y. Used in collision detection.
	 * @param zLength
	 *            The length of the entity, in z. Used in collision detection.
	 * @param xRenderLength
	 *            The length of the entity, in x. Used in collision detection.
	 * @param yRenderLength
	 *            The length of the entity, in y. Used in collision detection.
	 * @param centered
	 *            True if the entity is to be rendered centered, false otherwise.
	 * @param texture
	 *            The id of the texture for this entity.
	 * @param maxHealth
	 *            The initial maximum health of the enemy
	 * @param speed
	 * 			  The speed of the enemy
	 * @param goal
	 * 			  The attacking goal of the enemy         
	 *   
	 */
	public EnemyEntity(float posX, float posY, float posZ, float xLength, float yLength, float zLength,
			float xRenderLength, float yRenderLength, boolean centered, String texture, float maxHealth, float speed, Class<?> goal) {
		super(posX, posY, posZ, xLength, yLength, zLength, xRenderLength, yRenderLength, centered, texture, maxHealth);
		getBasicStats().registerEvents(this);
		this.speed = speed;
		this.goal = goal;
	}

	/**
	 * Move the enemy to its target. If the goal is player, use playerManager to get targeted player position for target, 
	 * otherwise get the closest targeted entity position.
	 */
	@Override
	public void onTick(long i) {
		float goalX;
		float goalY;
		//if goal is player, use playerManager to eet position and move towards target 
		if (goal == Player.class) {
			//goal = Player.class;
			PlayerManager playerManager = GameManager.get().getManager(PlayerManager.class);
			PathManager pathManager = GameManager.get().getManager(PathManager.class);

			// check that we actually have a path
			if (path == null || path.isEmpty()) {
				path = pathManager.generatePath(this.getBox3D(), playerManager.getPlayer().getBox3D());
			}


			//check if close enough to target
			if (target != null && target.overlaps(this.getBox3D())) {
				target = null;
			}

			//check if the path has another node
			if (target == null && !path.isEmpty()) {
				target = path.pop();
			}




			if (target == null) {
				target = playerManager.getPlayer().getBox3D();
			}

			goalX = target.getX();
			goalY = target.getY();

		} else {
			// set the target of Enemy to the closest goal
			Optional<AbstractEntity> target = WorldUtil.getClosestEntityOfClass(goal, getPosX(), getPosY());
			
			//if target is not found in the world, set target to player 
			if (!target.isPresent()) {
				PlayerManager playerManager = GameManager.get().getManager(PlayerManager.class);
				AbstractEntity getTarget = playerManager.getPlayer();
				// get the position of the target
				goalX = getTarget.getPosX();
				goalY = getTarget.getPosY(); 
				
				if(this.distance(getTarget) < speed) {
					this.setPosX(goalX);
					this.setPosY(goalY);
					return;
				}
				
			} else {
				//otehrwise, move to enemy's closest goal
				AbstractEntity getTarget = target.get();
				// get the position of the target
				goalX = getTarget.getPosX(); 
				goalY = getTarget.getPosY(); 
				
				if(this.distance(getTarget) < speed) {
					this.setPosX(goalX);
					this.setPosY(goalY);
					return;
				}
			}
			
		}
		

		float deltaX = getPosX() - goalX;
		float deltaY = getPosY() - goalY;

		float angle = (float)(Math.atan2(deltaY, deltaX)) + (float)(Math.PI);

		float changeX = (float)(speed * Math.cos(angle));
		float changeY = (float)(speed * Math.sin(angle));

		Box3D newPos = getBox3D();

		newPos.setX(getPosX() + changeX);
		newPos.setY(getPosY() + changeY);

		/*
		 * Check for enemies colliding with other entities. The following entities will not stop an enemy:
		 *     -> Enemies of the same type, projectiles, resources.
		 */
		Map<Integer, AbstractEntity> entities = GameManager.get().getWorld().getEntities();
		boolean collided = false;
		boolean collidedTankEffect = false;
		for (AbstractEntity entity : entities.values()) {
			if (!this.equals(entity) && !(entity instanceof Projectile)  && newPos.overlaps(entity.getBox3D()) ) {

				if(entity instanceof Tower) {
					//soundManager.playSound("ree1.wav");
				}

				if(entity instanceof Player) {
					LOGGER.info("Ouch! a " + this + " hit the player!");
					((Player) entity).damage(1);
					GameManager.get().getManager(PlayerManager.class).getPlayer().setDamaged(true);

				}
				if (entity instanceof Effect || entity instanceof ResourceEntity) {
					if (this instanceof TankEnemy && entity instanceof StompedGroundEffect) {
						collidedTankEffect = true;
					}
					continue;
				}
				collided = true;
			}
		}

		if (!collidedTankEffect && this instanceof TankEnemy) {
			GameManager.get().getWorld().addEntity(new StompedGroundEffect(getPosX(), getPosY(), 0, true));
			enemySoundManager.playSound("tankEnemyFootstep.wav");
		}

		if (!collided) {
			setPosX(getPosX() + changeX);
			setPosY(getPosY() + changeY);
		}
	}

	/**
	 * Registers the list of events given with the event manager and unregisters all
	 * other events for this object
	 */
	private void registerNewEvents(List<TimeEvent<EnemyEntity>> events) {
		EventManager eventManager = GameManager.get().getManager(EventManager.class);
		eventManager.unregisterAll(this);
		for (TimeEvent<EnemyEntity> timeEvent : events) {
		eventManager.registerEvent(this, timeEvent);
		}
	}

	/**
	 * Get the basic stats of this enemy
	 *
	 * @return the basic stats (BasicStats) for this enemy
	 * */
	public abstract EnemyStatistics getBasicStats();

	/**
	 * Get the goal of the enemy
	 * @return this enemy's goal
	 */
	public Class<?> getGoal() {
		return this.goal;
	}
	
	/**
	 * Set the enemy's goal to the given entity class
	 * @param g enemy's new goal(entity class)
	 */
	public void setGoal(Class<?> g) {
		this.goal = g;
	}
	
	/**
	 * Get the speed of this enemy
	 * @return the speed of this enemy
	 */
	public float getSpeed() {
		return this.speed;
	}
	
	/**
	 * Set this enemy's speed to given speed
	 * @param s enemy's new speed
	 */
	public void setSpeed(Float s) {
		this.speed = s;
	}

	/**
	 * If the enemy get shot, reduce enemy's health. Remove the enemy if dead. 
	 * @param projectile, the projectile shot
	 */
	public void getShot(Projectile projectile) {
		this.damage(projectile.getDamage());
		LOGGER.info(this + " was shot. Health now " + getHealth());
	}
	
	/**
	 * If the enemy get shot, reduce enemy's health. Remove the enemy if dead. 
	 * @param projectile, the projectile shot
	 */
	public void getShot(Effect effect) {
		this.damage(effect.getDamage());
		LOGGER.info(this + " was shot. Health now " + getHealth());
	}

	/**
	 * Returns the ProgressBar of an entity
	 * @return
	 */
	@Override
	public ProgressBarEntity getProgressBar() {
		return PROGRESS_BAR;
	}

	@Override
	public float getProgressRatio() {
		return getHealth() / getMaxHealth();
	}

	@Override
	public int getMaxProgress() {
		return (int) getMaxHealth();
	}

	//BROKEN BUILD!!
	//@Override
	//public void setMaxProgress(int p) { return; }
	
	/**
	 * remove the enemy if it is dead, and respawn after seconds 
	 */
	@Override
	public void deathHandler() {
		LOGGER.info(this + " is dead.");
		// destroy the enemy
		GameManager.get().getWorld().removeEntity(this);
		// get the event manager
		EventManager eventManager = GameManager.get().getManager(EventManager.class);
		// add the respawn event
		eventManager.registerEvent(this, new RespawnEvent(respawnTime));
	}

}