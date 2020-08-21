package src.main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <b>Board</b> represents a panel that holds a single Tetris game. The board
 * can either be active, allowing the player to control it, or passive, waiting
 * for inputs from the server to update.
 */
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
    private boolean player;

    /**
     * Construct a new board
     * 
     * @param newParent - the Tetris client connected to this board
     * @param p         - a boolean indicating whether this board is to be
     *                  controlled by the player
     */
    public Board(Tetris newParent, boolean p) {
        player = p;
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

    /**
     * Accessor for a tile's width
     * 
     * @return the width of a single tile on the board
     */
    public int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    /**
     * Accessor for a tile's height
     * 
     * @return the height of a single tile on the board
     */
    public int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    /**
     * Accessor for the shape (if any) that occupies a particular tile
     * 
     * @param x - the x coordinate
     * @param y - the y coordinate
     * @return if a shape exists on the tile, return the shape, otherwise return
     *         NoShape
     */
    public Tetromino shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    /**
     * Reset the board to its original empty state
     */
    public void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetromino.NoShape;
        }

        repaint();
    }

    /**
     * Stop the current game
     */
    public void stop() {
        clearBoard();
        currPiece.setShape(Tetromino.NoShape);
        timer.stop();
        isStarted = false;
        statusBar.setText("0");
    }

    /**
     * Add a line to the bottom of the board
     * 
     * @param line - the string representing the line -- X's are FillShapes and .'s
     *             are NoShapes
     */
    public void addLine(String line) {
        if (!checkMove(currPiece, currX, currY - 1)) {
            currY++;
        }

        for (int i = BOARD_HEIGHT - 1; i > 0; --i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                board[i * BOARD_WIDTH + j] = shapeAt(j, i - 1);
            }
        }

        for (int i = 0; i < 10; i++) {
            board[i] = (line.charAt(i) == 'X') ? Tetromino.FillShape : Tetromino.NoShape;
        }
    }

    /**
     * Handle a piece that has reached a point where it can no longer descend
     */
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

    /**
     * Accessor for the current game's score
     * 
     * @return the number of lines that the player has cleared this game
     */
    public int getScore() {
        return numLinesRemoved;
    }

    /**
     * Generate a new piece and spawn it at the top
     */
    public void newPiece() {
        currPiece.setRandomShape();
        currX = BOARD_WIDTH / 2 + 1;
        currY = BOARD_HEIGHT - 1 + currPiece.minY();

        if (!tryMove(currPiece, currX, currY - 1)) {
            currPiece.setShape(Tetromino.NoShape);
            timer.stop();
            isStarted = false;
            statusBar.setText("Game Over");

            if (player)
                parent.clientHandler.lose(numLinesRemoved);
        }
    }

    /**
     * Generate a specific new piece and spawn it at the top
     * 
     * @param p - the piece to generate
     */
    public void newPiece(int p) {
        if (!player) {
            currPiece.setShape(p);
            currX = BOARD_WIDTH / 2 + 1;
            currY = BOARD_HEIGHT - 1 + currPiece.minY();

            if (!tryMove(currPiece, currX, currY - 1)) {
                currPiece.setShape(Tetromino.NoShape);
                timer.stop();
                isStarted = false;
                statusBar.setText("Game Over");

                parent.clientHandler.lose(numLinesRemoved);
            }
        }
    }

    /**
     * Attempt to move the current piece down one square
     */
    private void oneLineDown() {
        if (!tryMove(currPiece, currX, currY - 1))
            pieceDropped();
    }

    /**
     * ActionListener for the board
     * 
     * @param ae - the event that has occurred
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    /**
     * Draw a single tile on the board
     * 
     * @param g     - the graphics editor
     * @param x     - the x coordinate
     * @param y     - the y coordinate
     * @param shape - the shape that occupies this tile
     */
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

    /**
     * Paint the entire board
     * 
     * @param g - the Graphics editor
     */
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

    /**
     * Start the game
     * 
     * @param seed - the seed used to generate pseudo-random pieces
     */
    public void start(long seed) {
        currPiece = new Shape(seed);
        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();
        newPiece();
        timer.start();
    }

    /**
     * Check if a particular move is valid
     * 
     * @param newPiece - the piece used in this move
     * @param newX     - the x coordinate of the piece
     * @param newY     - the y coordinate of the piece
     * @return true if the state of the new piece with its coordinates is valid,
     *         false otherwise
     */
    private boolean checkMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;

            if (shapeAt(x, y) != Tetromino.NoShape)
                return false;
        }
        return true;
    }

    /**
     * Attempt to perform a move
     * 
     * @param newPiece - the piece used in this move
     * @param newX     - the x coordinate of the piece
     * @param newY     - the y coordinate of the piece
     * @return true if the move was successfully performed, false otherwise
     */
    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;

            if (shapeAt(x, y) != Tetromino.NoShape)
                return false;
        }

        newPiece.rand = currPiece.rand;
        currPiece = newPiece;
        currX = newX;
        currY = newY;
        repaint();

        if (player)
            sendBoard();

        return true;
    }

    /**
     * Remove all of the completed lines on the board
     */
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
        if (numFullLines > 1 && player) {
            for (int k = 0; k < numFullLines - 1; k++) {
                parent.clientHandler.send();
            }
        }
    }

    /**
     * Hard-drop the current piece
     */
    private void dropDown() {
        int newY = currY;

        while (newY > 0) {
            if (!tryMove(currPiece, currX, newY - 1))
                break;

            --newY;
        }

        pieceDropped();
    }

    /**
     * Move the current piece according to user input
     * 
     * @param keyCode - the key that the user pressed
     */
    public void movePiece(int keyCode) {
        if (!isStarted || currPiece.getShape() == Tetromino.NoShape)
            return;

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

    /**
     * <b>TetrisKeyAdapter</b> is the KeyAdapter used during a game of Tetris.
     */
    class TetrisKeyAdapter extends KeyAdapter {

        /**
         * Handle a key being pressed by the user
         * 
         * @param ke - the key that the user pressed
         */
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

    /**
     * Import the board from a string
     * 
     * @param str - a string representation of the board
     */
    public void fromString(String str) {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            switch (str.charAt(i)) {
                case '0':
                    board[i] = Tetromino.NoShape;
                    break;
                case '1':
                    board[i] = Tetromino.ZShape;
                    break;
                case '2':
                    board[i] = Tetromino.SShape;
                    break;
                case '3':
                    board[i] = Tetromino.LineShape;
                    break;
                case '4':
                    board[i] = Tetromino.TShape;
                    break;
                case '5':
                    board[i] = Tetromino.SquareShape;
                    break;
                case '6':
                    board[i] = Tetromino.LShape;
                    break;
                case '7':
                    board[i] = Tetromino.LntShape;
                    break;
                default:
                    board[i] = Tetromino.FillShape;
                    break;
            }
        }
    }

    /**
     * Export the board as a string
     * 
     * @return a string representing the current state of the board
     */
    @Override
    public String toString() {
        char[] items = new char[BOARD_HEIGHT * BOARD_WIDTH];
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            if (board[i].equals(Tetromino.NoShape))
                items[i] = '0';
            else if (board[i].equals(Tetromino.ZShape))
                items[i] = '1';
            else if (board[i].equals(Tetromino.SShape))
                items[i] = '2';
            else if (board[i].equals(Tetromino.LineShape))
                items[i] = '3';
            else if (board[i].equals(Tetromino.TShape))
                items[i] = '4';
            else if (board[i].equals(Tetromino.SquareShape))
                items[i] = '5';
            else if (board[i].equals(Tetromino.LShape))
                items[i] = '6';
            else if (board[i].equals(Tetromino.LntShape))
                items[i] = '7';
            else
                items[i] = '8';
        }

        return new String(items);
    }

    /**
     * Send the current state of the board to the server to update on the opponent's
     * right-hand panel
     */
    public void sendBoard() {
        String str = this.toString();
        parent.clientHandler.board(str);
    }
}