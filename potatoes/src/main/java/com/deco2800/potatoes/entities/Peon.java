package com.deco2800.potatoes.entities;

import com.deco2800.moos.entities.Tickable;
import com.deco2800.moos.worlds.WorldEntity;

/**
 * A generic player instance for the game
 */
public class Peon extends WorldEntity implements Tickable {

	/**
	 * Constructor for the Peon
	 * @param world
	 * @param posX
	 * @param posY
	 * @param posZ
	 */
	public Peon(float posX, float posY, float posZ) {
		super(posX, posY, posZ, 1, 1, 1);
		this.setTexture("player");
	}

	@Override
	public void onTick(int i) {

	}
}