package base;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public abstract class BaseServer {
    
    protected ServerSocket serverSocket;
    protected BufferedReader input;
    protected PrintWriter output;
    
    public BaseServer () {
        this (80);
    }
    
    public BaseServer (int port) {
        try {
            input = new BufferedReader (new InputStreamReader (System.in));
            output = new PrintWriter (System.out, true);
            
            // try to open the port specified (requested). Exit program upon fail.
            output.println (String.format ("Opening socket on port %d...", port));
            serverSocket = new ServerSocket (port);
            output.println ("Port opened.\r\n");
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
            System.exit (1);
        }
    }
    
    protected void start () {
        // keep listening for new connection
        while (true)
            listen ();
    }
    
    protected void listen () {
        try {
            // listen for connection. Handle it, then close it upon finish.
            Socket client = serverSocket.accept ();
            handleClient (client);
            close (client);
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
            finish ();
        }
    }
    
    protected abstract void handleClient (Socket client);
    
    protected void close (Socket client) {
        try {
            output.println (String.format ("Closing connection with %s:%d...",
                                           client.getInetAddress (),
                                           client.getPort ()));
            client.close ();
            output.println ("Client disconnected.\r\n");
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
        }
    }
    
    protected void finish () {
        try {
            output.println ("Shutting down server...");
            serverSocket.close ();
            output.println ("Server closed.\r\n");
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
        }
    }
    
    protected String getResponse (String request) {
        switch (request) {
            case "t": return "Jam server: " + String.valueOf (new Date ().getHours ());
            case "d": return "Hari di server: " + String.valueOf (new Date ().getDay ());
            case "m": return "Bulan di server: " + String.valueOf (new Date ().getMonth ());
            case "#": return "ini kata2 mutiara";
            case "id": {
                System.out.println ("masukkan nama: ");
                return "Hello from the other side.";
            }
            default: return request;
        }
    }
}
