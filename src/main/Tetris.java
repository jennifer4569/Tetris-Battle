package src.main;

import javax.swing.*;
import java.awt.*;
import java.net.*;

public class Tetris extends JFrame {

  private JLabel statusBar;
  protected TetrisClientHandler clientHandler;

  private void initToolBar() {
    JToolBar toolBar = new JToolBar();

    JButton registerButton = new JButton("Register");
    registerButton.addActionListener((event) -> {
      JTextField field1 = new JTextField();
      JTextField field2 = new JTextField();
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
      JTextField field1 = new JTextField();
      JTextField field2 = new JTextField();
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

    add(toolBar, BorderLayout.NORTH);
  }

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

    initToolBar();

    statusBar = new JLabel("0");
    add(statusBar, BorderLayout.SOUTH);

    JPanel panel = new JPanel();

    Board board = new Board(this, true);
    panel.add(board);
    // board.start();

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

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Tetris myTetris = new Tetris();
        myTetris.setSize(750, 750);
        myTetris.setLocation(0, 0);
        myTetris.setVisible(true);
      }
    });
  }

}