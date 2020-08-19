package src.main;

import javax.swing.*;
import java.awt.*;
import java.net.*;

public class Tetris extends JFrame {

  private JLabel statusBar;
  protected TetrisClientHandler clientHandler;

  public Tetris() {
    try {
      String server = "localhost";
      int port = 8080;

      Socket socket = new Socket(server, port);

      clientHandler = new TetrisClientHandler(socket);
      Thread t = new Thread(clientHandler);
      t.start();

    } catch (Exception e) {
      e.printStackTrace();
    }

    statusBar = new JLabel("0");
    add(statusBar, BorderLayout.SOUTH);

    JPanel panel = new JPanel();

    Board board = new Board(this, true);
    panel.add(board);
    board.start();

    Board oppBoard = new Board(this, false);
    oppBoard.setBackground(Color.LIGHT_GRAY);
    panel.add(oppBoard);

    panel.setLayout(new GridLayout(1, 0));

    add(panel);

    setTitle("My Tetris");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public JLabel getStatusBar() {
    return statusBar;
  }

  public static void main(String[] args) {

    Tetris myTetris = new Tetris();
    myTetris.setSize(750, 750);
    myTetris.setLocation(0, 0);
    myTetris.setVisible(true);

  }

}