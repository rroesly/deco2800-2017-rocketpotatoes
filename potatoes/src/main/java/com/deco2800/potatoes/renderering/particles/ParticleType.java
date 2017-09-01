package com.deco2800.potatoes.renderering.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;

public class ParticleType {
    public Color color;
    public int sizeX, sizeY;
    public int number, rate;
    public float lifeTime;
    public float cycleDelta;
    public float currentCycleTime;
    public float rotationSpeed = 1.0f;
    public float alphaCeil = 0.8f;
    public float fadeOutPercent = 1.0f;

    // Not functional yet. Will implement when/if needed
    public float fadeInPercent = 0.1f;
    public Texture texture;

    // List of active particles of this type
    List<Particle> particles;

    /**
     * Creates a square particle of the given color.
     * @param number maximum number of particles of this type to be produced.
     * @param lifeTime lifetime of a single particle (in ms)
     * @param cycleDelta how long till a cycle (i.e. when we emit particles). (in ms)
     * @param rate how many particle should be produced on a cycle
     * @param color color of this particle
     * @param sizeX size of the particle in pixels
     * @param sizeY size of the particle in pixels
     */
    public ParticleType(int number, float lifeTime, float cycleDelta, int rate, Color color, int sizeX, int sizeY) {
        this.number = number;
        this.lifeTime = lifeTime;
        this.cycleDelta = cycleDelta;
        this.rate = rate;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.color = color;
        particles = new ArrayList<>();
    }

    /**
     * Creates a particle with the given texture
     * @param number maximum number of particles to produce
     * @param lifeTime lifetime of a single particle (in ms)
     * @param cycleDelta how long till a cycle (i.e. when we emit particles). (in ms)
     * @param rate the rate particles should be produced per cycle
     * @param texture texture to use for the particle
     */
    public ParticleType(int number, float lifeTime, float cycleDelta, int rate, Texture texture) {
        this.number = number;
        this.lifeTime = lifeTime;
        this.cycleDelta = cycleDelta;
        this.rate = rate;
        particles = new ArrayList<>();
        this.texture = texture;
    }

    /**
     * Removes all particles from being tracked
     */
    public void cleanup() {
        for (Particle particle : particles) {
            particle.alive = false;
        }
        particles.clear();
    }
}
