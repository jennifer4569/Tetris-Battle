package src.main;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
public class TetrisClientHandler implements Runnable {
    Socket socket;
    PrintWriter out;

    Tetris tetris;
    boolean inGame;

    public TetrisClientHandler(Socket s, Tetris t) {
        socket = s;
        tetris = t;
        inGame = false;
    }

    public void register(String username, String password) {
        out.println("REGISTER " + username + " " + password.hashCode());
    }

    public void login(String username, String password) {
        out.println("LOGIN " + username + " " + password.hashCode());
    }

    public void leaderboard() {
        out.println("LEADERBOARD");
    }

    public void leaderboard(int n) {
        out.println("LEADERBOARD " + n);
    }

    public void play() {
        out.println("PLAY");
    }

    public void move(int keyCode) {
        out.println("MOVE " + keyCode);
    }

    public void piece(int piece) {
        out.println("PIECE " + piece);
    }

    public void send() {
        out.println("SEND");
    }

    public void lose(int score) {
        out.println("LOSE " + score);
        inGame = false;
        JOptionPane.showMessageDialog(null, "You lost!");
        tetris.board.stop();
        tetris.oppBoard.stop();
    }

    public void run() {
        try {

            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();

            Scanner in = new Scanner(inStream);
            out = new PrintWriter(outStream, true); // autoflush
            while (in.hasNextLine()) {
                String[] line = in.nextLine().split(" ");
                if (line.length == 0)
                    continue;
                if(!line[0].equals("OPPONENT"))
                System.out.println(line[0]);
                if (line[0].equals("FAILURE")) {
                    if(line.length > 1 && line[1].equals("INVALID"))
                        JOptionPane.showMessageDialog(null, "Error: Invalid username/password! Must contain only alphanumeric characters!");
                    else if(line.length > 1 && line[1].equals("TAKEN"))
                        JOptionPane.showMessageDialog(null, "Error: Username taken!");
                    else if(line.length > 1 && line[1].equals("INCORRECT"))
                        JOptionPane.showMessageDialog(null, "Error: Incorrect login credentials!");
                    else if(line.length > 1 && line[1].equals("LOGGED"))
                        JOptionPane.showMessageDialog(null, "Error: User already logged in!");
                    else
                        JOptionPane.showMessageDialog(null, "Error: Could not validate credentials!");
                    // tetris.logged = false;
                }
                if (line[0].equals("SUCCESS")) {
                    //line[2], line[3], line[4] are stats
                    tetris.numWins = Integer.parseInt(line[2]);
                    tetris.numGames = Integer.parseInt(line[3]);
                    tetris.highScore = Integer.parseInt(line[4]);
                    JOptionPane.showMessageDialog(null, "Successfully logged in! Welcome, " + line[1]);
                    tetris.logged = true;
                }

                if (line[0].equals("LEADERBOARD")) {
                    // leaderboard stuff here
                    String msg = "";
                    for(int i = 1; i < line.length; i++){
                        // String user = line[i].substring(0, line[i].indexOf(','));
                        // int score = Integer.parseInt(line[i].substring(line[i].indexOf(',')+1));
                        // msg += String.format("%3d. %s %10d\n", i, user, score);
                        msg+= String.format("%3d. %s\n", i, line[i]);
                    }
                    JOptionPane.showMessageDialog(null, msg, "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
                }

                if (line[0].equals("MATCH")) {
                    // match found
                    inGame = true;
                    System.out.println("SEED: " + line[5]);
                    tetris.startGame(Long.parseLong(line[5]));
                    JOptionPane.showMessageDialog(null, "Opponent found: " + line[1]);
                    //seed = line[5]
                }

                if (line[0].equals("SENT")) {
                    // server gives u the line u sent to the opponent
                    System.out.println("u sent line");
                    tetris.oppBoard.addLine(line[1]);
                }

                if (line[0].equals("OPPONENT") && line.length > 1) {
                    // OPPONENT MOVE, PIECE, SEND, LOSE
                    if (line[1].equals("MOVE")) {
                        // opponent pressed key
                        tetris.oppBoard.movePiece(Integer.parseInt(line[2]));
                    }
                    else{
                        System.out.println("OPPONENT " + line[1]);
                    }
                    if (line[1].equals("PIECE")) {
                        // opponent's next piece
                        // tetris.oppBoard.newPiece(Integer.parseInt(line[2]));
                    }
                    if (line[1].equals("SEND")) {
                        // opponent sent line
                        System.out.println("opponent sent line");
                        tetris.board.addLine(line[2]);
                    }
                    if (line[1].equals("LOSE")) {
                        // opponent lost
                        inGame = false;
                        int score = tetris.board.getScore();
                        out.println("WIN " + score);
                        JOptionPane.showMessageDialog(null, "You won!");
                        tetris.board.stop();
                        tetris.oppBoard.stop();
                    }
                }
            }
        } catch (IOException e) {
        }
    }
}