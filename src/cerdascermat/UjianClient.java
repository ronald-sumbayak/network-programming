package cerdascermat;

import java.io.IOException;

import echo.multithread.MultiThreadEchoClient;

public class UjianClient extends MultiThreadEchoClient {
    
    public UjianClient () {
        this ("localhost");
    }
    
    public UjianClient (String host) {
        super (host);
    }
    
    @Override
    protected void onConnected () {
        new Thread (() -> {
            String response;
            try {
                while (true) {
                    response = is.readLine ();
                    if (response == null)
                        break;
                    System.out.println (response);
                    if (response.contains ("bermain"))
                        System.exit (0);
                }
            }
            catch (IOException e) { e.printStackTrace (); }
        }).start ();
        
        while (server.isConnected ())
            try { os.println (input.readLine ()); }
            catch (IOException e) { e.printStackTrace (); }
    }
    
    public static void main (String[] args) {
        UjianClient cerdasCermat = new UjianClient ("localhost");
        cerdasCermat.connect (3984);
    }
}
