package src.main;

import java.net.*;
import java.io.*;
import java.util.*;

public class TetrisClientHandler implements Runnable{
    public TetrisClientHandler(Socket s){

    }
    public void run(){
        
    }
    public static void main(String[] args){
        try{
            String server = "localhost";
            int port = 8080;

            Socket socket = new Socket(server, port);



            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            
            Scanner in = new Scanner(inStream);         
            PrintWriter out = new PrintWriter(outStream, true); //autoflush

            out.println("REGISTER JENNI WORLD");
            // TetrisClientHandler clientHandler = new TetrisClientHandler(socket);
            // Thread t = new Thread(clientHandler);
            // t.start();
        }
        catch(Exception e){
        }
    }
}