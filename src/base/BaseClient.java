package base;

import java.io.*;
import java.net.*;

public abstract class BaseClient {
    
    protected Socket server;
    protected BufferedReader is, input;
    protected PrintWriter os, output;
    protected String request;
    
    private InetAddress host;
    
    public BaseClient () {
        this ("localhost");
    }
    
    public BaseClient (String host) {
        try {
            input = new BufferedReader (new InputStreamReader (System.in));
            output = new PrintWriter (System.out, true);
            output.println ("Finding host...");
            this.host = InetAddress.getByName (host);
            output.println (String.format ("Host found with IP %s.\r\n", this.host.getHostAddress ()));
        }
        catch (UnknownHostException e) {
            e.printStackTrace (System.err);
            System.exit (1);
        }
    }
    
    public void connect () {
        connect (80);
    }
    
    protected void connect (int port) {
        try {
            output.println (String.format ("Connecting to %s on port %d...", host.getHostName (), port));
            server = new Socket (host, port);
            is = new BufferedReader (new InputStreamReader (server.getInputStream ()));
            os = new PrintWriter (server.getOutputStream (), true);
            output.println ("Connected.\r\n");
            onConnected ();
            finish ();
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
        }
    }
    
    public void finish () {
        try {
            output.println ("Closing connection...");
            server.close ();
            output.println ("Disconnected.\r\n");
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
        }
    }
    
    protected abstract void onConnected ();
}
