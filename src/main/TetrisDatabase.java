package src.main;

import java.util.*;
import java.sql.*;

public class TetrisDatabase {
    public static boolean createDB(){
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();
            String cmd = "CREATE TABLE USERS (" +
                            " USERNAME  CHAR(50) PRIMARY KEY NOT NULL, " +
                            " PASSWORD  INT                  NOT NULL, " +  //hashed
                            " NUM_WINS  INT                  NOT NULL, " + 
                            " NUM_GAMES INT                  NOT NULL, " + 
                            " HIGHSCORE INT                  NOT NULL);"; 
            stmt.executeUpdate(cmd);
            stmt.close();
            c.close();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    public static boolean addUser(String username, String password){
        if(!isValidEntry(password)) return false;
        return addUser(username, password.hashCode());
    }
    public static boolean addUser(String username, int password){
        if(!isValidEntry(username)) return false;
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            String cmd = "SELECT COUNT(*) FROM USERS WHERE" +
                            " USERNAME = '" + username + "';";
            ResultSet r = stmt.executeQuery(cmd);

            //if the user has not existed before
            if(r.getInt(1) == 0){
                cmd = "INSERT INTO USERS VALUES (" +
                        "'" + username + "'," +
                        " " + password + "," +
                        " 0, 0, 0);";
                stmt.executeUpdate(cmd);
                // System.out.println(cmd);
                stmt.close();
                c.close();
                return true;                
            }
            else{
                stmt.close();
                c.close();
                return false;
            }
        }
        catch (Exception e){
            // e.printStackTrace();
            return false;
        }
    }
    //since this is being run by server code:
    //the idea is to user authenticateuser, if it returns true then make a thread
    //and the rest of the functions assumes that the user has been authenticated
    public static boolean authenticateUser(String username, String password){
        if(!isValidEntry(password)) return false;
        return authenticateUser(username, password.hashCode());
    }
    public static boolean authenticateUser(String username, int password){        
        if(!isValidEntry(username)) return false;
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            String cmd = "SELECT COUNT(*) FROM USERS WHERE" +
                            " USERNAME = '" + username + "'" +
                            " AND" +
                            " PASSWORD = " + password + ";";
            // System.out.println(cmd);
            ResultSet r = stmt.executeQuery(cmd);
            
            boolean authCorrect = r.getInt(1) != 0;
            //no match
            stmt.close();
            c.close();
            return authCorrect;
        }
        catch (Exception e){
            // e.printStackTrace();
            return false;
        }
    }
    public static boolean isValidEntry(String s){
        if(s == null || s.length() > 50 ) return false;
        //makes sure alphanumeric
        return s.matches("^[a-zA-Z0-9]*$");
    }
    public static int[] getStats(String username){
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();


            String cmd = "SELECT NUM_WINS FROM USERS" + 
                            " WHERE USERNAME = '" + username + "';";
            ResultSet r = stmt.executeQuery(cmd);
            int numwins = r.getInt(1);

            cmd = "SELECT NUM_GAMES FROM USERS" + 
                    " WHERE USERNAME = '" + username + "';";
            r = stmt.executeQuery(cmd);
            int numgames = r.getInt(1);

            cmd = "SELECT HIGHSCORE FROM USERS" + 
                    " WHERE USERNAME = '" + username + "';";
            r = stmt.executeQuery(cmd);
            int highscore = r.getInt(1);

            stmt.close();
            c.close();

            int[] stats = new int[3];
            stats[0] = numwins;
            stats[1] = numgames;
            stats[2] = highscore;
            return stats;
        }
        catch (Exception e){
            return null;
        }
    }
    public static void addGame(String username, boolean isWin){
        addGame(username, isWin, -1);
    }
    public static void addGame(String username, boolean isWin, int score){
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            //updates numgames
            String cmd = "UPDATE USERS SET NUM_GAMES = NUM_GAMES + 1" +
                            " WHERE USERNAME = '" + username + "';";
            stmt.executeUpdate(cmd);
            
            //updates numwins
            if(isWin){
                cmd = "UPDATE USERS SET NUM_WINS = NUM_WINS + 1" +
                        " WHERE USERNAME = '" + username + "';";
                stmt.executeUpdate(cmd);
            }

            //checks highscore and updates if necessary
            cmd = "SELECT HIGHSCORE FROM USERS" + 
                    " WHERE USERNAME = '" + username + "';";
            ResultSet r = stmt.executeQuery(cmd);
            int currHS = r.getInt(1);
            if(currHS < score){
                cmd = "UPDATE USERS SET HIGHSCORE = " + score + 
                        " WHERE USERNAME = '" + username + "';";
                stmt.executeUpdate(cmd);
            }

            stmt.close();
            c.close();
        }
        catch (Exception e){
            // e.printStackTrace();
        }
    }
    public static Pair[] getLeaderboard(){
        return getLeaderboard(10);
    }
    public static Pair[] getLeaderboard(int n){

        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            //checks highscore and updates if necessary
            String cmd = "SELECT USERNAME, HIGHSCORE FROM USERS" +
            " ORDER BY HIGHSCORE DESC, USERNAME ASC" +
            " LIMIT " + n + ";";
            ResultSet r = stmt.executeQuery(cmd);
            int currHS = r.getInt(1);

            Pair[] leaderboard = new Pair[n];
            int i = 0;
            while(r.next()) {
                leaderboard[i] = new Pair(r.getString(1), Integer.parseInt(r.getString(2)));
                i++;
            }

            stmt.close();
            c.close();
            return leaderboard;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
    public static void main(String args[]){
        createDB();
        /*
        if(addUser("bob", "yoy1")) System.out.println("successfully added user");
        else System.out.println("error: user already exists // invalid credentials");

        if(authenticateUser("bob", "yoy2")) System.out.println("good credentials");
        else System.out.println("bad credentials");

        if(authenticateUser("bob", "yoy1")) System.out.println("good credentials");
        else System.out.println("bad credentials");
        addGame("bob", false, 100);
        */
        String[] users = {"apple", "banana", "carrot", "d", "eggs", "flour", "garbage", "heehee", "ice", "jyenni", "kitkat", "llama"};
        for(int i = 0; i < users.length; i++){
            addUser(users[i], users[i]);
            addGame(users[i], true, 10*i);
        }
        addGame("apple", true, 100);
        int[] stats = getStats("bob");
        System.out.println(stats[0]);
        System.out.println(stats[1]);
        System.out.println(stats[2]);
        getLeaderboard();
   }
}