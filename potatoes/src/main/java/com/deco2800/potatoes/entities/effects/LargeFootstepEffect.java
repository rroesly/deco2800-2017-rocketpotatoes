package com.deco2800.potatoes.entities.effects;

import com.badlogic.gdx.math.Vector3;
import com.deco2800.potatoes.entities.AbstractEntity;
import com.deco2800.potatoes.entities.resources.ResourceEntity;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.SoundManager;
import com.deco2800.potatoes.util.Box3D;

import java.util.Map;

/**
 * A large footstep effect. Created when a large enemy moves around the map.
 * Effect is meant to animate dirt rising up from the ground.
 * This effect destroys any resources it comes into contact with (i.e. the enemy "stomped" on the resource.
 *
 * @author ryanjphelan
 */
public class LargeFootstepEffect extends Effect {
    // TODO Texture is a placeholder. Need to design proper artwork for footstep (for different terrains as well?)
    private static final transient String TEXTURE = "TankFootstepTemp1";

    private boolean resourceStomped = false;
    private Box3D effectPosition;
    private int currentTextureIndexCount = 0;
    private String[] currentTextureArray = { "TankFootstepTemp1", "TankFootstepTemp2", "TankFootstepTemp3" };
    private int timer = 0;

    /**
     * Empty constructor. Used for serialisation purposes
     */
    public LargeFootstepEffect() {
    }

    /**
     * Creates a new footstep effect.
     * NOTE: Effect is currently being manually shifted for visual purposes.
     * i.e. posX and posY do not match with the given inputs (posX is decreased by 1 and posY is increased by 0.5)
     *
     * @param posX
     *            x start position
     * @param posY
     *            y start position
     * @param posZ
     *            z start position
     */
    public LargeFootstepEffect(Class<?> targetClass, float posX, float posY, float posZ, float damage, float range) {
        super(targetClass, new Vector3(posX - 1f, posY + 0.5f, posZ), 1f, 1f, 0, 1.4f, 1.4f, damage, range, EffectType.LARGE_FOOTSTEP);
        effectPosition = getBox3D();
    }

    @Override
    public void onTick(long time) {
        timer++;
        if (!resourceStomped) {
            Map<Integer, AbstractEntity> entities = GameManager.get().getWorld().getEntities();
            for (AbstractEntity entity : entities.values()) {
                if (this.equals(entity) || !(entity instanceof ResourceEntity)
                        || !effectPosition.overlaps(entity.getBox3D())) {
                    continue;
                }

                String resourceType = ((ResourceEntity) entity).getType().getTypeName();
                GameManager.get().getWorld().removeEntity(entity);
                if ("seed".equals(resourceType)) {
                    GameManager.get().getManager(SoundManager.class).playSound("seedResourceDestroyed.wav");
                } else if ("food".equals(resourceType)) {
                    GameManager.get().getManager(SoundManager.class).playSound("foodResourceDestroyed.wav");
                } else {
                    GameManager.get().getManager(SoundManager.class).playSound("seedResourceDestroyed.wav");
                }
            }
            resourceStomped = true;
        }
        if (timer % 10 == 0) {
            if (currentTextureIndexCount < 3) {
                setTexture(currentTextureArray[currentTextureIndexCount]);
                currentTextureIndexCount++;
            } else {
                GameManager.get().getWorld().removeEntity(this);
            }
        }
    }

    /**
     * Return the Box3D position of the large footstep
     *
     * @return Box3D position of footstep
     */
    public Box3D getFootstepPosition() {
        return effectPosition;
    }

    /**
     * Get the current index for the texture being used
     *
     * @return Int current index
     */
    public int getCurrentTextureIndex() {
        return currentTextureIndexCount;
    }

    /**
     * String representation of the footstep at its set position.
     *
     * @return String representation of the stomped ground
     */
    @Override
    public String toString() {
        return String.format("Large Footstep at (%d, %d)", (int) getPosX(), (int) getPosY());
    }
}