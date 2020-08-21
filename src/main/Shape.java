package src.main;

import java.util.*;

import java.awt.*;

/**
 * <b>Tetromino</b> represents the tiles occupied by the various pieces in
 * Tetris. Each tetromino is color-coded, and contains a set of four coordinates
 * that represent their shape, or in the case of the empty square and the filler
 * (which is part of the lines sent by the opponent), simply a set of four <0,
 * 0>'s.
 * 
 * @author Michael Ruvinshteyn
 * @version 1.5
 */
final class Tetromino {

    // the empty square -- has no color or coordinates
    public static Tetromino NoShape = new Tetromino(new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
            new Color(0, 0, 0));

    // the seven shapes of Tetris: Z, S, Line, T, Square, L, and Ln't
    public static Tetromino ZShape = new Tetromino(new int[][] { { 0, -1 }, { 0, 0 }, { -1, 0 }, { -1, 1 } },
            new Color(204, 102, 102));
    public static Tetromino SShape = new Tetromino(new int[][] { { 0, -1 }, { 0, 0 }, { 1, 0 }, { 1, 1 } },
            new Color(102, 204, 102));
    public static Tetromino LineShape = new Tetromino(new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } },
            new Color(102, 102, 204));
    public static Tetromino TShape = new Tetromino(new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } },
            new Color(204, 204, 102));
    public static Tetromino SquareShape = new Tetromino(new int[][] { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },
            new Color(204, 102, 204));
    public static Tetromino LShape = new Tetromino(new int[][] { { -1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } },
            new Color(102, 204, 204));
    public static Tetromino LntShape = new Tetromino(new int[][] { { 1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } },
            new Color(218, 170, 0));

    // the tiles used to fill the lines sent by the opponent
    public static Tetromino FillShape = new Tetromino(new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
            new Color(100, 100, 100));

    // the possible values of a Tetromino
    public static final Tetromino[] values = { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape,
            LntShape, FillShape };

    /**
     * Collect the possible shapes mentioned above and return them in an array
     * 
     * @return an array containing all of the possible static instances of this
     *         class
     */
    public static Tetromino[] values() {
        return values;
    }

    // instance variables -- this class is used more as a struct, which is why they
    // are public
    public int[][] coords;
    public Color color;

    /**
     * private constructor used only by the instances above
     * 
     * @param coords
     * @param c
     */
    private Tetromino(int[][] coords, Color c) {
        this.coords = coords;
        color = c;
    }
}

/**
 * <b>Shape</b> is a representation of a single piece moving on the Tetris
 * board. The piece possesses one of the seven possible shapes instanced in the
 * <b>Tetromino</b> class.
 */
public class Shape {
    private Tetromino pieceShape;
    private int[][] coords;
    public Random rand;

    /**
     * Construct the shape
     * 
     * @param seed - the seed used to randomize the shape's next form(s)
     */
    public Shape(long seed) {
        coords = new int[4][2];
        setShape(Tetromino.NoShape);
        rand = new Random(seed);
    }

    /**
     * Set the form of the current shape to match one of the seven Tetrominoes
     * 
     * @param shape - the variant that this shape will become
     */
    public void setShape(Tetromino shape) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = shape.coords[i][j];
            }
        }

        pieceShape = shape;
    }

    /**
     * Modifier for the x-coordinate of one of this shape's tiles
     * 
     * @param index - the index of the tile to modify
     * @param x     - the new x-coordinate of the tile
     */
    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    /**
     * Modifier for the y-coordinate of one of this shape's tiles
     * 
     * @param index - the index of the tile to modify
     * @param y     - the new y-coordinate of the tile
     */
    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    /**
     * Accessor for the x-coordinate of one of this shape's tiles
     * 
     * @param index - the tile to access
     * @return the x-coordinate of the tile
     */
    public int x(int index) {
        return coords[index][0];
    }

    /**
     * Accessor for the y-coordinate of one of this shape's tiles
     * 
     * @param index - the tile to access
     * @return the y-coordinate of the tile
     */
    public int y(int index) {
        return coords[index][1];
    }

    /**
     * Accessor for the type of shape this instance is
     * 
     * @return the Tetromino corresponding to this shape's form
     */
    public Tetromino getShape() {
        return pieceShape;
    }

    /**
     * Set the form of this shape to a random Tetromino
     * 
     * @return the index of the new Tetromino
     */
    public int setRandomShape() {
        int x = Math.abs(rand.nextInt()) % 7 + 1;
        Tetromino[] values = Tetromino.values();
        setShape(values[x]);
        return x;
    }

    /**
     * Set the form of this shape to a specified Tetromino
     * 
     * @param x - the index of the Tetromino to set this shape to
     */
    public void setShape(int x) {
        Tetromino[] values = Tetromino.values();
        setShape(values[x]);
    }

    /**
     * Accessor for the left-most x-coordinate of this shape
     * 
     * @return the lowest x-coordinate among the four tiles
     */
    public int minX() {
        int m = coords[0][0];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][0]);
        }

        return m;
    }

    /**
     * Accessor for the lowest y-coordinate of this shape
     * 
     * @return the lowest y-coordinate among the four tiles
     */
    public int minY() {
        int m = coords[0][1];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }

        return m;
    }

    /**
     * Rotate this shape counter-clockwise
     * 
     * @return the new Shape representing the rotated piece
     */
    public Shape rotateLeft() {
        if (pieceShape == Tetromino.SquareShape)
            return this;

        Shape result = new Shape(0);
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; i++) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }

        return result;
    }

    /**
     * Rotate this shape clockwise
     * 
     * @return the new Shapre representing the rotated piece
     */
    public Shape rotateRight() {
        if (pieceShape == Tetromino.SquareShape)
            return this;

        Shape result = new Shape(0);
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; i++) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }

        return result;
    }

}