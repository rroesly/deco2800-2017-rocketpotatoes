package com.deco2800.potatoes.worlds;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deco2800.potatoes.entities.Player;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.WorldManager;
import com.deco2800.potatoes.util.GridUtil;
import com.deco2800.potatoes.worlds.terrain.Terrain;
import com.deco2800.potatoes.worlds.terrain.TerrainType;

/**
 * Represents a type of world with a set of terrain types and world generation.
 */
public class WorldType {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorldType.class);
	private static final boolean FLOOD_CHECK = false;
	
	private static final String GROUND = "ground_1";
	private static final String WATER = "w1";
	private static final String GRASS = "grass";
	public static final WorldType FOREST_WORLD = new WorldType(new TerrainType(null, new Terrain(GRASS, 1, true),
			new Terrain(GROUND, 1, false), new Terrain(WATER, 0, false)));
	public static final WorldType DESERT_WORLD = new WorldType(new TerrainType(null, new Terrain(GRASS, 0.5f, true),
			new Terrain(GROUND, 1, false), new Terrain(WATER, 0, false)));
	public static final WorldType ICE_WORLD = new WorldType(new TerrainType(null, new Terrain(GRASS, 1, true),
			new Terrain(GROUND, 1, false), new Terrain(WATER, 2f, false)));
	public static final WorldType VOLCANO_WORLD = new WorldType(new TerrainType(null, new Terrain(GRASS, 1, true),
			new Terrain(GROUND, 0.5f, false), new Terrain(WATER, 0, false)));
	public static final WorldType OCEAN_WORLD = new WorldType(new TerrainType(null, new Terrain(WATER, 1, true),
			new Terrain(GROUND, 1, false), new Terrain(GRASS, 0, false)));

	private final TerrainType terrain;
	private List<Point> clearSpots = new ArrayList<>();
	private float landAmount = 0.3f;

	/**
	 * @param terrain
	 *            the terrain type
	 */
	public WorldType(TerrainType terrain) {
		this.terrain = terrain;
		// Temporary
		clearSpots.add(new Point(5, 10));
	}

	/**
	 * @return the terrain type
	 */
	public TerrainType getTerrain() {
		return terrain;
	}

	/**
	 * Generates a grid of terrain based on the given world size. The terrain types
	 * and world generation is based on the details of this world type
	 */
	public Terrain[][] generateWorld(int worldSize) {
		WorldManager wm = GameManager.get().getManager(WorldManager.class);
		Terrain[][] terrainSet = new Terrain[worldSize][worldSize];
		boolean validLand = false;
		while (!validLand) {
			float[][] water = wm.getRandomGridEdge();
			float[][] height = wm.getRandomGrid();
			float[][] grass = wm.getRandomGrid();
			for (int x = 0; x < worldSize; x++) {
				for (int y = 0; y < worldSize; y++) {
					terrainSet[x][y] = chooseTerrain(water, height, grass, x, y);
				}
			}
			validLand = checkValidLand(worldSize, terrainSet);
		}
		return terrainSet;
	}

	private boolean checkValidLand(int worldSize, Terrain[][] terrainSet) {
		boolean validLand = true;
		for (Point point : clearSpots) {
			if (FLOOD_CHECK) {
				// Currently broken
				Set<Point> filled = new HashSet<>();
				GridUtil.genericFloodFill(point, floodFillCheck(worldSize, terrainSet), new HashSet<>(), filled);
				if (landAmount * worldSize * worldSize > filled.size()) {
					validLand = false;
				}
			} else if(terrainSet[point.x][point.y].getTexture().equals(WATER)) {
				validLand = false;
			}
		}
		return validLand;
	}

	private Function<Point, Boolean> floodFillCheck(int worldSize, Terrain[][] terrainSet) {
		return p -> p.x > 0 && p.y > 0 && p.x < worldSize && p.y < worldSize
				&& !terrainSet[p.x][p.y].getTexture().equals(WATER);
	}

	private Terrain chooseTerrain(float[][] water, float[][] height, float[][] grass, int x, int y) {
		Terrain spot;
		if (height[x][y] < 0.3 || water[x][y] < 0.4) {
			spot = getTerrain().getWater();
		} else if (height[x][y] < 0.35 || water[x][y] < 0.5) {
			spot = getTerrain().getRock();
		} else {
			spot = grass[x][y] < 0.5 ? getTerrain().getGrass() : getTerrain().getRock();
		}
		return spot;
	}

	/*
	 * Auto generated, no need to manually test. Created from fields: terrain
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((terrain == null) ? 0 : terrain.hashCode());
		return result;
	}

	/*
	 * Auto generated, no need to manually test. Created from fields: terrain
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorldType other = (WorldType) obj;
		if (terrain == null) {
			if (other.terrain != null)
				return false;
		} else if (!terrain.equals(other.terrain))
			return false;
		return true;
	}
}
