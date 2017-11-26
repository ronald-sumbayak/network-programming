package echo.multithread;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;

import base.BaseServer;

public class MultiThreadEchoServer extends BaseServer {
    
    public MultiThreadEchoServer () {
        super ();
    }
    
    public MultiThreadEchoServer (int port) {
        super (port);
    }
    
    @Override
    protected void listen () {
        try {
            Socket client = serverSocket.accept ();
            handleClient (client);
        }
        catch (IOException e) {
            e.printStackTrace (System.err);
            finish ();
        }
    }
    
    @Override
    protected void handleClient (Socket client) {
        ClientHandler handler = new ClientHandler (client);
        handler.start ();
    }
    
    protected class ClientHandler extends Thread {
    
        protected BufferedReader is;
        protected PrintWriter os;
        
        public String username;
        public Socket socket;
        
        public ClientHandler (Socket socket) {
            this.socket = socket;
            
            try {
                is = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
                os = new PrintWriter (socket.getOutputStream (), true);
                os.println ("Username: ");
                username = is.readLine ();
                os.println (String.format ("Welcome, %s.", username));
                output.println (String.format ("%s (%s:%d) connected.",
                                               username,
                                               socket.getInetAddress (),
                                               socket.getPort ()));
            }
            catch (IOException | NoSuchElementException e) {
                e.printStackTrace (System.err);
                close (socket);
            }
        }
    
        @Override
        public void run () {
            try {
                String request;
                while ((request = is.readLine ()) != null) {
                    output.println (String.format ("%s: %s", username, request));
                    if (request.equals ("\\quit"))
                        break;
                    os.println (getResponse (request));
                }
            }
            catch (IOException | NoSuchElementException e) {
                //e.printStackTrace (System.err);
            }
            
            close (socket);
        }
    }
    
    public static void main (String[] args) {
        MultiThreadEchoServer server = new MultiThreadEchoServer (3984);
        server.start ();
    }
}
