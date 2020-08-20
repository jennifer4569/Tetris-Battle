package src.main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// import main.*;

public class Board extends JPanel implements ActionListener {

    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22;
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private int numLinesRemoved = 0;
    private int currX = 0;
    private int currY = 0;
    private JLabel statusBar;
    private Shape currPiece;
    private Tetris parent;
    private Tetromino[] board;

    public Board(Tetris newParent, boolean player) {
        setFocusable(true);
        timer = new Timer(400, this); // timer for lines down
        currPiece = new Shape(0);
        parent = newParent;
        statusBar = newParent.getStatusBar();
        board = new Tetromino[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();
        if (player)
            addKeyListener(new TetrisKeyAdapter());
    }

    public int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    public int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    public Tetromino shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    public void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetromino.NoShape;
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = currX + currPiece.x(i);
            int y = currY - currPiece.y(i);
            board[y * BOARD_WIDTH + x] = currPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    public int getScore() {
        return numLinesRemoved;
    }

    public void newPiece() {
        int x = currPiece.setRandomShape();
        currX = BOARD_WIDTH / 2 + 1;
        currY = BOARD_HEIGHT - 1 + currPiece.minY();

        parent.clientHandler.piece(x);

        if (!tryMove(currPiece, currX, currY - 1)) {
            currPiece.setShape(Tetromino.NoShape);
            timer.stop();
            isStarted = false;
            statusBar.setText("Game Over");

            parent.clientHandler.lose(numLinesRemoved);
        }
    }

    private void oneLineDown() {
        if (!tryMove(currPiece, currX, currY - 1))
            pieceDropped();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetromino shape) {
        Color color = shape.color;
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetromino shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetromino.NoShape) {
                    drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
                }
            }
        }

        if (currPiece.getShape() != Tetromino.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = currX + currPiece.x(i);
                int y = currY - currPiece.y(i);
                drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        currPiece.getShape());
            }
        }
    }

    public void start(long seed) {
        currPiece = new Shape(seed);
        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();
        newPiece();
        timer.start();
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;

            if (shapeAt(x, y) != Tetromino.NoShape)
                return false;
        }

        currPiece = newPiece;
        currX = newX;
        currY = newY;
        repaint();

        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetromino.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                ++numFullLines;

                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                    }
                }
            }

            if (numFullLines > 0) {
                numLinesRemoved += numFullLines;
                statusBar.setText(String.valueOf(numLinesRemoved));
                isFallingFinished = true;
                currPiece.setShape(Tetromino.NoShape);
                repaint();
            }
        }
    }

    private void dropDown() {
        int newY = currY;

        while (newY > 0) {
            if (!tryMove(currPiece, currX, newY - 1))
                break;

            --newY;
        }

        pieceDropped();
    }

    class TetrisKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent ke) {
            if (!isStarted || currPiece.getShape() == Tetromino.NoShape)
                return;

            int keyCode = ke.getKeyCode();
            parent.clientHandler.move(keyCode);

            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                    tryMove(currPiece, currX - 1, currY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(currPiece, currX + 1, currY);
                    break;
                case KeyEvent.VK_DOWN:
                    tryMove(currPiece.rotateRight(), currX, currY);
                    break;
                case KeyEvent.VK_UP:
                    tryMove(currPiece.rotateLeft(), currX, currY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case 'd':
                case 'D':
                    oneLineDown();
                    break;
            }

        }
    }

}