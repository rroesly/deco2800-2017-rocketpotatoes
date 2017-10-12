package com.deco2800.potatoes.cheats.rust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.TextureManager;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.lwjgl.opengl.Display;

public class Rustyfish {

    private static SpriteBatch batch = new SpriteBatch();
    private static ShapeRenderer sr = new ShapeRenderer();

    private interface RLibrary extends Library {
        RLibrary INSTANCE = (RLibrary) Native.loadLibrary("rustyfish", RLibrary.class);

        void startGame(Callback startDraw, Callback endDraw,
                       Callback updateWindow, Callback isSpacePressed,
                       Callback clearWindow, Callback flushWindow, Callback getWindowInfo,
                       Callback drawSprite, Callback drawLine);
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
     * Places key information inside STRUCT TODO
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
            info.sizeX = Gdx.graphics.getWidth();
            info.sizeY = Gdx.graphics.getHeight();

            //System.out.println(info);
        }
    };

    /**
     * Draw's a given sprite with
     *
     * name
     * x
     * y
     * etc... TODO
     */
    private static Callback drawSprite = new Callback() {
        @SuppressWarnings("unused")
        public void run(RenderObject.ByValue obj) {
            TextureManager m = GameManager.get().getManager(TextureManager.class);
            Texture t = m.getTexture(obj.asset);

            switch (obj.color) {
                case -1:
                    batch.setColor(Color.WHITE);
                    break;
                case 0:
                    batch.setColor(Color.BLACK);
                    break;
                case 1:
                    batch.setColor(Color.RED);
                    break;
                case 2:
                    batch.setColor(Color.BLUE);
                    break;
                case 3:
                    batch.setColor(Color.GREEN);
                    break;
                case 4:
                    batch.setColor(Color.YELLOW);
                    break;
                case 5:
                    batch.setColor(Color.ORANGE);
                    break;

                default:
                    break;
            }

            batch.draw(t,
                    obj.x, Gdx.graphics.getHeight() - t.getHeight() * obj.scale - obj.y,
                    0, 0,
                    t.getWidth(), t.getHeight(), obj.scale, obj.scale, obj.rotation,
                    0, 0, t.getWidth(), t.getHeight(), obj.flipX != 0, obj.flipY != 0);
        }
    };

    private static Callback drawLine = new Callback() {
        @SuppressWarnings("unused")
        public void run(RenderLine.ByValue obj) {

            sr.setColor(Color.WHITE);

            Gdx.gl.glLineWidth(1);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.line(obj.srcX, Gdx.graphics.getHeight() - 3 - obj.srcY, obj.dstX, Gdx.graphics.getHeight() - 3 - obj.dstY);
            sr.end();
        }
    };

    public static void run() {
        RLibrary.INSTANCE.startGame(startDraw, endDraw, updateWindow, isSpacePressed, clearWindow, flushWindow, getWindowInfo, drawSprite, drawLine);
    }
}
