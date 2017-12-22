package cerdascermat;

import java.io.IOException;

import echo.multithread.MultiThreadEchoClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class UjianClient extends MultiThreadEchoClient {
    
    @FXML
    private BorderPane usernameContainer;
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private VBox vboxSoal;
    @FXML
    private Text counter;
    @FXML
    private Text notice;
    @FXML
    private Text name;
    @FXML
    private Text pertanyaan;
    @FXML
    private Button pilihanA;
    @FXML
    private Button pilihanB;
    @FXML
    private Button pilihanC;
    
    @FXML
    private void applyUsername () {
        os.println (usernameField.getText ());
        usernameLabel.setText (usernameField.getText ());
        usernameField.setVisible (false);
        usernameContainer.setMaxHeight (0);
        notice.setText ("");
        name.setVisible (true);
        counter.setVisible (true);
        notice.setVisible (true);
        vboxSoal.setVisible (true);
    }
    
    @FXML
    private void pilihA () {
        os.println ("a");
        System.out.println ("PILIH A");
        sealChoice (true);
    }
    
    @FXML
    private void pilihB () {
        os.println ("b");
        System.out.println ("PILIH B");
        sealChoice (true);
    }
    
    @FXML
    private void pilihC () {
        os.println ("c");
        System.out.println ("PILIH C");
        sealChoice (true);
    }
    
    private void sealChoice (boolean seal) {
        pilihanA.setDisable (seal);
        pilihanB.setDisable (seal);
        pilihanC.setDisable (seal);
    }
    
    private void showSoal () {
        pertanyaan.setVisible (true);
        pilihanA.setVisible (true);
        pilihanB.setVisible (true);
        pilihanC.setVisible (true);
    }
    
    public UjianClient () {
        this ("localhost");
    }
    
    public UjianClient (String host) {
        super (host);
    }
    
    @Override
    public void connect () {
        super.connect (50000);
    }
    
    @Override
    protected void onConnected () {
        String response;
        try {
            while (true) {
                response = is.readLine ();
                if (response == null)
                    break;
                System.out.println (response);
                String realResponse = response.split (" ", 2)[1];
                System.out.println (realResponse);
                if (response.startsWith ("<counter>"))
                    Platform.runLater (() -> counter.setText (realResponse));
                else if (response.startsWith ("<notice>")) {
                    Platform.runLater (() -> notice.setText (realResponse));
                    if (response.contains ("<end>"))
                        counter.setVisible (false);
                }
                else if (response.startsWith ("<pertanyaan>")) {
                    System.out.println (realResponse);
                    Platform.runLater (() -> pertanyaan.setText (realResponse));
                    showSoal ();
                    sealChoice (false);
                    Platform.runLater (() -> notice.setText (""));
                }
                else if (response.startsWith ("<pilihanA>"))
                    Platform.runLater (() -> pilihanA.setText (realResponse));
                else if (response.startsWith ("<pilihanB>"))
                    Platform.runLater (() -> pilihanB.setText (realResponse));
                else if (response.startsWith ("<pilihanC>"))
                    Platform.runLater (() -> pilihanC.setText (realResponse));
                
                //if (response.contains ("bermain"))
                //    System.exit (0);
            }
        }
        catch (IOException e) { e.printStackTrace (); }
        
        //while (server.isConnected ())
        //    try { os.println (input.readLine ()); }
        //    catch (IOException e) { e.printStackTrace (); }
    }
    
    public static void main (String[] args) {
        UjianClient cerdasCermat = new UjianClient ("localhost");
        cerdasCermat.connect (3984);
    }
}
