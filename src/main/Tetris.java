package src.main;

import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

  private JLabel statusBar;

  public Tetris() {
    statusBar = new JLabel("0");
    add(statusBar, BorderLayout.SOUTH);

    JPanel panel = new JPanel();

    Board board = new Board(this);
    panel.add(board);
    board.start();

    Board oppBoard = new Board(this);
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