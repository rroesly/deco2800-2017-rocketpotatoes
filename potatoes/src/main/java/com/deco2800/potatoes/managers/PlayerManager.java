package com.deco2800.potatoes.managers;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Array;
import com.deco2800.potatoes.entities.player.Archer;
import com.deco2800.potatoes.entities.player.Caveman;
import com.deco2800.potatoes.entities.player.Player;
import com.deco2800.potatoes.entities.player.Wizard;

/**
 * PlayerManager for managing the Player instance.
 * 
 * @author leggy
 *
 */
public class PlayerManager extends Manager {

	private Player player;

	public PlayerManager() {
		InputManager input = GameManager.get().getManager(InputManager.class);

		input.addKeyDownListener(this::handleKeyDown);
		input.addKeyUpListener(this::handleKeyUp);
	}
	
	// Types of players in the game
	public enum PlayerType { caveman, wizard, archer;
		public static Array<String> names() {
			Array<String> names = new Array<>();
			for (PlayerType type : PlayerType.values()) {
				names.add(type.name());
			}
			return names;
		}
	};
	
	private PlayerType playerType = PlayerType.caveman;	// The type of the player
	
	public void setPlayerType(PlayerType type) {
		this.playerType = type;
	}
	
	public PlayerType getPlayerType() {
		return this.playerType;
	}

	/**
	 * Sets the player to specified player.
	 * 
	 * @param player
	 */
	public void setPlayer(Player player) {
		this.player = player;
		// Set camera manager to target the player
		GameManager.get().getManager(CameraManager.class).setTarget(player);
	}
	
	/**
	 * Sets the player based on the player type.
	 * 
	 * @param player
	 */
	public void setPlayer(float posX, float posY, float posZ) {
		switch (this.playerType) {
		case caveman:
			this.player = new Caveman(posX, posY, posZ);
			break;
		case archer:
			this.player = new Archer(posX, posY, posZ);
			break;
		case wizard:
			this.player = new Wizard(posX, posY, posZ);
			break;
		default:
			this.player = new Player(posX, posY, posZ);
			break;
		}
		
		// Set camera manager to target the player
		GameManager.get().getManager(CameraManager.class).setTarget(this.player);
	}

	public void handleKeyDown(int keycode) {
		if (player != null) {
			player.handleKeyDown(keycode);
		}
	}

	public void handleKeyUp(int keycode) {
		if (player != null) {
			player.handleKeyUp(keycode);
		}
	}

	/**
	 * Gets the player.
	 * 
	 * @return Returns the player.
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Returns the distance from player in tile coordinates
	 * 
	 * @param tileX
	 *            x coord in tiles
	 * @param tileY
	 *            y coord in tiles
	 * @return distance from position to player
	 */
	public float distanceFromPlayer(float tileX, float tileY) {
		float deltaX = player.getPosX() - tileX;
		float deltaY = player.getPosY() - tileY;
		return (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
	}

}
