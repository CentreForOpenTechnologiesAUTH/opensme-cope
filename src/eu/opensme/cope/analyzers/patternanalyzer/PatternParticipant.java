/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers.patternanalyzer;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Apostolis
 */
public class PatternParticipant {
    private String class_role;
    private String class_name;
    private static Connection connection;
    {
        try {
            if (connection == null) {
                connection = PatternParticipant.getConnection();
            }
            if (connection.isClosed()) {
                connection = PatternParticipant.getConnection();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PatternParticipant.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Connection getConnection() throws SQLException {
        Connection con = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", "copeuser");
        connectionProps.put("password", "opensme");
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/dependencies",
                connectionProps);
        return con;
    }    
    
    public PatternParticipant(int pid) {
        try {
            //if (connection.isClosed()) {
            //    connection = PatternParticipant.getConnection();
            //}
            Statement statement = connection.createStatement();
            String sql = "select * from patternparticipant where patternParticiapntId=" + pid;
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                class_role = rs.getString("role");
                class_name = rs.getString("class");
            }
        }  catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public PatternParticipant(String role, String name) {
        class_role = role;
        class_name = name;
    }
    
    public String getClassName() {
        return class_name;
    }
    
    public String getClassRole() {
        return class_role;
    }
    
    public void printParticipant() {
        System.out.println("role : " + class_role);
        System.out.println("name : " + class_name);
        System.out.println("\n");
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(PatternInstance.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }

    
    public void writeOnDB(long pid) {
        try {
            //if (connection.isClosed()) {
            //    connection = PatternParticipant.getConnection();
            //}
            Statement statement = connection.createStatement();
            if(class_name.contains("$"))
            {
                int pos = class_name.indexOf("$");
                class_name = class_name.substring(0, pos);
            }
            String sql = "insert into patternparticipant(patternParticiapntId, role, class) values(\"" + pid + "\", \"" + class_role + "\", \"" + class_name +"\")" ;
//            System.out.println(sql);
            statement.executeUpdate(sql);
            //connection.close();
        }  catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
}
