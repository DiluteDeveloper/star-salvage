package starsalvage.engine;

import java.io.Serializable;
import java.util.Objects;

/**
 * Two component integer vector class
 */
public class Vec2 implements Serializable {
    /**
     * Vector components X and Y
     */
    public int x, y;

    /**
     * Initialize X and Y
     * @param x value of X
     * @param y value of Y
     */
    public Vec2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Initialize X and Y to the same value
     * @param v value of X and Y
     */
    public Vec2(int v) {
        this.x = v;
        this.y = v;
    }
    /**
     * Copy constructor to copy values
     * @param other Vec2 to copy values from
     */
    public Vec2(Vec2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Perform component addition on two Vec2's
     * @param lhs Left hand side Vec2
     * @param rhs Right hand side Vec2
     * @return Sum of Vec2's
     */
    public static Vec2 add(Vec2 lhs, Vec2 rhs) {
        return new Vec2(lhs.x + rhs.x, lhs.y + rhs.y);
    }
    /**
     * Multiply a Vec2 by a scalar integer
     * @param lhs Vec2 to scale
     * @param rhs Scalar integer
     * @return Vec2 multiplied by scalar
     */
    public static Vec2 mulScalar(Vec2 lhs, int rhs) { return new Vec2(lhs.x * rhs, lhs.y * rhs ); }

    @Override
    /**
     * Compare if two Vec2's have the same X and Y;
     * == operator DOES NOT WORK unless both Vec2's are
     * referencing the same class instance; operator overloading
     * is not a feature in Java
     * @param o Vec2 to compare with this Vec2
     * @return true if Vec2's have the same component value,
     * otherwise false
     */
    public boolean equals(Object o) {
        if (!(o instanceof Vec2 v)) return false;
        return x == v.x && y == v.y;
    }

    /**
     * Used for HashMap
     * @return the hash of x and y
     */
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
