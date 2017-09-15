package com.deco2800.potatoes.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertThat;

import static org.junit.Assert.*;

public class MinimumSpanningTreeTest {

    float gameHeight = 100;
    float gameWidth = 100;
    int numberOfNodes = 20;
    float largeWeight = 100;
    ArrayList<Box3D> nodes;
    MinimumSpanningTree tree;

    @Before
    public void setUp() {

        nodes = new ArrayList<>();
        // Create nodes
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Box3D(
                    (float) (Math.random() * gameWidth),      // x coordinate
                    (float) (Math.random() * gameHeight),     // y coordinate
                    0,
                    0,
                    0,
                    0
            ));
        }
        // Create minimum spanning tree
        tree = new MinimumSpanningTree(numberOfNodes);
        // Add vertices to graph
        for (int i = 0; i < numberOfNodes; i++) {
            tree.addVertex(nodes.get(i), i);
        }
        // Add weights to graph
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                if (i == j) {
                    tree.putGraphEntry(largeWeight, i, j);
                    continue;
                }
                Line line = new Line(
                        nodes.get(i).getX(),
                        nodes.get(i).getY(),
                        nodes.get(j).getX(),
                        nodes.get(j).getY()
                );
                tree.putGraphEntry(line.getDistance(), i, j);

            }
        }

    }

    @Test
    public void treeTest() {
        tree.createTree(tree.getVertexList().get(0));
    }

}