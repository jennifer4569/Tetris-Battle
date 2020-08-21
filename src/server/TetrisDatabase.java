package src.server;

import java.util.*;
import java.sql.*;

/** 
 * <b>TetrisDatabase</b> connects to the SQL database, and updates/retrieves the 
 * requested information. The database consists of user credentials, as well as
 * game statistics, such as number of games won, total number of games, and high 
 * score.
 * @author Jennifer Zhang
 * @version 1.7
 */
public class TetrisDatabase {

    /** The minimum String size for a username/password */
    public static int MIN_ENTRY_SIZE = 5;
    
    /** The maximum String size for a username/password */
    public static int MAX_ENTRY_SIZE = 20;

    /** 
     * Creates the SQL database if it doesn't exist
     * @return True if the database was successfully created, false
     * otherwise
     */
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

    /**
     * Checks to make sure that the entry is valid for the SQL database
     * @param s The entry to check for
     * @return True if the entry is valid (valid size and valid characters),
     * false otherwise
     */
    public static boolean isValidEntry(String s){
        return checkEntrySize(s) && checkEntryChars(s);
    }

    /**
     * Checks to make sure that the entry size is valid for the SQL database
     * @param s The entry to check for
     * @return True if the entry size is valid, false otherwise
     */
    public static boolean checkEntrySize(String s){
        return MIN_ENTRY_SIZE <= s.length() && s.length() <= MAX_ENTRY_SIZE;
    }

    /**
     * Checks to make sure that the entry characters is valid for the SQL database
     * @param s The entry to check for
     * @return True if the entry characters is valid, false otherwise
     */
    public static boolean checkEntryChars(String s){
        //makes sure alphanumeric
        return s.matches("^[a-zA-Z0-9]*$");
    }

    /** 
     * Adds the user with the requested credentials
     * @param username The username of the requested user to register
     * @param password The password of the requested user to register
     * @return True if the user was successfully created, false otherwise
     */
    public static boolean addUser(String username, String password){
        if(!isValidEntry(password)) return false;
        return addUser(username, password.hashCode());
    }

    /** 
     * Adds the user with the requested credentials
     * @param username The username of the requested user to register
     * @param password The hashed password of the requested user to register 
     * @return True if the user was successfully created, false otherwise
     */
    public static boolean addUser(String username, int password){
        if(!isValidEntry(username)) return false;
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            //gets the number of users with the same username 
            //(should be 0 if the username isn't taken, and 1 if it is)
            String cmd = "SELECT COUNT(*) FROM USERS WHERE" +
                            " USERNAME = '" + username + "';";
            ResultSet r = stmt.executeQuery(cmd);

            //if the user has not existed before, add to the database
            if(r.getInt(1) == 0){
                cmd = "INSERT INTO USERS VALUES (" +
                        "'" + username + "'," +
                        " " + password + "," +
                        " 0, 0, 0);";
                stmt.executeUpdate(cmd);
                stmt.close();
                c.close();
                return true;                
            }
            //the username exists, register failed
            else{
                stmt.close();
                c.close();
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
    }
    
    /** 
     * Authenticates the user with the given credentials
     * @param username The username of the requested user to authenticate
     * @param password The password of the requested user to authenticate 
     * @return True if the user credentials were correct, false otherwise
     */
    public static boolean authenticateUser(String username, String password){
        if(!isValidEntry(password)) return false;
        return authenticateUser(username, password.hashCode());
    }

    /** 
     * Authenticates the user with the given credentials
     * @param username The username of the requested user to authenticate
     * @param password The hashed password of the requested user to authenticate 
     * @return True if the user credentials were correct, false otherwise
     */
    public static boolean authenticateUser(String username, int password){        
        if(!isValidEntry(username)) return false;
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            //gets the number of users with the same username and password
            //(should be 0 if the user credentials are correct, and 1 if they aren't)
            String cmd = "SELECT COUNT(*) FROM USERS WHERE" +
                            " USERNAME = '" + username + "'" +
                            " AND" +
                            " PASSWORD = " + password + ";";

            ResultSet r = stmt.executeQuery(cmd);
            
            boolean authCorrect = r.getInt(1) != 0;
            stmt.close();
            c.close();
            return authCorrect;
        }
        catch (Exception e){
            return false;
        }
    }
    
    /** 
     * Gets the statistics of the requested user
     * @param username The username of the user to get the statistics of
     * @return An int array of size 3, where [0] is the number of wins,
     * [1] is the total number of games, and [2] is the high score
     */
    public static int[] getStats(String username){
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            //gets the number of wins
            String cmd = "SELECT NUM_WINS FROM USERS" + 
                            " WHERE USERNAME = '" + username + "';";
            ResultSet r = stmt.executeQuery(cmd);
            int numwins = r.getInt(1);

            //gets the total number of games played
            cmd = "SELECT NUM_GAMES FROM USERS" + 
                    " WHERE USERNAME = '" + username + "';";
            r = stmt.executeQuery(cmd);
            int numgames = r.getInt(1);

            //gets the highscore
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

    /** 
     * Adds the requested game to the database
     * @param username The username of the user to add game to
     * @param isWin True if the user won this game, false otherwise
     */
    public static void addGame(String username, boolean isWin){
        addGame(username, isWin, -1);
    }

    /** 
     * Adds the requested game to the database, with the given score
     * @param username The username of the user to add game to
     * @param isWin True if the user won this game, false otherwise
     * @param score The score the user received this game
     */
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
            
            //updates numwins if this game was a win
            if(isWin){
                cmd = "UPDATE USERS SET NUM_WINS = NUM_WINS + 1" +
                        " WHERE USERNAME = '" + username + "';";
                stmt.executeUpdate(cmd);
            }

            //checks highscore and updates if this score is the new highscore
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
        }
    }
    
    /** 
     * Gets the default leaderboard, which displays the top 10 users
     * based on high score
     * @return A Pair array of the top 10 users, where the key of each Pair
     * is the username, and the value is their highscore
     */
    public static Pair[] getLeaderboard(){
        return getLeaderboard(10);
    }

    /** 
     * Gets the default leaderboard, which displays the top n users
     * based on high score
     * @param n The top n users to return
     * @return A Pair array of the top n users, where the key of each Pair
     * is the username, and the value is their highscore
     */
    public static Pair[] getLeaderboard(int n){

        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tetris.db");
            stmt = c.createStatement();

            //takes the top n users, ordered by highscore
            //tiebreakers are handled by alphabetical order
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

    /** 
     * The main of the TetrisDatabase, initializes the database
     * if necessary
     * @param args The command line arguments
     */
    public static void main(String args[]){
        createDB();
    }
}
