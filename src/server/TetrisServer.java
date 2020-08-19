package src.server;

import java.net.*;
import java.io.*;
import java.util.*;
public class TetrisServer{
    private static int MAX_CLIENTS = 32;

    public static void main(String[] args){
        try{
            int port = 8080;
            ServerSocket s = new ServerSocket(port, MAX_CLIENTS);
            ArrayList<TetrisServerHandler> matchmakingQueue = new ArrayList<TetrisServerHandler>(); //yeah ik its not a queue 5hed
            Object queueLock = new Object();
            while(true){
                Socket socket = s.accept();            
                System.out.println("SERVER: Client connected");
   
                TetrisServerHandler serverHandler = new TetrisServerHandler(socket, matchmakingQueue, queueLock);
                Thread t = new Thread(serverHandler);
                t.start();
            }
        }
        catch(Exception e){
        }
    }
}

class TetrisServerHandler implements Runnable{
    Socket socket;
    String user;
    String tName;
    ArrayList<TetrisServerHandler> matchmakingQueue;
    Object queueLock;
    boolean inQueue;
    boolean inGame;
    TetrisServerHandler opponent;

    PrintWriter out;
    public TetrisServerHandler(Socket s, ArrayList<TetrisServerHandler> q, Object l){
        socket = s;
        user = null;
        matchmakingQueue = q;
        queueLock = l;
        inQueue = false;
        inGame = false;
        opponent = null;
    }

    public String getName(){
        return user;
    }

    private boolean register(String[] line){
        String username = line[1];
        int hashedPassword;
        
        //getting hashed password
        try{
            hashedPassword = Integer.parseInt(line[2]);
        }
        catch(Exception e){
            //invalid password
            if(!TetrisDatabase.isValidEntry(line[2])){
                out.println("FAILURE INVALID");
                System.out.println(tName + ": REGISTER failed, credentials invalid");
                return false;
            }
            hashedPassword = line[2].hashCode();
        }
        
        //invalid username
        if(!TetrisDatabase.isValidEntry(username)){
            out.println("FAILURE INVALID");
            System.out.println(tName + ": REGISTER failed,  credentials invalid");
            return false;
        }

        //username taken
        if(!TetrisDatabase.addUser(username, hashedPassword)){
            out.println("FAILURE TAKEN");
            System.out.println(tName + ": REGISTER failed, username taken");
            return false;
        }
        else{
            user = username;
            out.println("SUCCESS");
            System.out.println(tName + ": REGISTER success, User " + user);
            return true;
        }
    }
    private boolean login(String[] line){
        String username = line[1];
        int hashedPassword;
        
        //getting hashed password
        try{
            hashedPassword = Integer.parseInt(line[2]);
        }
        catch(Exception e){
            //invalid password
            if(!TetrisDatabase.isValidEntry(line[2])){
                out.println("FAILURE INVALID");
                System.out.println(tName + ": LOGIN failed, credentials invalid");
                return false;
            }
            hashedPassword = line[2].hashCode();
        }
        
        //invalid username
        if(!TetrisDatabase.isValidEntry(username)){
            out.println("FAILURE INVALID");
            System.out.println(tName + ": LOGIN failed, credentials invalid");
            return false;
        }

        //username taken
        if(!TetrisDatabase.authenticateUser(username, hashedPassword)){
            out.println("FAILURE INCORRECT");
            System.out.println(tName + ": LOGIN failed, credentials incorrect");
            return false;
        }
        else{
            user = username;
            out.println("SUCCESS " + getStatsStr());
            System.out.println(tName + ": LOGIN success, User " + user);
            return true;
        }
    }
    public String getStatsStr(){
        int[] stats = TetrisDatabase.getStats(user);
        return stats[0] + " " + stats[1] + " " + stats[2];
    }

    private void leaderboard(String[] line){
        //username,highscore username,highscore ...
        Pair[] lb;
        
        try{
            lb = TetrisDatabase.getLeaderboard(Integer.parseInt(line[1]));
        }
        catch(Exception e){
            lb = TetrisDatabase.getLeaderboard();
        }

        String output = "LEADERBOARD";
        for(int i = 0; i < lb.length; i++){
            output += " " + lb[i];
        }
        out.println(output);
        System.out.println(tName + ": LEADERBOARD success");
    }

