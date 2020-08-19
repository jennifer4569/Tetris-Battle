package src.main;

import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

  private JLabel statusBar;

  public Tetris() {
    statusBar = new JLabel("0");
    add(statusBar, BorderLayout.SOUTH);
    Board board = new Board(this);
    add(board);
    board.start();
    setTitle("My Tetris");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public JLabel getStatusBar() {
    return statusBar;
  }

  public static void main(String[] args) {
    Tetris myTetris = new Tetris();
    myTetris.setSize(100, 750);
    myTetris.setLocation(0, 0);
    myTetris.setVisible(true);
  }

}