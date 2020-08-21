package src.server;

import java.net.*;
import java.io.*;
import java.util.*;

/** 
 * <b>TetrisServer</b> is the centralized server for the Tetris app.
 * It will allow for up to MAX_CLIENTS client connections at a time, 
 * and will manage each client connection through a separate thread. 
 * @author Jennifer Zhang
 * @version 2.5
 */
public class TetrisServer {
    
    /** The maximum number of clients that can connect to the server at one time */
    public static int MAX_CLIENTS = 32;

    /**
     * The TetrisServer main function. It will run the centralized server code,
     * and wait for new client connections. When a new client connects, it will 
     * create a new thread for that client.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        try {

            int port = 8080;
            ServerSocket s = new ServerSocket(port, MAX_CLIENTS);
            
            //matching queue and logged users, as well as their respective locks
            LinkedList<TetrisServerHandler> matchmakingQueue = new LinkedList<TetrisServerHandler>(); 
            LinkedHashSet<String> loggedUsers = new LinkedHashSet<String>();
            Object queueLock = new Object();
            Object loggedLock = new Object();
            
            //awaits for clients to connect, and when they do, creates a new thread to handle the connection
            while (true) {
                Socket socket = s.accept();
                System.out.println("SERVER: Client connected");

                TetrisServerHandler serverHandler = new TetrisServerHandler(socket, matchmakingQueue, queueLock, loggedUsers, loggedLock);
                Thread t = new Thread(serverHandler);
                t.start();
            }
        }
        catch (Exception e) {
        }
    }
}

/** 
 * TetrisServerHandler is the server handler for the TetrisServer class.
 * It is connected to one client at all times.
 */
class TetrisServerHandler implements Runnable {
    
    private Socket socket;    /** The socket connected with this client */
    private String tName;     /** The current thread name, for readability in debug prints */
    private String user;      /** The username of the current client -- null if the client is not logged in */
    
    private LinkedHashSet<String> loggedUsers;   /** Stores all the users that are currently logged in */
    private Object loggedLock;                   /** Lock for the loggedUsers Set */
    private LinkedList<TetrisServerHandler> matchmakingQueue; /** The matchmaking queue for users waiting for a game */
    private Object queueLock;                    /** Lock for the matchmakingQueue */

    private boolean inQueue;   /** Boolean to check if the user is currently in queue */
    private boolean inGame;    /** Boolean to check if the user is currently in game */

    private TetrisServerHandler opponent; /** Reference to the opponent's TetrisServerHandler, null if currently not in game */
    
    private PrintWriter out;   /** The output stream */

    /**
     * Creates an instance of the TetrisServerHandler 
     * @param sock The socket connected with this client
     * @param mQueue The reference to the matchmakingQueue
     * @param qLock The lock for the matchmakingQueue
     * @param lUsers The reference to the loggedUsers
     * @param lLock The lock for the loggedUsers
     */
    public TetrisServerHandler(Socket sock, LinkedList<TetrisServerHandler> mQueue, Object qLock, LinkedHashSet<String> lUsers, Object lLock) {
        socket = sock;
        user = null;
        
        loggedUsers = lUsers;
        loggedLock = lLock;
        matchmakingQueue = mQueue;
        queueLock = qLock;

        inQueue = false;
        inGame = false;
        opponent = null;
    }

    /**
     * Gets the name of the user currently logged in
     * @return The username of the current user
     */
    public String getName() {
        return user;
    }

    /**
     * Attempts to register the requested user, called when the client sends 
     * "REGISTER"
     * @param line The lines sent, [1] is the username, [2] is the password
     * @return True if the registration was successful, false otherwise
     */
    private boolean register(String[] line) {
        String username = line[1];
        int hashedPassword;

        // getting the hashed password
        try {
            hashedPassword = Integer.parseInt(line[2]);
        } catch (Exception e) {
            // invalid password
            if (!TetrisDatabase.isValidEntry(line[2])) {
                out.println("FAILURE INVALID");
                System.out.println(tName + ": REGISTER failed, credentials invalid");
                return false;
            }
            hashedPassword = line[2].hashCode();
        }

        // invalid username
        if (!TetrisDatabase.isValidEntry(username)) {
            out.println("FAILURE INVALID");
            System.out.println(tName + ": REGISTER failed, credentials invalid");
            return false;
        }

        // username taken
        if (!TetrisDatabase.addUser(username, hashedPassword)) {
            out.println("FAILURE TAKEN");
            System.out.println(tName + ": REGISTER failed, username taken");
            return false;
        } 
        //successful registration
        else {
            user = username;
            out.println("SUCCESS " + user + " 0 0 0");
            System.out.println(tName + ": REGISTER success, User " + user);
            return true;
        }
    }