    private void play(){
        synchronized(queueLock){
            if(matchmakingQueue.isEmpty()){
                System.out.println(tName + ": PLAY awaiting, Added to matchmaking queue");
                matchmakingQueue.add(this);
                inQueue = true;
            }
            else{
                opponent = matchmakingQueue.get(0);
                matchmakingQueue.remove(0);
            }
        }

        if(opponent != null){
            long seed = (new Random()).nextLong();
            this.match(seed, opponent);
            opponent.match(seed, this);
        }
    }

    public void match(long seed, TetrisServerHandler op){
        inQueue = false;
        inGame = true;
        opponent = op;
        System.out.println(tName + ": MATCH success, opponent " + opponent.getName());
        out.println("MATCH " + opponent.getName() + " " + opponent.getStatsStr() + " " + seed);
    }
    private void move(String keyPressed){
        opponent.opponentMove(keyPressed);
        System.out.println(tName + ": MOVE success, " + keyPressed);
    }

    public void opponentMove(String keyPressed){
        out.println("OPPONENT MOVE " + keyPressed);
        System.out.println(tName + ": OPPONENT MOVE success, " + keyPressed);
    }

    private void send(){
        String line = "XXXXXXX.X"; //rng this later
        opponent.opponentSend(line);
        out.println("SENT " + line);
        System.out.println(tName + ": SEND success, " + line);
    }   

    public void opponentSend(String lineSent){
        out.println("OPPONENT SEND " + lineSent);
        System.out.println(tName + ": OPPONENT SEND success, " + lineSent);
    }

    private void lose(String[] line){
        opponent.opponentLose();
        opponent = null;
        inGame = false;
        System.out.println(tName + ": LOSE success");

        //update db here
        try{
            int score = Integer.parseInt(line[1]);
            TetrisDatabase.addGame(user, false, score);
        }
        catch(Exception e){
            TetrisDatabase.addGame(user, false);
        }
    }

    public void opponentLose(){
        opponent = null;
        // inGame = false;
        
        out.println("OPPONENT LOSE");
        System.out.println(tName + ": OPPONENT LOSE success");
    }

    private void win(String[] line){
        opponent = null;
        inGame = false;

        System.out.println(tName + ": WIN success");
        //update db here
        try{
            int score = Integer.parseInt(line[1]);
            TetrisDatabase.addGame(user, true, score);
        }
        catch(Exception e){
            TetrisDatabase.addGame(user, true);
        }
    }

    public void run(){ 
        tName = Thread.currentThread().getName();
        System.out.println("SERVER: Created thread " + tName);
 
        // Thread currentThread = Thread.currentThread();
        // System.out.println("Executing  thread : " + currentThread.getName()) ;
        // System.out.println("id of the thread is " + currentThread.getId());   

        try{        

            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            
            Scanner in = new Scanner(inStream);         
            out = new PrintWriter(outStream, true); //autoflush
            while(in.hasNextLine()){
                String[] line = in.nextLine().split(" ");
                if(line.length == 0) continue;
                // System.out.println(tName + ": Received command " + line[0]);
                
                //if not logged in
                if(user == null){
                    if(line[0].equals("REGISTER") && line.length >= 2) register(line);
                    if(line[0].equals("LOGIN") && line.length >= 2) login(line);
                }

                if(line[0].equals("LEADERBOARD")) leaderboard(line);
                
                //if logged in
                if(user != null){
                    if(line[0].equals("PLAY") && !inQueue && !inGame) play();
                    if(inGame){
                        if(line[0].equals("MOVE") && line.length >= 1) move(line[1]);
                        if(line[0].equals("SEND")) send();
                        if(line[0].equals("LOSE")) lose(line);
                        if(line[0].equals("WIN")) win(line);
                    }
                }
            }
        }
        catch (IOException e){
        }
        System.out.println(tName + ": Client disconnected");
    }
}