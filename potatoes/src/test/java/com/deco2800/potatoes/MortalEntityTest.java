package com.deco2800.potatoes;

import com.deco2800.potatoes.entities.health.MortalEntity;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.worlds.InitialWorld;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MortalEntityTest {

	MortalEntity mortalEntity;
	private static final float HEALTH = 100f;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//a fake game world so deathHandler can interact with it
		InitialWorld mockWorld = mock(InitialWorld.class);
		GameManager gm = GameManager.get();
		gm.setWorld(mockWorld);
	}

	@Before
	public void setUp() throws Exception {
		mortalEntity = new MortalEntity(1, 2, 3, 4, 5, 6, "texture", HEALTH);
	}

	//Common to all initialisation test
	private void initTestCommon() {
		assertEquals("getMaxHealth() bad init ", HEALTH, mortalEntity.getMaxHealth(), 0f);
		assertEquals("getHealth() bad init ", HEALTH, mortalEntity.getHealth(), 0f);
		assertEquals("isDead() bad init ", false, mortalEntity.isDead());
		assertEquals("getDamageOffset() bad init ", 0f, mortalEntity.getDamageOffset(), 0f);
		assertEquals("getDamageScaling() bad init ", 1f, mortalEntity.getDamageScaling(), 0f);
	}

	@Test
	public void initTest() {
		mortalEntity = new MortalEntity(1, 2, 3, 4, 5, 6, "texture", HEALTH);
		initTestCommon();
	}

	@Test
	public void initTestEmpty() {
		try {
			mortalEntity = new MortalEntity();
		} catch (Exception E) {
			fail("No MortalEntity serializable constructor");
		}
	}

	@Test
	public void initTestVariableRendering() {
		mortalEntity = new MortalEntity(1, 2, 3, 4, 5, 6, 7, 8, "texture", HEALTH);
		initTestCommon();
	}

	@Test
	public void initTestCentred() {
		mortalEntity = new MortalEntity(1, 2, 3, 4, 5, 6, 7, 8, true, "texture", HEALTH);
		initTestCommon();
	}

	@Test
	public void maxHealthTest() {
		mortalEntity.addMaxHealth(37);
		assertEquals("addMaxHealth() can't add", HEALTH + 37, mortalEntity.getMaxHealth(), 0f);
		mortalEntity.addMaxHealth(-39);
		assertEquals("addMaxHealth() can't subtract", HEALTH - 2, mortalEntity.getMaxHealth(), 0f);
		mortalEntity.addMaxHealth(-99);
		assertEquals("addMaxHealth() can't subtract", 1f, mortalEntity.getMaxHealth(), 0f);

	}

	@Test
	public void damageHealTest() {
		assertEquals(HEALTH, mortalEntity.getHealth(), 0f);

		//regular case
		mortalEntity.damage(99);
		assertEquals(1, mortalEntity.getHealth(), 0f);
		mortalEntity.heal(98);
		assertEquals(99, mortalEntity.getHealth(), 0f);

		//0 health
		mortalEntity.damage(99);
		assertEquals(0, mortalEntity.getHealth(), 0f);
		assertTrue(mortalEntity.isDead());

		//back from the dead, asser no overheal
		mortalEntity.heal(130);
		assertFalse(mortalEntity.isDead());
		assertEquals(100, mortalEntity.getHealth(), 0f);

		//sub-zero
		mortalEntity.damage(103);
		assertEquals(-3f, mortalEntity.getHealth(), 0f);
		assertTrue(mortalEntity.isDead());
	}

	@Test
	public void damageOffsetTest() {
		//standard test
		mortalEntity.addDamageOffset(50);
		mortalEntity.damage(100);
		assertEquals(50, mortalEntity.getDamageOffset(), 0f);
		assertEquals(50, mortalEntity.getHealth(), 0f);

		//reverse previous offset
		mortalEntity.addDamageOffset(-50);
		mortalEntity.damage(25);
		assertEquals(0, mortalEntity.getDamageOffset(), 0f);
		assertEquals(25, mortalEntity.getHealth(), 0f);

		//ensure damageOffset cannot cause healing
		mortalEntity.addDamageOffset(500);
		mortalEntity.damage(25);
		assertEquals(500, mortalEntity.getDamageOffset(), 0f);
		assertEquals(25, mortalEntity.getHealth(), 0f);

	}

	@Test
	public void damageScalingTest() {
		//damage scaling
		mortalEntity.addDamageScaling(0.5f);
		mortalEntity.damage(100);
		assertEquals(0.5f, mortalEntity.getDamageScaling(), 0f);
		assertEquals(50, mortalEntity.getHealth(), 0f);

		//assert that scaling stacks correctly
		mortalEntity.addDamageScaling(0.5f);
		mortalEntity.damage(100);
		assertEquals(0.25f, mortalEntity.getDamageScaling(), 0f);
		assertEquals(25, mortalEntity.getHealth(), 0f);


		//revert scaling
		mortalEntity.removeDamageScaling(0.5f);
		assertEquals(0.5f, mortalEntity.getDamageScaling(), 0f);
		mortalEntity.removeDamageScaling(0.5f);
		assertEquals(1f, mortalEntity.getDamageScaling(), 0f);

		mortalEntity.heal(75);

		//dealing extra damage (scaling > 1)
		mortalEntity.addDamageScaling(4f);
		mortalEntity.damage(12.5f);
		assertEquals(4f, mortalEntity.getDamageScaling(), 0f);
		assertEquals(50, mortalEntity.getHealth(), 0f);

		//revert scaling again
		mortalEntity.removeDamageScaling(2f);
		assertEquals(2f, mortalEntity.getDamageScaling(), 0f);
		mortalEntity.removeDamageScaling(2f);
		assertEquals(1f, mortalEntity.getDamageScaling(), 0f);

		//negative scaling (healing)
		mortalEntity.addDamageScaling(-0.5f);
		mortalEntity.damage(200);
		assertEquals(-0.5f, mortalEntity.getDamageScaling(), 0f);
		assertEquals(100, mortalEntity.getHealth(), 0f);

		mortalEntity.removeDamageScaling(-0.5f);
		assertEquals(1f, mortalEntity.getDamageScaling(), 0f);

		//scaling and offset (negative)
		mortalEntity.addDamageScaling(0.5f);
		mortalEntity.addDamageOffset(-25);
		mortalEntity.damage(100);
		assertEquals(25, mortalEntity.getHealth(), 0f);

		mortalEntity.removeDamageScaling(0.5f);

		//scaling and offset (negative)
		mortalEntity.addDamageScaling(-1f);
		mortalEntity.addDamageOffset(100);
		mortalEntity.damage(25);
		assertEquals(50, mortalEntity.getHealth(), 0f);

	}

	@Test
	public void deathHandlerTest() {
		reset(GameManager.get().getWorld()); //resets all invocation counters related to mockWorld
		mortalEntity.deathHandler();
		verify(GameManager.get().getWorld()).removeEntity(any()); //ensure deathHandler called removeEntity()
	}
	
	@Test
	public void setProgressTest() {
		assertFalse("Health should be set to 0f", mortalEntity.setProgress(0f));
		assertTrue("Health should be set to 100f", mortalEntity.setProgress(100f));
	}

}
