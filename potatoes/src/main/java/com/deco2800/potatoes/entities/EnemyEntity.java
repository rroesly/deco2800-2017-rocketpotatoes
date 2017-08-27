package com.deco2800.potatoes.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import com.deco2800.potatoes.entities.Enemies.BasicStats;
import com.badlogic.gdx.graphics.Color;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.PlayerManager;
import com.deco2800.potatoes.managers.SoundManager;
import com.deco2800.potatoes.util.Box3D;
import com.deco2800.potatoes.util.WorldUtil;
import com.deco2800.potatoes.managers.EventManager;

public abstract class EnemyEntity extends MortalEntity implements HasProgressBar, Tickable {
	private transient Random random = new Random();
	private float speed;
	private Class<?> goal;

	private static final List<Color> colours = Arrays.asList(Color.RED);
	private static final ProgressBarEntity progressBar = new ProgressBarEntity("progress_bar", colours, 0, 1);

	/**
	 * Default constructor for serialization
	 */
	public EnemyEntity() {
		// empty for serialization
		resetStats();
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
	 */
	public EnemyEntity(float posX, float posY, float posZ, float xLength, float yLength, float zLength,
			String texture, float maxHealth, float speed, Class<?> goal) {
		super(posX, posY, posZ, xLength, yLength, zLength, xLength, yLength, false, texture, maxHealth);
		resetStats();
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
	 */
	public EnemyEntity(float posX, float posY, float posZ, float xLength, float yLength, float zLength,
			float xRenderLength, float yRenderLength, String texture, float maxHealth, float speed, Class<?> goal) {
		super(posX, posY, posZ, xLength, yLength, zLength, xRenderLength, yRenderLength, texture, maxHealth);
		resetStats();
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
	 */
	public EnemyEntity(float posX, float posY, float posZ, float xLength, float yLength, float zLength,
			float xRenderLength, float yRenderLength, boolean centered, String texture, float maxHealth, float speed, Class<?> goal) {
		super(posX, posY, posZ, xLength, yLength, zLength, xRenderLength, yRenderLength, centered, texture, maxHealth);
		resetStats();
		this.speed = speed;
		this.goal = goal;
	}

	/**
	 * Move the enemy to its target. If the goal is player, use playerManager to get targeted player position for target, 
	 * otherwise get the closest targeted entity position.
	 */
	@Override
	public void onTick(long i) {
		if (goal == Player.class) {
			PlayerManager playerManager = (PlayerManager) GameManager.get().getManager(PlayerManager.class);
			SoundManager soundManager = (SoundManager) GameManager.get().getManager(SoundManager.class);

			//The X and Y position of the player without random floats generated
			float goalX = playerManager.getPlayer().getPosX() ;
			float goalY = playerManager.getPlayer().getPosY() ;
		
			if(this.distance(playerManager.getPlayer()) < speed) {
				this.setPosX(goalX);
				this.setPosY(goalY);
				return;
			}

			float deltaX = getPosX() - goalX;
			float deltaY = getPosY() - goalY;

			float angle = (float)(Math.atan2(deltaY, deltaX)) + (float)(Math.PI);

			float changeX = (float)(speed * Math.cos(angle));
			float changeY = (float)(speed * Math.sin(angle));

			Box3D newPos = getBox3D();

			newPos.setX(getPosX() + changeX);
			newPos.setY(getPosY() + changeY);

			 
			Map<Integer, AbstractEntity> entities = GameManager.get().getWorld().getEntities();
			boolean collided = false;
			for (AbstractEntity entity : entities.values()) {
				if (!this.equals(entity) && !(entity instanceof Projectile) && newPos.overlaps(entity.getBox3D()) ) {
					if(entity instanceof Player) {
						//soundManager.playSound("ree1.wav");
					}
					collided = true;
				}
			}

			if (!collided) {
				setPosX(getPosX() + changeX);
				setPosY(getPosY() + changeY);
			}
		} else {
			//set the target of tankEnemy to the closest goal
			Optional<AbstractEntity> target = WorldUtil.getClosestEntityOfClass(goal, getPosX(), getPosY());
			//get the position of the target
			float goalX = target.get().getPosX(); 
			float goalY = target.get().getPosY(); 
			
			if(this.distance(target.get()) < speed) {
				this.setPosX(goalX);
				this.setPosY(goalY);
				return;
			}

			float deltaX = getPosX() - goalX;
			float deltaY = getPosY() - goalY;

			float angle = (float)(Math.atan2(deltaY, deltaX)) + (float)(Math.PI);

			float changeX = (float)(speed * Math.cos(angle));
			float changeY = (float)(speed * Math.sin(angle));

			Box3D newPos = getBox3D();

			newPos.setX(getPosX() + changeX);
			newPos.setY(getPosY() + changeY);
 
			Map<Integer, AbstractEntity> entities = GameManager.get().getWorld().getEntities();
			boolean collided = false;
			for (AbstractEntity entity : entities.values()) {
				if (!this.equals(entity) && !(entity instanceof Projectile) && newPos.overlaps(entity.getBox3D()) ) {
					if(entity instanceof Tower) {
						//soundManager.playSound("ree1.wav");
					}
					collided = true;
				}
			}

			if (!collided) {
				setPosX(getPosX() + changeX);
				setPosY(getPosY() + changeY);
			}
		}

	}

	private void registerNewEvents(List<TimeEvent<EnemyEntity>> events) {
		EventManager eventManager = (EventManager) GameManager.get().getManager(EventManager.class);
		eventManager.unregisterAll(this);
		for (TimeEvent<EnemyEntity> timeEvent : events) {
		eventManager.registerEvent(this, timeEvent);
		}
	}

	/**
	 * Gets the basic stats that apply to this enemy
	 *
	 * @return the basic stats (BasicStats) for this enemy
	 * */
	public abstract BasicStats getBasicStats();

	public void resetStats() {
		this.addMaxHealth(getBasicStats().getHealth() - this.getMaxHealth());
		this.heal(getMaxHealth());
		setTexture(getBasicStats().getTexture());
		this.speed = getBasicStats().getSpeed();
		registerNewEvents(getBasicStats().getNormalEventsCopy());
	}

	@Override
	public int getProgress() {
		return (int) getHealth();
	}

	@Override
	public void setProgress(int p) {
		return;
	}

	@Override
	public boolean showProgress() {
		return true;
	}

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
		//System.out.println(this + " was shot. Health now " + getHealth());
	}

	public ProgressBarEntity getProgressBar() {
		return progressBar;
	}

	@Override
	public float getProgressRatio() {
		return (getHealth() / getMaxHealth());
	}

	@Override
	public int getMaxProgress() {
		return (int) getMaxHealth();
	}

	@Override
	public void setMaxProgress(int p) { return; }

}
