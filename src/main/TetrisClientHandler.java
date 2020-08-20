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
                System.out.println(line[0]);
                if (line[0].equals("FAILURE")) {
                    if(line.length > 1 && line[1].equals("INVALID"))
                        JOptionPane.showMessageDialog(null, "Error: Invalid username/password! Must contain only alphanumeric characters!");
                    else if(line.length > 1 && line[1].equals("TAKEN"))
                        JOptionPane.showMessageDialog(null, "Error: Username taken!");
                    else if(line.length > 1 && line[1].equals("INCORRECT"))
                        JOptionPane.showMessageDialog(null, "Error: Incorrect login credentials!");
                    else
                        JOptionPane.showMessageDialog(null, "Error: Could not validate credentials!");
                    // tetris.logged = false;
                }
                if (line[0].equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(null, "Successfully logged in! Welcome, " + line[1]);
                    tetris.logged = true;
                }

                if (line[0].equals("LEADERBOARD")) {
                    // leaderboard stuff here
                }

                if (line[0].equals("MATCH")) {
                    // match found
                    inGame = true;
                }

                if (line[0].equals("SENT")) {
                    // server gives u the line u sent to the opponent
                }

                if (line[0].equals("OPPONENT") && line.length > 1) {
                    // OPPONENT MOVE, PIECE, SEND, LOSE
                    if (line[1].equals("MOVE")) {
                        // opponent pressed key
                    }
                    if (line[1].equals("PIECE")) {
                        // opponent's next piece
                    }
                    if (line[1].equals("SEND")) {
                        // opponent sent line
                    }
                    if (line[1].equals("LOSE")) {
                        // opponent lost
                        inGame = false;
                        int score = 0; // replace score with actual score thanks
                        out.println("WIN " + score);
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) {
    }
}