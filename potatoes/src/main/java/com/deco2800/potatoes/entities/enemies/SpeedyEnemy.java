package com.deco2800.potatoes.entities.enemies;


import java.util.*;

import com.deco2800.potatoes.entities.Direction;
import com.deco2800.potatoes.entities.enemies.enemyactions.StealingEvent;
import com.deco2800.potatoes.entities.player.Archer;
import com.deco2800.potatoes.entities.player.Caveman;
import com.deco2800.potatoes.entities.player.Wizard;
import com.deco2800.potatoes.entities.portals.BasePortal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.graphics.Color;
import com.deco2800.potatoes.collisions.Circle2D;
import com.deco2800.potatoes.collisions.Shape2D;
import com.deco2800.potatoes.entities.AbstractEntity;
import com.deco2800.potatoes.entities.Direction;
import com.deco2800.potatoes.entities.PropertiesBuilder;
import com.deco2800.potatoes.entities.Tickable;
import com.deco2800.potatoes.entities.health.ProgressBarEntity;
import com.deco2800.potatoes.entities.trees.ResourceTree;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.PathManager;
import com.deco2800.potatoes.managers.PlayerManager;
import com.deco2800.potatoes.util.Path;
import com.deco2800.potatoes.util.WorldUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A speedy raccoon enemy that steals resources from resource trees.
 */
public class SpeedyEnemy extends EnemyEntity implements Tickable {

	private static final transient String TEXTURE = "speedyRaccoon";
	private static final transient float HEALTH = 80f;
	private static final transient float ATTACK_RANGE = 0.5f;
	private static final transient int ATTACK_SPEED = 2000;
//	private static final transient String ENEMY_TYPE = "raccoon";
	private static final transient String[] ENEMY_TYPE = new String[]{

		"raccoon"
};

	private static final EnemyProperties STATS = initStats();

	private static float speed = 0.08f;
	private static Class<?> goal = ResourceTree.class;

	private Path path = null;
	private Shape2D target = null;
	private PathAndTarget pathTarget = new PathAndTarget(path, target);
	private EnemyTargets targets = initTargets();
	private LinkedList<ResourceTree> resourceTreeQueue = allResourceTrees(); //is there a better data structure for this
	private LinkedList<ResourceTree> visitedResourceTrees = new LinkedList<>();

	private static final List<Color> COLOURS = Arrays.asList(Color.PURPLE, Color.RED, Color.ORANGE, Color.YELLOW);
	private static final ProgressBarEntity PROGRESSBAR = new ProgressBarEntity(COLOURS);

	private Direction currentDirection; // The direction the enemy faces
	//public enum PlayerState {idle, walk, attack, damaged, death}  // useful for when sprites available

	/**
	 * Empty constructor for serialization
	 */
	public SpeedyEnemy() {
		//Empty constructor for serialization
	}

	/***
	 * Construct a new speedy raccoon enemy at specific position with pre-defined size and render-length.
	 *
	 * @param posX
	 * @param posY
	 */
	public SpeedyEnemy(float posX, float posY) {
        super(new Circle2D(posX, posY, 0.707f), 0.55f, 0.55f, TEXTURE, HEALTH, speed, goal);
		SpeedyEnemy.speed = speed + ((speed*roundNum)/2);
		SpeedyEnemy.goal = goal;
		this.path = null;
	}

	/***
	 * Initialise EnemyStatistics belonging to this enemy which is referenced by other classes to control
	 * enemy.
	 *
	 * @return
	 */
	private static EnemyProperties initStats() {
		EnemyProperties result = new PropertiesBuilder<EnemyEntity>().setHealth(HEALTH).setSpeed(speed)
				.setAttackRange(ATTACK_RANGE).setAttackSpeed(ATTACK_SPEED).setTexture(TEXTURE)
				.addEvent(new StealingEvent(1000,ResourceTree.class))
				.createEnemyStatistics();
		return result;
	}

	/***
	 * @return the EnemyStatistics of enemy which contain various governing stats of this enemy
	 */
	@Override
	public EnemyProperties getBasicStats() {
		return STATS;
	}

	/**
	 * @return string representation of this class including its enemytype and x,y coordinates
	 */
	@Override
	public String toString() {
		return String.format("%s at (%d, %d)", getEnemyType()[0], (int) getPosX(), (int) getPosY());
	}