    /**
     * Attempts to login with the given credentials, called when the client sends
     * "LOGIN"
     * @param line The lines sent, [1] is the username, [2] is the password
     * @return True if the login was successful, false otherwise
     */
    private boolean login(String[] line) {
        String username = line[1];
        int hashedPassword;

        // getting the hashed password
        try {
            hashedPassword = Integer.parseInt(line[2]);
        } catch (Exception e) {
            // invalid password
            if (!TetrisDatabase.isValidEntry(line[2])) {
                out.println("FAILURE INVALID");
                System.out.println(tName + ": LOGIN failed, credentials invalid");
                return false;
            }
            hashedPassword = line[2].hashCode();
        }

        // invalid username
        if (!TetrisDatabase.isValidEntry(username)) {
            out.println("FAILURE INVALID");
            System.out.println(tName + ": LOGIN failed, credentials invalid");
            return false;
        }

        // username taken
        if (!TetrisDatabase.authenticateUser(username, hashedPassword)) {
            out.println("FAILURE INCORRECT");
            System.out.println(tName + ": LOGIN failed, credentials incorrect");
            return false;
        } 
        else {
            synchronized(loggedLock){
                //if the user is already logged in
                if(loggedUsers.contains(username)){
                    out.println("FAILURE LOGGED");
                    System.out.println(tName + ": LOGIN failed, user already logged in");
                    return false;
                }
                //successful login
                else{
                    loggedUsers.add(username);
                    user = username;
                    out.println("SUCCESS " + user + " " + getStatsStr());
                    System.out.println(tName + ": LOGIN success, User " + user);
                    return true;
                }
            }
        }
    }

    /**
     * Gets the String representation of the current user's stats
     * @return The String representation of the user's stats
     */
    public String getStatsStr() {
        int[] stats = TetrisDatabase.getStats(user);
        return stats[0] + " " + stats[1] + " " + stats[2];
    }

    /**
     * Gets the leaderboard, which is based on high scores, called when the 
     * client sends "LEADERBOARD"
     * @param line The lines sent, line[1] is the optional parameter, which 
     * will instead of showing the top 10 scores, will retrieve the top
     * line[1] scores
     */
    private void leaderboard(String[] line) {
        Pair[] lb;

        //attempts to parse the optional parameter
        try {
            lb = TetrisDatabase.getLeaderboard(Integer.parseInt(line[1]));
        } catch (Exception e) {
            lb = TetrisDatabase.getLeaderboard();
        }
        
        //sends the leaderboard
        String output = "LEADERBOARD";
        for (int i = 0; i < lb.length; i++) {
            if(lb[i] == null) break;
            output += " " + lb[i];
        }
        out.println(output);
        System.out.println(tName + ": LEADERBOARD success");
    }

    /**
     * Enters the user in the matchmaking queue, called when the client sends
     * "PLAY"
     */
    private void play() {
        synchronized (queueLock) {
            //if the queue is empty, then the add to the and wait
            if (matchmakingQueue.peek() == null) {
                System.out.println(tName + ": PLAY awaiting, Added to matchmaking queue");
                matchmakingQueue.add(this);
                inQueue = true;
            } 
            //if not, match with the opponent
            else {
                opponent = matchmakingQueue.poll();
            }
        }

        //matched with the opponent, generates the seed and sends to both clients
        if (opponent != null) {
            long seed = (new Random()).nextLong();
            this.match(seed, opponent);
            opponent.match(seed, this);
        }
    }

    /**
     * Sends to the client that they've been matched with an opponent
     * @param seed The seed both clients will use to randomly generate their pieces
     * @param op The reference to the opponent's TetrisServerHandler
     */
    public void match(long seed, TetrisServerHandler op) {
        inQueue = false;
        inGame = true;
        opponent = op;
        System.out.println(tName + ": MATCH success, opponent " + opponent.getName());
        out.println("MATCH " + opponent.getName() + " " + opponent.getStatsStr() + " " + seed);
    }

    /**
     * Tells the opponent that they have moved their piece, to update
     * the opponent's opponent board. Called when the client sends "MOVE"
     * @param keyPressed The keyCode of the key the client pressed
     */
    private void move(String keyPressed) {
        opponent.opponentMove(keyPressed);
        System.out.println(tName + ": MOVE success, " + keyPressed);
    }

    /**
     * Tells the client that the opponent has moved their piece, to update
     * the client's opponent board. 
     * @param keyPressed The keyCode of the key the opponent pressed
     */
    public void opponentMove(String keyPressed) {
        out.println("OPPONENT MOVE " + keyPressed);
        System.out.println(tName + ": OPPONENT MOVE success, " + keyPressed);
    }

    /**
     * Sends the opponent their entire board, to update the opponent's 
     * opponent board (to avoid synchronization errors). Called when the
     * sends "BOARD"
     * @param board The String representation of the client's board
     */
    public void board(String board){
        opponent.opponentBoard(board);
        System.out.println(tName + ": BOARD success, " + board);
    }

