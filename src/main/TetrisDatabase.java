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
                            " USERNAME='" + username + "';";
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
                            " USERNAME='" + username + "'" +
                            " AND" +
                            " PASSWORD=" + password + ";";
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

    public static void main(String args[]){
        createDB();
        if(addUser("bob", "yoy1")) System.out.println("successfully added user");
        else System.out.println("error: user already exists // invalid credentials");

        if(authenticateUser("bob", "yoy2")) System.out.println("good credentials");
        else System.out.println("bad credentials");

        if(authenticateUser("bob", "yoy1")) System.out.println("good credentials");
        else System.out.println("bad credentials");
   }
}