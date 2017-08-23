package com.deco2800.potatoes.handlers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.deco2800.potatoes.entities.AbstractEntity;
import com.deco2800.potatoes.entities.Clickable;
import com.deco2800.potatoes.entities.SeedResource;
import com.deco2800.potatoes.entities.Tower;
import com.deco2800.potatoes.entities.trees.ResourceTree;
import com.deco2800.potatoes.managers.CameraManager;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.MultiplayerManager;
import com.deco2800.potatoes.observers.MouseMovedObserver;
import com.deco2800.potatoes.observers.TouchDownObserver;
import com.deco2800.potatoes.observers.TouchDraggedObserver;
import com.deco2800.potatoes.renderering.Render3D;
import com.deco2800.potatoes.util.WorldUtil;
import com.deco2800.potatoes.worlds.AbstractWorld;
import com.deco2800.potatoes.worlds.InitialWorld;

import java.util.Optional;

/**
 * Really crappy mouse handler for the game
 */
public class MouseHandler implements TouchDownObserver, TouchDraggedObserver, MouseMovedObserver {
	private int originX;
	private int originY;

	/**
	 * Constructor for the mouse handler
	 */
	public MouseHandler() {
	}

	/**
	 * Currently only handles objects on height 0
	 * 
	 * @param x
	 * @param y
	 */
	public void handleMouseClick(float x, float y, int button) {
		Vector2 coords = Render3D.screenToWorldCoordiates(x, y);

		Optional<AbstractEntity> closest = WorldUtil.closestEntityToPosition(coords.x, coords.y, 2f);
		if (closest.isPresent() && closest.get() instanceof Clickable) {
			((Clickable) closest.get()).onClick();
		} else {
			AbstractWorld world = GameManager.get().getWorld();
			if (world instanceof InitialWorld) {
				((InitialWorld) (world)).deSelectAll();
			}
		}
		// Build Testing
		// Check tile is occupied
		if (!WorldUtil.getEntityAtPosition(Math.round(coords.x), Math.round(coords.y)).isPresent()) {
			MultiplayerManager multiplayerManager = (MultiplayerManager) GameManager.get()
					.getManager(MultiplayerManager.class);
			if (!multiplayerManager.isMultiplayer() || multiplayerManager.isMaster()) {
				if (button == 0) {
					// Adds a projectile tree
					GameManager.get().getWorld().addEntity(new Tower(Math.round(coords.x), Math.round(coords.y), 0));
				} else {
					// Adds a resource tree
					GameManager.get().getWorld().addEntity(new ResourceTree(Math.round(coords.x), Math.round(coords.y), 0, new SeedResource()));
				}
			} else {
				multiplayerManager.broadcastBuildOrder(Math.round(coords.x), Math.round(coords.y));
			}
		}

	}

	@Override
	public void notifyTouchDown(int screenX, int screenY, int pointer, int button) {
		originX = screenX;
		originY = screenY;

		Vector3 worldCoords = getCameraManager().getCamera().unproject(new Vector3(screenX, screenY, 0));
		handleMouseClick(worldCoords.x, worldCoords.y, button);
	}

	@Override
	public void notifyTouchDragged(int screenX, int screenY, int pointer) {
		OrthographicCamera c = getCameraManager().getCamera();

		originX -= screenX;
		originY -= screenY;

		// invert the y axis
		originY = -originY;

		originX += getCameraManager().getCamera().position.x;
		originY += getCameraManager().getCamera().position.y;

		c.translate(originX - c.position.x, originY - c.position.y);

		originX = screenX;
		originY = screenY;
	}

	private CameraManager getCameraManager() {
		return (CameraManager) GameManager.get().getManager(CameraManager.class);
	}

	@Override
	public void notifyMouseMoved(int screenX, int screenY) {
		Vector2 coords = Render3D.screenToWorldCoordiates(screenX, screenY);

		
	}
}
