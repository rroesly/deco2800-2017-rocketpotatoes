package com.deco2800.potatoes.entities;

import java.util.Collection;
import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction;
import com.deco2800.potatoes.managers.GameManager;

public class BallisticProjectile extends Projectile {

	private final static transient String TEXTURE = "projectile";
	private static float DAMAGE = 1;
	private static float RANGE;

	private float goalX;
	private float goalY;
	private float goalZ;
	
	private int rotateAngle = 0;

	private final float speed = 0.2f;
	private Optional<AbstractEntity> mainTarget;
	private float changeX;
	private float changeY;
	private float changeZ;
	
	private boolean maxRange = false;

	public BallisticProjectile() {
		// empty for serialization
	}

	/**
	 * Creates a new Ballistic Projectile. Ballistic Projectiles do not change
	 * direction once fired. The initial direction is based on the direction to the
	 * closest entity
	 * 
	 * @param posX
	 *            x start position
	 * @param posY
	 *            y start position
	 * @param posZ
	 *            z start position
	 * @param target
	 *            Entity target object
	 * @param RANGE
	 *            Projectile range
	 * @param DAMAGE
	 *            Projectile damage
	 */
	public BallisticProjectile(float posX, float posY, float posZ, Optional<AbstractEntity> target, float RANGE,
			float DAMAGE) {
		super(posX, posY, posZ, TEXTURE);
		this.DAMAGE = DAMAGE;
		this.mainTarget = target;
		this.goalX = target.get().getPosX();
		this.goalY = target.get().getPosY();
		this.goalZ = target.get().getPosZ();

		this.RANGE = RANGE;

		float deltaX = getPosX() - goalX;
		float deltaY = getPosY() - goalY;
		float deltaZ = getPosZ() - goalZ;

		float angle = (float) (Math.atan2(deltaY, deltaX)) + (float) (Math.PI);

		changeX = (float) (speed * Math.cos(angle));
		changeY = (float) (speed * Math.sin(angle));
		// TODO: add changeZ
		
		rotateAngle = (int) ((angle * 180 / Math.PI) + 45 + 90);

	}
	
	/**
	 * Creates a new Ballistic Projectile. Ballistic Projectiles do not change
	 * direction once fired. The initial direction is based on the direction to the
	 * closest entity
	 * 
	 * @param posX
	 *            x start position
	 * @param posY
	 *            y start position
	 * @param posZ
	 *            z start position
	 * @param fPosX
	 *            target x position
	 * @param fPosY
	 *            target y position
	 * @param fPosZ
	 *            target z position
	 * @param RANGE
	 *            Projectile range
	 * @param DAMAGE
	 *            Projectile damage
	 */
	public BallisticProjectile(float posX, float posY, float posZ, float fPosX, float fPosY, float fPosZ, float RANGE,
			float DAMAGE) {
		super(posX, posY, posZ, TEXTURE);
		this.DAMAGE = DAMAGE;
		
		this.goalX = fPosX;
		this.goalY = fPosY;
		this.goalZ = fPosZ;

		this.RANGE = RANGE;

		float deltaX = getPosX() - goalX;
		float deltaY = getPosY() - goalY;
		float deltaZ = getPosZ() - goalZ;

		float angle = (float) (Math.atan2(deltaY, deltaX)) + (float) (Math.PI);
		
		changeX = (float) (speed * Math.cos(angle));
		changeY = (float) (speed * Math.sin(angle));
		// TODO: add changeZ
		
		rotateAngle = (int) ((angle * 180 / Math.PI) + 45 + 90);
	}
	
	public int rotateAngle() {
		System.out.println(rotateAngle);
		return rotateAngle;
	}

	@Override
	public void onTick(long time) {
		
		updatePos();

		Collection<AbstractEntity> entities = GameManager.get().getWorld().getEntities().values();
		for (AbstractEntity entity : entities) {

			if (entity instanceof EnemyEntity && this.collidesWith(entity)) {
				((EnemyEntity) entity).getShot(this);
				GameManager.get().getWorld().removeEntity(this);

				/**
				 * Width of AOE sprite. NOTE: (height < width) to give isometric illusion
				 */
				float AOE_width = 5f;
				float AOE_height = 2f;
				
				/**
				 * Spawn explosion when projectile hits entity
				 */
				ExplosionProjectile exp = new ExplosionProjectile(goalX - (AOE_width / 2), goalY - (AOE_height / 2), 0,
						AOE_width+10, AOE_height+10, 0, AOE_width, AOE_height, 1);
				GameManager.get().getWorld().addEntity(exp);

				return;
			}
		}

		if (maxRange) {
			GameManager.get().getWorld().removeEntity(this);
		}
	}
	
	public void updatePos() {
		maxRange = false;
		if (RANGE < speed) {
			setPosX(goalX);
			setPosY(goalY);
			setPosZ(goalZ);
			maxRange = true;
		} else {
			setPosX(getPosX() + changeX);
			setPosY(getPosY() + changeY);
		}
		RANGE -= speed;
	}
	
	public float getRange() {
		return RANGE;
	}
	
	@Override
	public float getDamage() {
		return DAMAGE;
	}

}
