package cerdascermat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CerdasCermatServer extends Application {
    
    private UjianServer controller;
    
    @Override
    public void start (Stage primaryStage) throws Exception {
        primaryStage.setTitle ("Cerdas Cermat");
        
        FXMLLoader loader = new FXMLLoader ();
        loader.setLocation (CerdasCermatServer.class.getResource ("CerdasCermatServer.fxml"));
        
        AnchorPane rootLayout = loader.load ();
        primaryStage.setScene (new Scene (rootLayout));
        primaryStage.show ();
        
        controller = loader.getController ();
        new Thread (() -> controller.start ()).start ();
    }
    
    @Override
    public void stop () throws Exception {
        super.stop ();
        controller.finish ();
    }
    
    public static void main (String args[]) {
        launch (args);
    }
}
