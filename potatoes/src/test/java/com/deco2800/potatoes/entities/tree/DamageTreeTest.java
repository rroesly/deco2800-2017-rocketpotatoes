package com.deco2800.potatoes.entities.tree;


import com.deco2800.potatoes.entities.trees.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class DamageTreeTest {

    DamageTree defaultDamageTree;
    DamageTree iceTreeDamageTree;
    DamageTree acornTreeDamageTree;
    DamageTree nullTypeDamageTree;
    DamageTree fireTreeDamageTree;

    @Before
    public void setup() {
        defaultDamageTree = new DamageTree();
        defaultDamageTree = new DamageTree(0, 0);
        iceTreeDamageTree = new DamageTree(1, 0, new IceTreeType());
        acornTreeDamageTree = new DamageTree(1, 0, new AcornTreeType());
        fireTreeDamageTree = new DamageTree(1, 0, new FireTreeType());
        nullTypeDamageTree = new DamageTree(2, 0, null);

    }

    @After
    public void tearDown() {
        defaultDamageTree = null;
        iceTreeDamageTree = null;
        acornTreeDamageTree = null;
        fireTreeDamageTree = null;
        nullTypeDamageTree = null;
    }

    /* Test getDmageTreeType method */
    @Test
    public void getDamageTreeTypeTest() {
        assertTrue(iceTreeDamageTree.getDamageTreeType() instanceof IceTreeType);
        assertTrue(acornTreeDamageTree.getDamageTreeType() instanceof AcornTreeType);
        assertTrue(fireTreeDamageTree.getDamageTreeType() instanceof FireTreeType);
        assertTrue(defaultDamageTree.getDamageTreeType() instanceof LightningTreeType);
        assertTrue(nullTypeDamageTree.getDamageTreeType() instanceof LightningTreeType);
        defaultDamageTree.createCopy();
        defaultDamageTree.getName();
        iceTreeDamageTree.getName();
        acornTreeDamageTree.getName();
        fireTreeDamageTree.getName();
        nullTypeDamageTree.getName();

    }
    @Test
    public void otherTreesTest() {
        DefenseTree dTree = new DefenseTree(1,2);
        dTree.createCopy();
        dTree.getAllUpgradeStats();
        CactusTreeType cac = new CactusTreeType();
    }

}
