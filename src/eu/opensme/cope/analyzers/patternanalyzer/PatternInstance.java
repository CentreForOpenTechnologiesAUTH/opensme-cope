/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers.patternanalyzer;
import java.util.ArrayList;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Apostolis
 */
public class PatternInstance {
    private String pattern_name;
    private ArrayList<PatternParticipant> pList;
    private static Connection connection;
    {
        try {
            if (connection == null) {
                connection = PatternInstance.getConnection();
            }
            if (connection.isClosed()) {
                connection = PatternInstance.getConnection();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PatternInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Connection getConnection() throws SQLException {
        Connection con = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", "copeuser");
        connectionProps.put("password", "opensme");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dependencies",connectionProps);
        return con;
    }    

    public void closeConnection() {
        try {
            connection.close();
            pList.get(0).closeConnection();
        } catch (SQLException ex) {
            Logger.getLogger(PatternInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public PatternInstance(int pid) {
        try {
            //if (connection.isClosed()) {
            //    connection = PatternInstance.getConnection();
            //}
            pList = new ArrayList<PatternParticipant>();
            Statement statement = connection.createStatement();
            String sql = "select * from patterninstance where patternId=" + pid;
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                pattern_name = rs.getString("patternName");
                int patt_inst_id = rs.getInt("patternInstanceID");
                PatternParticipant pp = new PatternParticipant(patt_inst_id);
                pList.add(pp);
            }
        }  catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public PatternInstance(String pname) {
        pattern_name = pname;
        pList = new ArrayList<PatternParticipant>();
    }
    
    public PatternParticipant getParticipant(int i) {
        return pList.get(i);
    }
    
    public String getName(int i) {
        return (pattern_name + "_" + i);
    }
    
    public int getSize() {
        return pList.size();
    }
    
    public void addParticipant(PatternParticipant temp) {
        pList.add(temp);
    }
    
    public void printInstance() {
        System.out.println("\n\n\n");
        System.out.println("pattern_name " + pattern_name);
        System.out.println("------");
        for (int i=0;i<pList.size();i++) {
            pList.get(i).printParticipant();
        }
    }
    
    public void writeOnDB(long pid) {
        int patt_id = 0;
        for (int i=0; i<pList.size();i++) {
            try {
                //if (connection.isClosed()) {
                //    connection = PatternInstance.getConnection();
                //}
                Statement statement = connection.createStatement();
                String sql = "insert into patterninstance(patternId, patternName) values(\"" + pid + "\", \"" + pattern_name +"\")" ;
//                System.out.println(sql);
                statement.executeUpdate(sql);
                statement = connection.createStatement();
                sql = "select max(patternInstanceID) as m from patterninstance";
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    String maximum = rs.getString("m");
                    patt_id = Integer.parseInt(maximum);
                }
                pList.get(i).writeOnDB(patt_id);
                //connection.close();
            }  catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
