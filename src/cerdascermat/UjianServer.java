package cerdascermat;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import cerdascermat.DBConn.Soal;
import echo.multithread.MultiThreadEchoServer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class UjianServer extends MultiThreadEchoServer {
    
    private static final int SOAL_INTERVAL = 5;
    
    private List<Peserta> pesertaList = new ArrayList<> ();
    private Map<String, Peserta> pesertas = new HashMap<> ();
    private List<Soal> soalList;
    private boolean inUjian;
    private DBConn conn;
    private int soalIndex;
    
    @FXML
    private ListView<String> pesertaListView;
    private ObservableList<String> pesertaListItems = FXCollections.observableArrayList ();
    
    @FXML
    private Button mulaiButton;
    
    @FXML
    private void initialize () {
        pesertaListView.setItems (pesertaListItems);
    }
    
    @FXML
    private void mulaiUjian () {
        mulaiButton.setDisable (true);
        new Thread (() -> {
            inUjian = true;
    
            broadcast ("<notice> Ujian akan dimulai dalam 10 detik");
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor ();
    
            final int[] counter = {10};
            service.scheduleAtFixedRate (() -> {
                if (counter[0]-- > 1)
                    broadcast ("<counter> " + String.valueOf (counter[0]));
                else
                    service.shutdownNow ();
            }, 0, 1, TimeUnit.SECONDS);
    
            while (!service.isShutdown ()) {}
            mulai ();
        });
    }
    
    public UjianServer () {
        this (50000);
    }
    
    public UjianServer (int port) {
        super (port);
        conn = new DBConn ();
        soalList = conn.getSoal ();
    }
    
    private void broadcast (String msg) {
        for (Peserta peserta : pesertaList)
            peserta.send (msg);
    }
    
    @Override
    protected void handleClient (Socket client) {
        Peserta peserta = new Peserta (client);
        peserta.start ();
    }
    
    @Override
    protected void start () {
        // keep listening for new connection
        while (true)
            listen ();
    }
    
    private void mulai () {
        broadcast ("<notice> pilih dengan meng-klik pada jawaban");
        soalIndex = -1;
        
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor ();
        service.scheduleAtFixedRate (() -> {
            if (++soalIndex == soalList.size ()) {
                service.shutdownNow ();
                return;
            }
    
            System.out.println (soalList.get (soalIndex).idSoal);
            
            Soal soal = soalList.get (soalIndex);
            broadcast ("<pertanyaan> " + soal.pertanyaan);
            broadcast ("<pilihanA> " + soal.pilihan1);
            broadcast ("<pilihanB> " + soal.pilihan2);
            broadcast ("<pilihanC> " + soal.pilihan3);
    
            Timer timer = new Timer ();
            TimerTask task = new TimerTask () {
                int counter = SOAL_INTERVAL;
                @Override
                public void run () {
                    broadcast ("<counter> " + String.valueOf (counter));
                    counter--;
                    if (counter == 0)
                        timer.cancel ();
                }
            };
            timer.scheduleAtFixedRate (task, 0, 1000);
        }, 0, SOAL_INTERVAL, TimeUnit.SECONDS);
        
        while (!service.isShutdown ()) {}
        inUjian = false;
        calculateScore ();
        for (Peserta peserta : pesertaList)
            close (peserta.socket);
        System.exit (0);
    }
    
    private void calculateScore () {
        for (Peserta peserta : pesertaList) {
            int score = 0;
            for (Soal soal : soalList)
                if (conn.getJawaban (peserta.username, soal.idSoal).equals (soal.jawaban))
                    score++;
            peserta.send (String.format ("<notice><end> Jumlah jawaban benar: %d\nTerima kasih sudah bermain :D", score));
        }
    }
    
    class Peserta extends Thread{
    
        protected BufferedReader is;
        protected PrintWriter os;
        
        public String username;
        public Socket socket;
        
        private List<Integer> soalDijawab = new ArrayList<> ();
    
        public Peserta (Socket socket) {
            this.socket = socket;
    
            try {
                is = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
                os = new PrintWriter (socket.getOutputStream (), true);
                os.println ("<notice> Username: ");
                username = is.readLine ();
                os.println (String.format ("<notice> Welcome, %s.", username));
                output.println (String.format ("%s (%s:%d) connected.",
                                               username,
                                               socket.getInetAddress (),
                                               socket.getPort ()));
                pesertaListItems.add (username);
            }
            catch (IOException | NoSuchElementException e) {
                e.printStackTrace (System.err);
                close (socket);
            }
            
            if (!inUjian) {
                pesertaList.add (this);
                pesertas.put (username, this);
                os.println ("<notice> Mohon tunggu sampai ujian dimulai");
            }
        }
        
        void send (String msg) {
            os.println (msg);
        }
    
        @Override
        public void run () {
            String inputUser;
            try {
                do {
                    inputUser = is.readLine ();
                    if (inUjian) {
                        if (soalDijawab.contains (soalIndex))
                            send ("<notice> Kamu sudah menjawab soal ini :D");
                        else if (inputUser.trim ().length () > 1)
                            send ("<notice> Jawab dengan mengetikkan pilihan huruf jawaban");
                        else {
                            conn.insertJawaban (username, soalList.get (soalIndex).idSoal, inputUser);
                            soalDijawab.add (soalIndex);
                            send ("<notice> Jawaban disimpan");
                        }
                    }
                }
                while (inputUser != null);
            }
            catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }
    
    public static void main (String[] args) {
        UjianServer server = new UjianServer (3984);
        server.start ();
    }
}
