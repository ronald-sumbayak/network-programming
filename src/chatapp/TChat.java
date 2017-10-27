package chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TChat extends Application {
    
    private FXMLLoader loader;
    
    @Override
    public void start (Stage primaryStage) throws Exception {
        primaryStage.setTitle ("TChat");
        
        loader = new FXMLLoader ();
        loader.setLocation (TChat.class.getResource ("TChat.fxml"));
        
        AnchorPane rootLayout = loader.load ();
        primaryStage.setScene (new Scene (rootLayout));
        primaryStage.show ();
        
        new Thread (() -> loader.<ChatClient>getController ().connect ()).start ();
    }
    
    @Override
    public void stop () throws Exception {
        super.stop ();
        loader.<ChatClient>getController ().finish ();
    }
    
    public static void main (String args[]) {
        launch (args);
    }
}
