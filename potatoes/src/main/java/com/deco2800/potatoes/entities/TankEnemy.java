package com.deco2800.potatoes.entities;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.LinkedList;
import java.util.List;


import com.deco2800.potatoes.entities.Enemies.BasicStats;
import com.deco2800.potatoes.entities.Enemies.MeleeAttackEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deco2800.potatoes.entities.AbstractEntity;
import com.deco2800.potatoes.entities.Tickable;
import com.deco2800.potatoes.entities.trees.AbstractTree;
import com.deco2800.potatoes.entities.trees.ProjectileTree;
import com.deco2800.potatoes.entities.trees.ResourceTree;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.PlayerManager;
import com.deco2800.potatoes.managers.SoundManager;
import com.deco2800.potatoes.util.Box3D;
import com.deco2800.potatoes.util.WorldUtil;

/**
 * Create a tank enemy in the game. Tank has doubled health and half speed but only attacks trees/towers. 
 */
public class TankEnemy extends EnemyEntity implements Tickable, HasProgress, ProgressBar{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TankEnemy.class);
	private static final transient String TEXTURE = "tankBear";
	private static final transient float HEALTH = 200f;
	private transient Random random = new Random();

	/*Testing attacking*/
	private static final BasicStats STATS = initStats();
	/*Testing attacking*/

	private static float speed = 0.05f;
	private static Class goal = Tower.class;

	public TankEnemy() {
		super(0, 0, 0, 1f, 1f, 1f, 1f, 1f, TEXTURE, HEALTH);
		initStats();
	}

	public TankEnemy(float posX, float posY, float posZ) {
		super(posX, posY, posZ, 1f, 1f, 1f, 1f, 1f, TEXTURE, HEALTH);
		initStats();
	}

	@Override
	public void onTick(long i) {
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
	
	
	@Override
	public void setProgressBar(AbstractEntity entity, Texture progressBar, SpriteBatch batch, int xLength, int yLength) {
		if (health > 160f) {
			batch.setColor(Color.GREEN);
		} else if (health > 100f) {
			batch.setColor(Color.ORANGE);
		} else {
			batch.setColor(Color.RED);
		}

		batch.draw(progressBar, xLength, yLength/3, health/3, 5);
		batch.setColor(Color.WHITE);
	}

	private static BasicStats initStats() {
		List<TimeEvent<EnemyEntity>> normalEvents = new LinkedList<>();
		BasicStats result = new BasicStats(200, 500, .8f, 500, normalEvents,"tankBear");
		result.getNormalEventsReference().add(new MeleeAttackEvent(500));
		return result;
	}

	@Override
	public BasicStats getBasicStats() {
		return STATS;
	}
	
	
	
	
	@Override
	public String toString() {
		return String.format("Tank Enemy at (%d, %d)", (int) getPosX(), (int) getPosY());
	}

}