	/***
	 * Gets the progress bar that corresponds to the health of this enemy
	 *
	 * @return ProgressBarEntity corresponding to enemy's health
	 */
	@Override
	public ProgressBarEntity getProgressBar() {
		return PROGRESSBAR;
	}

	/**
	 * Steal resources from ResourceTrees if within range
	 */
	public void stealResources() {
		double interactRange = 2f;
		Collection<AbstractEntity> entities = GameManager.get().getWorld().getEntities().values();
		for (AbstractEntity entitiy : entities) {
			if (entitiy instanceof ResourceTree && entitiy.distanceTo(this) <= interactRange &&((ResourceTree) entitiy).getGatherCount() > 0) {
				((ResourceTree) entitiy).gather(-1);
			}
		}
	}

	public void addTreeToVisited(ResourceTree tree) {
		resourceTreeQueue.remove(tree);
		visitedResourceTrees.add(tree);
		if (resourceTreeQueue.isEmpty()) {
			//Raccoon has stolen from all resource trees, now reset.
			resourceTreeQueue = allResourceTrees();
			visitedResourceTrees = new LinkedList<>();
			visitedResourceTrees.add(tree);
		}
	}


	private LinkedList<ResourceTree> allResourceTrees() {
		LinkedList<ResourceTree> resourceTrees = new LinkedList<>();
		for (AbstractEntity entity : GameManager.get().getWorld().getEntities().values()) {
			if (entity instanceof ResourceTree) {
				resourceTrees.add((ResourceTree) entity);
			}
		}
		return resourceTrees;
	}

	/*Find the most relevant target to go to according to its EnemyTargets*/
	public AbstractEntity mostRelevantTarget(EnemyTargets targets) {
		Map<Integer, AbstractEntity> entities = GameManager.get().getWorld().getEntities();
		/*Is a sight aggro-able target within range of enemy - if so, return as a target*/
		for (Class sightTarget : targets.getSightAggroTargets()) {
			for (AbstractEntity entity : entities.values()) {
				if (entity.getClass().isAssignableFrom(sightTarget)) {	//HOW TO CHECK SUPERCLASS SO WE CAN JUST ADD PLAYER TO TARGETS?
					float distance = WorldUtil.distance(this.getPosX(), this.getPosY(), entity.getPosX(), entity.getPosY());
					if ((distance < 10) && (!(visitedResourceTrees.contains(entity)))) {
						return entity;
					}
				}
			}
		}
		/*If no aggro, return 'ultimate' target*/
		for (Class mainTarget : targets.getMainTargets()) {
			for (AbstractEntity entity : entities.values()) {
				if (entity.getClass().isAssignableFrom(mainTarget)) {
					if  (!(visitedResourceTrees.contains(entity))) {
						return entity;
					}
				}
			}
		}
		return null;
	}


	/**
	 *	@return the current Direction of raccoon
	 * */
	public Direction getDirection() { return currentDirection; }

	/**
	 * @return String of this type of enemy (ie 'raccoon').
	 * */
	@Override
	public String[] getEnemyType() { return ENEMY_TYPE; }

	@Override
	public void onTick(long i) {
		AbstractEntity relevantTarget = mostRelevantTarget(targets);
		if (getMoving() == true) {
			pathMovement(pathTarget, relevantTarget);
			super.onTickMovement();
		}
		super.updateDirection();
	}


	private EnemyTargets initTargets() {
		/*Enemy will move to these (in order) if no aggro*/
		ArrayList<Class> mainTargets = new ArrayList<>();
		mainTargets.add(ResourceTree.class);
		mainTargets.add(BasePortal.class);
		mainTargets.add(Archer.class);
		mainTargets.add(Caveman.class);
		mainTargets.add(Wizard.class);

		/*if enemy can 'see' these, then enemy aggros to these*/
		ArrayList<Class> sightAggroTargets = new ArrayList<>();
		//sightAggroTargets.add(ResourceTree.class);
		sightAggroTargets.add(Archer.class);
		sightAggroTargets.add(Caveman.class);
		sightAggroTargets.add(Wizard.class);

		return new EnemyTargets(mainTargets, sightAggroTargets);
	}
}
