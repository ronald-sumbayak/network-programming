package chatapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import base.BaseClient;

class ChatClient extends BaseClient {
    
    @Override
    protected void onConnected () {
        Thread osThread = new Thread (() -> {
            try {
                while (server.isConnected ()) {
                    String msg = is.readLine ();
                    
                    if (msg.startsWith ("/time")) {
                        String username = msg.split (" ")[1];
                        String time = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (new Date ());
                        os.println (String.format ("@%s My local time is [%s]", username, time));
                        output.println (String.format ("%s requested your local time.", username));
                    }
                    else
                        output.println (msg);
                }
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
                if (request.equals ("/quit"))
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