    /**
     * Sends the client the opponent's entire board, to update the client's 
     * opponent board (to avoid synchronization errors). 
     * @param board The String representation of the opponent's board
     */
    public void opponentBoard(String board) {
        out.println("BOARD " + board);
        System.out.println(tName + ": BOARD success, " + board);
    }

    /**
     * Sends a randomized line to the opponent, called when the client sends
     * "SEND"
     */
    private void send() {
        String line = "XXXXXXXXXX";
        
        //30% chance of getting 1 hole
        //40% chance of getting 2 holes
        //30% chance of getting 3 holes
        Random r = new Random();
        Double p = r.nextDouble();
        int numHoles = 0;
        if(p < 0.3) numHoles = 1;
        else if(p < 0.7) numHoles = 2;
        else numHoles = 3;

        //while there are still holes to add, add holes
        while(numHoles != 0){
            int i = r.nextInt(10);
            if(line.charAt(i)== 'X'){
                line = line.substring(0,i)+'.'+line.substring(i+1);
                numHoles--;
            }
        }

        //sends the line to the opponent
        opponent.opponentSend(line);
        out.println("SENT " + line);
        System.out.println(tName + ": SEND success, " + line);
    }

    /**
     * Sends the client that the opponent has sent a line
     * @param lineSent The String representation of the line the opponent sent
     * the client
     */
    public void opponentSend(String lineSent) {
        out.println("OPPONENT SEND " + lineSent);
        System.out.println(tName + ": OPPONENT SEND success, " + lineSent);
    }

    /**
     * Tells the opponent that the client has lost, and updates the database for
     * this current game. Called with the client sends "LOSE"
     * @param line The lines sent, line[1] is an optional parameter that contains
     * the user's score for this game
     */
    private void lose(String[] line) {

        //tells the opponent that the user has lost before disconnecting
        if(opponent != null)
            opponent.opponentLose();
        opponent = null;
        inGame = false;
        System.out.println(tName + ": LOSE success");

        // updates the database with this game
        try {
            int score = Integer.parseInt(line[1]);
            TetrisDatabase.addGame(user, false, score);
        } catch (Exception e) {
            TetrisDatabase.addGame(user, false);
        }
    }

    /**
     * Tells the client that the opponent has lost
     */
    public void opponentLose() {
        opponent = null;
        out.println("OPPONENT LOSE");
        System.out.println(tName + ": OPPONENT LOSE success");
    }

    /**
     * The client has won, and updates the database for this current game. Called 
     * with the client sends "WIN"
     * @param line The lines sent, line[1] is an optional parameter that contains
     * the user's score for this game
     */
    private void win(String[] line) {
        opponent = null;
        inGame = false;

        System.out.println(tName + ": WIN success");
        // update db here
        try {
            int score = Integer.parseInt(line[1]);
            TetrisDatabase.addGame(user, true, score);
        } catch (Exception e) {
            TetrisDatabase.addGame(user, true);
        }
    }

    /**
     * The run function for the TetrisServerHandler, which handles a single client/server
     * connection.
     */
    public void run() {
        tName = Thread.currentThread().getName();
        System.out.println("SERVER: Created thread " + tName);

        try {

            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();

            Scanner in = new Scanner(inStream);
            out = new PrintWriter(outStream, true); // autoflush

            while (in.hasNextLine()) {
                String[] line = in.nextLine().split(" ");
                if (line.length == 0)
                    continue;

                // if not logged in, the available commands are register and login
                if (user == null) {
                    if (line[0].equals("REGISTER") && line.length > 2)
                        register(line);
                    if (line[0].equals("LOGIN") && line.length > 2)
                        login(line);
                }

                if (line[0].equals("LEADERBOARD"))
                    leaderboard(line);

                // if logged in, the user can join queue
                if (user != null) {
                    if (line[0].equals("PLAY") && !inQueue && !inGame)
                        play();
                    //if the user is in game, then the game commands are available
                    if (inGame) {
                        if (line[0].equals("MOVE") && line.length > 1)
                            move(line[1]);
                        if (line[0].equals("PIECE") && line.length > 1)
                            piece(line[1]);
                        if (line[0].equals("BOARD") && line.length > 1)
                            board(line[1]);
                        if (line[0].equals("SEND"))
                            send();
                        if (line[0].equals("LOSE"))
                            lose(line);
                        if (line[0].equals("WIN"))
                            win(line);
                    }
                }
            }
        } 
        catch (IOException e) {
        }

        System.out.println(tName + ": Client disconnected");
        
        //removes the user from the loggedUsers, since the client disconnected
        if(user != null){
            synchronized(loggedLock){
                loggedUsers.remove(user);
            }
        }
    }
}
