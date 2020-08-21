package src.main;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/** 
 * <b>TetrisClientHandler</b> is the client handler for the Tetris app.
 * It connects with the TetrisServer, which is assumed to be running.
 * @author Jennifer Zhang
 * @version 3.5
 */
public class TetrisClientHandler implements Runnable {

    private Socket socket;    /** The socket connected to the server */
    private Tetris tetris;    /** The reference to the Tetris object */

    private boolean inGame;   /** Boolean to check if the user is currently in game */

    private PrintWriter out;  /** The output stream */

    /**
     * Creates an instance of the TetrisClientHandler 
     * @param sock The socket connected to the server
     * @param t The reference to the Tetris object
     */
    public TetrisClientHandler(Socket sock, Tetris t) {
        socket = sock;
        tetris = t;
        inGame = false;
    }

    /**
     * Sends the server the request to register a new user
     * @param username The username for the requested user to register
     * @param password The password for the requested user to register
     */
    public void register(String username, String password) {
        out.println("REGISTER " + username + " " + password.hashCode());
    }

    /**
     * Sends the server the request to login with the given credentials
     * @param username The username for the login credentials
     * @param password The password for the login credentials
     */
    public void login(String username, String password) {
        out.println("LOGIN " + username + " " + password.hashCode());
    }

    /**
     * Sends the server the request to get the leaderboard (of the top 10)
     */
    public void leaderboard() {
        out.println("LEADERBOARD");
    }

    /**
     * Sends the server the request to get the leaderboard (of the top n)
     * @param n The top n players to retrieve
     */
    public void leaderboard(int n) {
        out.println("LEADERBOARD " + n);
    }

    /**
     * Sends the server the request to play. The server will matchmake you to your
     * opponent
     */
    public void play() {
        out.println("PLAY");
    }

    /**
     * Sends the server the keyCode of the key the user pressed.
     * @param keyCode The keyCode of the key the user pressed.
     */
    public void move(int keyCode) {
        out.println("MOVE " + keyCode);
    }

    /**
     * Sends the server the String representation of the user's board.
     * @param board The String representation of the user's board
     */
    public void board(String board) {
        out.println("BOARD " + board);
    }

    /**
     * Sends the server the request to send the opponent a line
     */
    public void send() {
        out.println("SEND");
    }

    /**
     * Sends the server that the user has lost
     * @param score The user's score for this current game.
     */
    public void lose(int score) {
        out.println("LOSE " + score);
        inGame = false;
        tetris.board.stop();
        tetris.oppBoard.stop();
        JOptionPane.showMessageDialog(null, "You lost!");
    }

    /**
     * The run function for the TetrisClientHandler, which handles a the connection to the server
     */
    public void run() {
        try{
            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();

            Scanner in = new Scanner(inStream);
            out = new PrintWriter(outStream, true); // autoflush

            while (in.hasNextLine()) {
                String[] line = in.nextLine().split(" ");
                if (line.length == 0)
                    continue;
                    
                //if received "FAILURE" from the server, this is from an invalid register/login attempt
                if (line[0].equals("FAILURE")) {
                    if(line.length > 1 && line[1].equals("INVALID"))
                        JOptionPane.showMessageDialog(null, "Error: Invalid username/password! Must contain 5-20 characters, and only contain alphanumeric characters!");
                    else if(line.length > 1 && line[1].equals("TAKEN"))
                        JOptionPane.showMessageDialog(null, "Error: Username taken!");
                    else if(line.length > 1 && line[1].equals("INCORRECT"))
                        JOptionPane.showMessageDialog(null, "Error: Incorrect login credentials!");
                    else if(line.length > 1 && line[1].equals("LOGGED"))
                        JOptionPane.showMessageDialog(null, "Error: User already logged in!");
                    else
                        JOptionPane.showMessageDialog(null, "Error: Could not validate credentials!");
                }
                //if received "SUCCESS" from the server, this means that the user has successfully registered/logged in
                if (line[0].equals("SUCCESS")) {
                    tetris.numWins = Integer.parseInt(line[2]);
                    tetris.numGames = Integer.parseInt(line[3]);
                    tetris.highScore = Integer.parseInt(line[4]);
                    JOptionPane.showMessageDialog(null, "Successfully logged in! Welcome, " + line[1]);
                    tetris.logged = true;
                }

                //if received "LEADERBOARD" from the server, this means that the client's request for the leaderboard was
                //finished
                if (line[0].equals("LEADERBOARD")) {
                    String msg = "";
                    for(int i = 1; i < line.length; i++)
                        msg+= String.format("%3d. %s\n", i, line[i]);
                    JOptionPane.showMessageDialog(null, msg, "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
                }

                //if received "MATCH" from the server, this means that the server has successfully matched the client with
                //another player
                if (line[0].equals("MATCH")) {
                    inGame = true;
                    tetris.startGame(Long.parseLong(line[5]));
                    JOptionPane.showMessageDialog(null, "Opponent found: " + line[1]);
                }

                //if received "SENT" from the server, this means that the server has generated the line you requested to send
                //to your opponent
                if (line[0].equals("SENT"))
                    tetris.oppBoard.addLine(line[1]);


                //if received "OPPONENT" from the server, we are getting information about the opponent
                if (line[0].equals("OPPONENT") && line.length > 1) {

                    //if received "OPPONENT MOVE" from the server, the opponent has moved their piece
                    if (line[1].equals("MOVE"))
                        tetris.oppBoard.movePiece(Integer.parseInt(line[2]));
        
                    //if received "OPPONENT SEND" from the server, the opponent has sent you a line
                    if (line[1].equals("SEND")) 
                        tetris.board.addLine(line[2]);
        
                    //if received "OPPONENT LOSE" from the server, the opponent has lost
                    if (line[1].equals("LOSE")) {
                        inGame = false;
                        int score = tetris.board.getScore();
                        out.println("WIN " + score);
                        tetris.board.stop();
                        tetris.oppBoard.stop();
                        JOptionPane.showMessageDialog(null, "You won!");
                    }
                }
                //if received "BOARD" from the server, then fromString the sent board
                if (line[0].equals("BOARD")) {
                    tetris.oppBoard.fromString(line[1]);
                }

            }
        } 
        catch (IOException e) {
        }
    }
}