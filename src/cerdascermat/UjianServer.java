package cerdascermat;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import cerdascermat.DBConn.Soal;
import echo.multithread.MultiThreadEchoServer;

class UjianServer extends MultiThreadEchoServer {
    
    private List<Peserta> pesertaList = new ArrayList<> ();
    private Map<String, Peserta> pesertas = new HashMap<> ();
    private List<Soal> soalList;
    private boolean inUjian;
    private DBConn conn;
    private int soalIndex;
    
    public UjianServer () {
        this (80);
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
        new Thread (() -> {
            String text;
            try {
                do { System.out.println ("menunggu"); text = input.readLine (); }
                while (!text.equals ("mulaiujian"));
                inUjian = true;
                
                broadcast ("Ujian akan dimulai dalam 10 detik");
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor ();
                
                final int[] counter = {10};
                service.scheduleAtFixedRate (() -> {
                    System.out.println ("a");
                    if (counter[0]-- > 1)
                        broadcast (String.valueOf (counter[0]));
                    else {
                        System.out.println ("a");
                        service.shutdownNow ();
                    }
                }, 0, 1, TimeUnit.SECONDS);
                
                while (!service.isShutdown ()) {}
                mulai ();
            }
            catch (IOException e) {
                e.printStackTrace ();
            }
        }).start ();
        System.out.println ("siap");
        // keep listening for new connection
        while (true)
            listen ();
    }
    
    private void mulai () {
        broadcast ("pilih dengan mengetikkan pilihan huruf");
        soalIndex = -1;
        
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor ();
        service.scheduleAtFixedRate (() -> {
            if (++soalIndex == soalList.size ()) {
                service.shutdownNow ();
                return;
            }
    
            System.out.println (soalList.get (soalIndex).idSoal);
            
            Soal soal = soalList.get (soalIndex);
            String msg = String.format ("%s\na.%s\nb.%s\nc.%s",
                                        soal.pertanyaan,
                                        soal.pilihan1, soal.pilihan2, soal.pilihan3);
            broadcast (msg);
        }, 0, 5, TimeUnit.SECONDS);
        
        while (!service.isShutdown ()) {}
        System.out.println ("service shut down");
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
            peserta.send (String.format ("Jumlah jawaban benar: %d", score));
            peserta.send ("Terima kasih sudah bermain :D");
        }
    }
    
    class Peserta extends ClientHandler {
        
        private List<Integer> soalDijawab = new ArrayList<> ();
    
        public Peserta (Socket socket) {
            super (socket);
            if (!inUjian) {
                System.out.println ("terdaftar");
                pesertaList.add (this);
                pesertas.put (username, this);
                os.println ("Mohon tunggu sampai ujian dimulai");
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
                            send ("Kamu sudah menjawab soal ini :D");
                        else if (inputUser.trim ().length () > 1)
                            send ("Jawab dengan mengetikkan pilihan huruf jawaban");
                        else {
                            conn.insertJawaban (username, soalList.get (soalIndex).idSoal, inputUser);
                            soalDijawab.add (soalIndex);
                            send ("Jawaban disimpan");
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
