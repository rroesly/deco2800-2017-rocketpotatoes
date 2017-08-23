package com.deco2800.potatoes;

import com.deco2800.potatoes.entities.trees.*;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.worlds.InitialWorld;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProjectileTreeTest {

    ProjectileTree testTree;
    private static final int RELOAD = 100;
    private static final float HEALTH = 10f;
    private static final float RANGE = 8f;

    //Common to all initialisation test
    private void initTestCommon() {
        assertEquals("getConstructionLeft() incorect construction time", 100, testTree.getConstructionLeft());
        testTree.decrementConstructionLeft();
        assertEquals("decrementConstructionLeft() incorect construction time", 99, testTree.getConstructionLeft());
        assertNotNull("getUpgradeStats() returns null", testTree.getUpgradeStats());
        assertNotNull("getAllUpgradeStats() returns null", testTree.getAllUpgradeStats());
    }

    private void methodTestCommon() {
        //
        assertTrue(HEALTH==testTree.getHealth());
        testTree.damage(2);
        assertTrue(HEALTH-2==testTree.getHealth());
        testTree.resetStats();
        assertTrue(HEALTH==testTree.getHealth());
        testTree.onTick(1);
        testTree.upgrade();
        assertTrue(20f==testTree.getHealth());
        assertNotNull("getUpgradeStats() returns null", testTree.getUpgradeStats());
        testTree.onTick(1);
        testTree.upgrade();
        assertTrue(30f==testTree.getHealth());
        assertNotNull("getUpgradeStats() returns null", testTree.getUpgradeStats());
        testTree.upgrade();
        assertTrue(30f==testTree.getHealth());
        assertNotNull("getUpgradeStats() returns null", testTree.getUpgradeStats());
    }

    @Test
    public void initTest() {
        testTree = new ProjectileTree(10, 10, 0, "real_tree", RELOAD, RANGE, HEALTH);
        initTestCommon();
    }
    @Test
    public void methodTest() {
        testTree = new ProjectileTree(10, 10, 0, "real_tree", RELOAD, RANGE, HEALTH);
        methodTestCommon();
    }
}