package cerdascermat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CerdasCermat extends Application {
    
    private UjianClient controller;
    
    @Override
    public void start (Stage primaryStage) throws Exception {
        primaryStage.setTitle ("Cerdas Cermat");
    
        FXMLLoader loader = new FXMLLoader ();
        loader.setLocation (CerdasCermat.class.getResource ("CerdasCermat.fxml"));
        
        AnchorPane rootLayout = loader.load ();
        primaryStage.setScene (new Scene (rootLayout));
        primaryStage.show ();
        
        controller = loader.getController ();
        new Thread (() -> controller.connect ()).start ();
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
