package echo;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;

import base.BaseServer;

public class TCPEchoServer extends BaseServer {
    
    public TCPEchoServer () {
        super ();
    }
    
    public TCPEchoServer (int port) {
        super (port);
    }
    
    @Override
    protected void handleClient (Socket client) {
        output.println (String.format ("Someone connected from %s:%d.",
                                       client.getInetAddress (),
                                       client.getPort ()));
        try {
            BufferedReader is = new BufferedReader (new InputStreamReader (client.getInputStream ()));
            PrintWriter os = new PrintWriter (client.getOutputStream (), true);
            String request;
            
            while ((request = is.readLine ()) != null) {
                output.println (String.format ("> %s", request));
                if (request.equals ("\\quit"))
                    break;
                os.println (getResponse (request));
            }
        }
        catch (IOException | NoSuchElementException e) {
            //e.printStackTrace (System.err);
        }
    }
    
    public static void main (String[] args) {
        TCPEchoServer server = new TCPEchoServer (8090);
        server.start ();
    }
}
