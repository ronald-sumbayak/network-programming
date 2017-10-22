package echo.multithread;

import java.io.IOException;
import java.util.NoSuchElementException;

import base.BaseClient;

public class MultiThreadEchoClient extends BaseClient {
    
    private MultiThreadEchoClient () {
        super ();
    }
    
    private MultiThreadEchoClient (String host) {
        super (host);
    }
    
    @Override
    protected void onConnected () {
        try {
            output.println (is.readLine ());
            os.println (input.readLine ());
            output.println (is.readLine ());
            
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
        MultiThreadEchoClient client = new MultiThreadEchoClient ();
        client.connect (3984);
    }
}
