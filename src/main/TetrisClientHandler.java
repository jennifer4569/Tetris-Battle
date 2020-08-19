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

            int i = 0;


            System.out.println(i);
            i++;

            Socket socket = new Socket(server, port);

            System.out.println(i);
            i++;




            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            
            Scanner in = new Scanner(inStream);         
            PrintWriter out = new PrintWriter(outStream, true); //autoflush

            System.out.println(i);
            i++;
            out.println("REGISTER JENNI WORLD");
            System.out.println(i);
            i++;
            // TetrisClientHandler clientHandler = new TetrisClientHandler(socket);
            // Thread t = new Thread(clientHandler);
            // t.start();
        }
        catch(Exception e){
System.out.println("wot");
        }
    }
}