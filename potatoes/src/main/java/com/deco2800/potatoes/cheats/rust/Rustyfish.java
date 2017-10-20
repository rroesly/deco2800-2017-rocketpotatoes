package com.deco2800.potatoes.cheats.rust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.deco2800.potatoes.cheats.CheatExecution;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.TextureManager;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.lwjgl.opengl.Display;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.*;

public class Rustyfish implements CheatExecution {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Rustyfish.class);
    private static SpriteBatch batch = new SpriteBatch();
    private static ShapeRenderer sr = new ShapeRenderer();

    private interface RLibrary extends Library {
        RLibrary INSTANCE = null;

        void startGame(Callback startDraw, Callback endDraw,
                       Callback updateWindow, Callback isSpacePressed,
                       Callback clearWindow, Callback flushWindow, Callback getWindowInfo,
                       Callback drawSprite, Callback drawLine, Callback drawRectangle);
    }

    private static Color getColor(int color) {
        switch (color) {
            case -1:
                return Color.WHITE;
            case 0:
                return Color.BLACK;
            case 1:
                return Color.RED;
            case 2:
                return Color.BLUE;
            case 3:
                return new Color(0, 0, 0.8f, 1.0f);
            case 4:
                return Color.GREEN;
            case 5:
                return Color.YELLOW;
            case 6:
                return Color.ORANGE;

            default:
                return new Color();
        }
    }


    /**
     * Starts a draw batch
     */
    private static Callback startDraw = new Callback() {
        @SuppressWarnings("unused")
        public void run() {
            batch.begin();
        }
    };

    /**
     * Ends the current draw batch
     */
    private static Callback endDraw = new Callback() {
        @SuppressWarnings("unused")
        public void run() {
            batch.end();
        }
    };

    /**
     * Updates the window, checking for resize events, key events etc.
     *
     */
    private static Callback updateWindow = new Callback() {
        @SuppressWarnings("unused")
        public boolean run() {
            Display.update(true);
            int w = (int)(Display.getWidth() * Display.getPixelScaleFactor());
            int h = (int)(Display.getHeight() * Display.getPixelScaleFactor());
            Gdx.gl.glViewport(0, 0, w, h);
            batch = new SpriteBatch();
            sr = new ShapeRenderer();
            Gdx.graphics.setTitle("Rustyfish");

            return true;
        }
    };

    /**
     * Returns true if space is pressed
     */
    private static Callback isSpacePressed = new Callback() {
        @SuppressWarnings("unused")
        public boolean run() {
            return Gdx.input.isKeyPressed(Input.Keys.SPACE);
        }
    };

    /**
     * Clears the window with default black color
     */
    private static Callback clearWindow = new Callback() {
        @SuppressWarnings("unused")
        public void run() {
            Gdx.gl.glClearColor(0, 0.4f, 0.8f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
    };

    /**
     * Flushes the window (swaps backbuffers)
     */
    private static Callback flushWindow = new Callback() {
        @SuppressWarnings("unused")
        public void run() {
            Gdx.gl.glFlush();
        }
    };

    /**
     * Get's the window information and places it inside the info object
     */
    private static Callback getWindowInfo = new Callback() {
        @SuppressWarnings("unused")
        public void run(RenderInfo.ByReference info) {
            info.setSizeX(Gdx.graphics.getWidth());
            info.setSizeY(Gdx.graphics.getHeight());
        }
    };

    /**
     * Draw's a given sprite
     */
    private static Callback drawSprite = new Callback() {
        @SuppressWarnings("unused")
        public void run(RenderObject.ByValue obj) {
            TextureManager m = GameManager.get().getManager(TextureManager.class);
            Texture t = m.getTexture(obj.getAsset());

            batch.setColor(getColor(obj.getColor()));

            batch.draw(t,
                    obj.getX(), Gdx.graphics.getHeight() - t.getHeight() * obj.getScale() - obj.getY(),
                    0, 0,
                    t.getWidth(), t.getHeight(), obj.getScale(), obj.getScale(), obj.getRotation(),
                    0, 0, t.getWidth(), t.getHeight(), obj.getFlipX() != 0, obj.getFlipY() != 0);
        }
    };

    private static Callback drawLine = new Callback() {
        @SuppressWarnings("unused")
        public void run(RenderLine.ByValue obj) {

            sr.setColor(Color.WHITE);

            Gdx.gl.glLineWidth(1);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.line(obj.getSrcX(), Gdx.graphics.getHeight() - 3 - obj.getSrcY(), obj.getDstX(), Gdx.graphics.getHeight() - 3 - obj.getDstY());
            sr.end();
        }
    };

    private static Callback drawRectangle = new Callback() {
        @SuppressWarnings("unused")
        public void run(RenderRectangle.ByValue obj) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            Color c = getColor(obj.getColor());
            c.a = obj.getAlpha();
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.rect(obj.getX(), Gdx.graphics.getHeight() - obj.getH() - obj.getY(), obj.getW(), obj.getH(), c, c, c, c);
            sr.end();

            glDisable(GL_BLEND);
        }
    };

    /**
     * Runs game!
     */
    @Override
    public void run() {
        try {
            Native.loadLibrary("rustyfish", RLibrary.class).startGame(startDraw, endDraw, updateWindow, isSpacePressed, clearWindow, flushWindow,
                    getWindowInfo, drawSprite, drawLine, drawRectangle);
        }
        catch (UnsatisfiedLinkError ex) {
            // Ignore failure, don't start game!
            LOGGER.error("Unsatified Link Error occured",ex);
            System.exit(-1);
        }
    }
}
