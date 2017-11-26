package cerdascermat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class DBConn {
    
    private String host = "jdbc:mysql://localhost/cerdascermat";
    private String user = "root";
    private String pass = "";
    
    private Connection connection;
    
    public DBConn () {
        try {
            Class.forName ("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection (host, user, pass);
            truncate ();
        }
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace ();
        }
    }
    
    private void truncate () {
        try {
            String sql = "truncate jawaban";
            Statement statement = connection.createStatement ();
            statement.execute (sql);
        }
        catch (SQLException e) {
            e.printStackTrace ();
        }
    }
    
    public void insertJawaban (String peserta, String idSoal, String pilihan) {
        try {
            String sql = "insert into jawaban (peserta, id_soal, pilihan) values (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement (sql);
            preparedStatement.setString (1, peserta);
            preparedStatement.setString (2, idSoal);
            preparedStatement.setString (3, pilihan);
            preparedStatement.executeUpdate ();
        }
        catch (SQLException e) {
            e.printStackTrace ();
        }
    }
    
    public String getJawaban (String peserta, String idSoal) {
        try {
            System.out.println (peserta);
            System.out.println (idSoal);
            String sql = "select pilihan from jawaban where peserta = ? and id_soal = ?";
            PreparedStatement preparedStatement = connection.prepareStatement (sql);
            preparedStatement.setString (1, peserta);
            preparedStatement.setString (2, idSoal);
            ResultSet resultSet = preparedStatement.executeQuery ();
            resultSet.next ();
            
            String pilihan;
            try { pilihan = resultSet.getString ("pilihan"); }
            catch (SQLException e) { pilihan = "z"; }
            return pilihan;
        }
        catch (SQLException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    public List<Soal> getSoal () {
        try {
            String sql = "select * from soal";
            Statement statement = connection.createStatement ();
            ResultSet resultSet = statement.executeQuery (sql);
    
            List<Soal> soalList = new ArrayList<> ();
            while (resultSet.next ()) {
                Soal soal = new Soal ();
                soal.idSoal = resultSet.getString ("id");
                soal.pertanyaan = resultSet.getString ("pertanyaan");
                soal.jawaban = resultSet.getString ("jawaban");
                soal.pilihan1 = resultSet.getString ("pilihan1");
                soal.pilihan2 = resultSet.getString ("pilihan2");
                soal.pilihan3 = resultSet.getString ("pilihan3");
                soalList.add (soal);
            }
            return soalList;
        }
        catch (SQLException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    class Soal {
        String idSoal;
        String pertanyaan;
        String pilihan1, pilihan2, pilihan3;
        String jawaban;
    }
}
