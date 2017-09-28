package com.deco2800.potatoes.collisions;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.deco2800.potatoes.managers.GameManager;
import com.deco2800.potatoes.managers.TextureManager;
import com.deco2800.potatoes.renderering.Render3D;

/**
 * A point class that implements CollisionMask.
 * Can be used to check distance or overlaps with other CollisionMask's.
 * Can render to isometric view. TODO
 * Being used by AbstractEntity & descendents for collision
 *          & by PathManger to represent points in a path
 *
 * @author Tazman_Schmidt
 */
public class Point2D implements CollisionMask{
    
    private float x;
    private float y;
    private static final String textureStr = "Point2D_highlight";

    /**
     * Default constructor for the purposes of serialization.
     */
    public Point2D() {
        //Empty constructor because Sonar
    }

    /**
     * Constructs a new point at a given location.
     * 
     * @param x
     *              The X coordinate of the point.
     * @param y
     *              The Y coordinate of the point.
     */
    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Makes a copy of the current Point2D.
     *
     * @return A copy of the current Point2D
     */
    @Override
    public CollisionMask copy() {
        return new Point2D(x, y);
    }

    /**
     * Checks if this collision mask overlaps another collision masks.
     * This function is symmetric.
     * Touching the edge is not considered as overlapping.
     *
     * @param other The other collision mask.
     * @return True iff the collision masks are overlapping.
     */
    @Override
    public boolean overlaps(CollisionMask other) {
        if (other instanceof Point2D) {
            return this.equals(other);
        } else {
            return other.overlaps(this);
        }
    }

    /**
     * Finds the minimum straight-line distance between this collision mask and another collision mask.
     * This function is symmetric.
     *
     * @param other     The other collision mask.
     * @return  The distance. If the collision masks overlap, a negative number is returned.
     */
    @Override
    public float distance(CollisionMask other) {
        if (other instanceof Point2D) {
            Point2D point = (Point2D) other;

            float distX = point.getX() - this.x;
            float distY = point.getY() - this.y;

            // use pythagorean theorem
            return (float) Math.sqrt((double) distX * distX + distY * distY );
        } else {
            return other.distance(this);
        }
    }

    /**
     * Finds the minimum straight-line distance between the edges of this collision mask and the given line.
     * Returns 0 if intersecting.
     *
     * @param x1    The x coord of point 1 of the line
     * @param y1    The y coord of point 1 of the line
     * @param x2    The x coord of point 2 of the line
     * @param y2    The y coord of point 2 of the line
     * @return      The minimum straight-line distance
     */
    @Override
    public float distance(float x1, float y1, float x2, float y2) {
        // don't sqrt anything you don't have to
        float segmentLength = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (Float.compare(segmentLength, 0) == 0) {
            return distance(new Point2D(x1, y1));
        }

        // how far along the line segment is the closest point to us?
        float unclamped = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / segmentLength;
        float clamped = Math.max(0f, Math.min(1f, unclamped));

        return distance(new Point2D(x1 + clamped * (x2 - x1), y1 + clamped * (y2 - y1)));
    }

    /**
     * Renders an X using this shape using an current shapeRenderer
     * @param shapeRenderer a shapeRenderer that has run begin() & setcolour() already
     */
    @Override
    public void renderShape(ShapeRenderer shapeRenderer) {
        //TODO use two shapeRenderer.rectLine();
    }

    /**
     * Renders an X image where this shape is, in the isometric game view
     * @param batch Batch to render outline image onto
     */
    @Override
    public void renderHighlight(SpriteBatch batch) {
        //TODO needs revision
        Texture textureHighlight  = GameManager.get().getManager(TextureManager.class).getTexture(textureStr);

        Vector2 isoPosition = Render3D.worldToScreenCoordinates(x, y, 0);

        int tileWidth = (int) GameManager.get().getWorld().getMap().getProperties().get("tilewidth");
        int tileHeight = (int) GameManager.get().getWorld().getMap().getProperties().get("tileheight");
        // We want to keep the aspect ratio of the image so...
        float aspect = (float) textureHighlight.getWidth() / (float) tileWidth;

        batch.draw(textureHighlight,
                // x, y
                isoPosition.x, isoPosition.y,
                // originX, originY
                tileWidth, tileHeight,
                // width, height
                tileWidth, textureHighlight.getHeight() / aspect,
                // scaleX, scaleY, rotation
                1, 1, 0,
                // srcX, srcY
                0, 0,
                // srcWidth, srcHeight
                textureHighlight.getWidth(), textureHighlight.getHeight(),
                // flipX, flipY
                false, false);
    }

    /**
     * Returns the x coordinate at the centre of the mask.
     *
     * @return Returns the x coordinate.
     */
    @Override
    public float getX() { return this.x; }

    /**
     * Sets the x coordiante at the centre of the mask.
     *
     * @param x The new x coordinate.
     */
    @Override
    public void setX(float x) { this.x = x; }

    /**
     * Returns the y coordinate at the centre of the mask.
     *
     * @return Returns the y coordinate.
     */
    @Override
    public float getY() { return this.y; }

    /**
     * Sets the y coordinate at the centre of the mask.
     *
     * @param y The new y coordinate.
     */
    @Override
    public void setY(float y) { this.y = y; }

    @Override
    public int hashCode() {
        // Start with a non-zero constant prime
        int result = 17;

        // Include a hash for each field.
        result = 31 * result + Float.floatToIntBits(this.x);
        result = 31 * result + Float.floatToIntBits(this.y);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Point2D point2D = (Point2D) o;

        if (Float.compare(point2D.x, x) != 0)
            return false;
        return Float.compare(point2D.y, y) == 0;
    }

    /**
     * Returns the variables of this Point2D in the form:
     * "<x>, <y>"
     *
     * @return This Point2D's parameters
     */
    @Override
    public String toString() {
        return this.x + ", " + this.y;
    }
}
