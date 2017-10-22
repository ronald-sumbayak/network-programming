package echo;

import java.io.IOException;
import java.util.NoSuchElementException;

import base.BaseClient;

public class TCPEchoClient extends BaseClient {
    
    protected TCPEchoClient () {
        this ("localhost");
    }
    
    protected TCPEchoClient (String host) {
        super (host);
    }
    
    @Override
    protected void onConnected () {
        try {
            while ((request = input.readLine ()) != null) {
                os.println (request);
                if (request.equals ("\\quit"))
                    break;
                output.println (String.format ("Response: %s", is.readLine ()));
            }
        }
        catch (IOException | NoSuchElementException e) {
            //e.printStackTrace (System.err);
        }
    }
    
    public static void main (String[] args) {
        TCPEchoClient client = new TCPEchoClient ();
        client.connect (8090);
    }
}
