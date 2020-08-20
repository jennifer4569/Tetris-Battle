package src.main;

import java.util.*;

import java.awt.*;

final class Tetromino {
    public static Tetromino NoShape = new Tetromino(new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
            new Color(0, 0, 0));
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
    public static Tetromino FillShape = new Tetromino(new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
            new Color(100, 100, 100));

    public static final Tetromino[] values = { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape,
            LntShape, FillShape };

    public static Tetromino[] values() {
        return values;
    }

    public int[][] coords;
    public Color color;

    private Tetromino(int[][] coords, Color c) {
        this.coords = coords;
        color = c;
    }
}

public class Shape {
    private Tetromino pieceShape;
    private int[][] coords;
    public Random rand;

    public Shape(long seed) {
        coords = new int[4][2];
        setShape(Tetromino.NoShape);
        rand = new Random(seed);
    }

    public void setShape(Tetromino shape) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = shape.coords[i][j];
            }
        }

        pieceShape = shape;
    }

    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    public int x(int index) {
        return coords[index][0];
    }

    public int y(int index) {
        return coords[index][1];
    }

    public Tetromino getShape() {
        return pieceShape;
    }

    public int setRandomShape() {
        int x = Math.abs(rand.nextInt()) % 7 + 1;
        Tetromino[] values = Tetromino.values();
        setShape(values[x]);
        return x;
    }

    public void setShape(int x) {
        Tetromino[] values = Tetromino.values();
        setShape(values[x]);
    }

    public int minX() {
        int m = coords[0][0];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][0]);
        }

        return m;
    }

    public int minY() {
        int m = coords[0][1];

        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }

        return m;
    }

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