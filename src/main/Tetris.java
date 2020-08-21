package src.main;

import javax.swing.*;
import java.awt.*;
import java.net.*;

/**
 * <b>Tetris</b> represents the application that the user runs in order to play
 * Tetris Battle. The application hosts two <b>Board</b>s and a series of
 * controls that allows the user to register, login, connect to another player,
 * or view their stats and leaderboard status.
 */
public class Tetris extends JFrame {

    private JLabel statusBar;
    protected TetrisClientHandler clientHandler;
    protected boolean logged, queue, ingame;
    protected int numWins;
    protected int numGames;
    protected int highScore;
    protected Board board, oppBoard;

    /**
     * Iniitialize the toolbar and set it as this application's toolbar.
     */
    private void initToolBar() {
        JToolBar toolBar = new JToolBar();

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener((event) -> {
            if (logged) {
                JOptionPane.showMessageDialog(null, "You are already logged in!");
                return;
            }
            JTextField field1 = new JTextField();
            JTextField field2 = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Username:"));
            panel.add(field1);
            panel.add(new JLabel("Password:"));
            panel.add(field2);
            int result = JOptionPane.showConfirmDialog(null, panel, "Register", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                clientHandler.register(field1.getText(), field2.getText());
            } else {
                System.out.println("Cancelled");
            }
        });
        toolBar.add(registerButton);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener((event) -> {
            if (logged) {
                JOptionPane.showMessageDialog(null, "You are already logged in!");
                return;
            }
            JTextField field1 = new JTextField();
            JTextField field2 = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Username:"));
            panel.add(field1);
            panel.add(new JLabel("Password:"));
            panel.add(field2);
            int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                clientHandler.login(field1.getText(), field2.getText());
            } else {
                System.out.println("Cancelled");
            }
        });
        toolBar.add(loginButton);

        JButton playButton = new JButton("Play");
        playButton.addActionListener((event) -> {
            if (queue || ingame) {
                JOptionPane.showMessageDialog(null, "Cannot search for game at this time!");
                return;
            }
            if (logged) {
                clientHandler.play();
                JOptionPane.showMessageDialog(null, "You have been placed in the queue!");
            } else {
                JOptionPane.showMessageDialog(null, "You must be logged in to play!");
            }
        });
        toolBar.add(playButton);

        JButton leaderboardButton = new JButton("Leaderboard");
        leaderboardButton.addActionListener((event) -> {
            clientHandler.leaderboard();
        });
        toolBar.add(leaderboardButton);

        JButton statsButton = new JButton("Stats");
        statsButton.addActionListener((event) -> {
            // message dialog with stats goes here
            if (!logged)
                JOptionPane.showMessageDialog(null, "You must be logged in to see your stats!");
            else {
                String msg = String.format(
                        "Win Rate: %d%%\nNumber of Games Won: %d\nTotal Games Played: %d\nHigh Score: %d\n",
                        100 * numWins / numGames, numWins, numGames, highScore);
                JOptionPane.showMessageDialog(null, msg, "Stats", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        toolBar.add(statsButton);

        registerButton.setFocusable(false);
        loginButton.setFocusable(false);
        playButton.setFocusable(false);
        leaderboardButton.setFocusable(false);
        statsButton.setFocusable(false);

        add(toolBar, BorderLayout.NORTH);
    }

    /**
     * Construct the application by initializing the instance variables and
     * instantiating the GUI components
     */
    public Tetris() {

        logged = false;
        queue = false;
        ingame = false;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        try {
            String server = "67.205.133.16";
            int port = 8080;

            Socket socket = new Socket(server, port);

            clientHandler = new TetrisClientHandler(socket, this);
            Thread t = new Thread(clientHandler);
            t.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        initToolBar();

        statusBar = new JLabel("0");
        add(statusBar, BorderLayout.SOUTH);

        JPanel panel = new JPanel();

        board = new Board(this, true);
        panel.add(board);
        // board.start();

        oppBoard = new Board(this, false);
        oppBoard.setBackground(Color.LIGHT_GRAY);
        panel.add(oppBoard);

        panel.setLayout(new GridLayout(1, 0));

        add(panel);

        setTitle("My Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Return the label that represents the status of the current game
     * 
     * @return the JLabel representing the status of the game
     */
    public JLabel getStatusBar() {
        return statusBar;
    }

    /**
     * Get the score of the current game
     * 
     * @return the amount of lines that the player has cleared this game
     */
    public int getScore() {
        return board.getScore();
    }

    /**
     * Start the game
     * 
     * @param seed - the seed used for the random shape generator
     */
    public void startGame(long seed) {
        board.start(seed);
        oppBoard.start(seed);
    }

    /**
     * Driver code for the program
     * 
     * @param args - command line arguments (unused)
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Tetris myTetris = new Tetris();
                myTetris.setSize(750, 750);
                myTetris.setLocation(0, 0);
                myTetris.setResizable(false);
                myTetris.setVisible(true);
            }
        });
    }

}