package chatapp;

import java.io.IOException;
import java.util.*;

import base.BaseClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatClient extends BaseClient {
    
    @FXML
    private ListView<String> messageListView;
    
    @FXML
    private ListView<String> chatListView;
    
    @FXML
    private TextField inputMessage;
    
    private Map<String, ObservableList<String>> messages = new HashMap<> ();
    private ObservableList<String> chats = FXCollections.observableArrayList ();
    
    @FXML
    private void sendMessage () {
        String msg = inputMessage.getText ();
        
        if (msg.startsWith ("@") || msg.startsWith ("#"))
            putMessage (msg.replaceFirst (" ", " You: "));
        else
            putMessage ("@system " + "You: " + msg);
        
        os.println (msg);
        inputMessage.clear ();
        
        if (msg.equals ("/quit"))
            finish ();
    }
    
    private void putMessage (String msg) {
        String split[], delimiter, room, message;
        delimiter = String.valueOf (msg.charAt (0));
        split = msg.split (String.format ("[%s ]", delimiter), 3);
        room = delimiter + split[1];
        message = split[2];
        
        if (!messages.containsKey (room)) {
            messages.put (room, FXCollections.observableArrayList ());
            chats.add (room);
        }
        
        messages.get (room).add (message);
    }
    
    @Override
    protected void onConnected () {
        chatListView.setItems (chats);
        chatListView.getSelectionModel ().selectedItemProperty ().addListener ((observable, oldValue, newValue) -> {
            messageListView.setItems (messages.get (newValue));
            inputMessage.clear ();
        });
        
        Thread osThread = new Thread (() -> {
            try {
                while (server.isConnected ()) {
                    String msg = is.readLine ();
                    if (msg.startsWith ("@") || msg.startsWith ("#"))
                        Platform.runLater (() -> putMessage (msg));
                    
                    else
                        Platform.runLater (() -> putMessage ("@system " + msg));
                }
            }
            catch (IOException e) {
                e.printStackTrace (System.out);
                System.exit (1);
            }
        });
        
        osThread.start ();
        while (true)
            if (!server.isConnected ())
                break;
    }
    
    public static void main (String[] args) {
        ChatClient client = new ChatClient ();
        client.connect ();
    }
}
