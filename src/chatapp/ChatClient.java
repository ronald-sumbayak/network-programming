package chatapp;

import java.io.IOException;

import base.BaseClient;

class ChatClient extends BaseClient {
    
    @Override
    protected void onConnected () {
        Thread osThread = new Thread (() -> {
            try {
                while (server.isConnected ())
                    output.println (is.readLine ());
            }
            catch (IOException e) {
                e.printStackTrace (System.out);
                System.exit (1);
            }
        });
        osThread.start ();
    
        try {
            output.println ("Enter your username!");
            while ((request = input.readLine ()) != null) {
                os.println (request);
                if (request.equals ("\\quit"))
                    break;
            }
        }
        catch (IOException e) {
            e.printStackTrace (System.out);
        }
    }
    
    public static void main (String[] args) {
        ChatClient client = new ChatClient ();
        client.connect ();
    }
}
