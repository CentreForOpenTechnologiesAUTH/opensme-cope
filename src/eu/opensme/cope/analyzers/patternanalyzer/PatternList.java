/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers.patternanalyzer;
import java.util.ArrayList;
import java.io.*;
import java.util.StringTokenizer;
import java.sql.*;
import java.util.Properties;
import eu.opensme.cope.domain.ReuseProject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Apostolis
 */
public class PatternList {
    private ArrayList<PatternInstance> pList;
    private static Connection connection;
    {
        try {
            if (connection == null) {
                connection = PatternList.getConnection();
            }
            if (connection.isClosed()) {
                connection = PatternList.getConnection();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PatternList.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public PatternList(ReuseProject rp) {
        String project_name = rp.getProjectName();
        try {
            //if (connection.isClosed()) {
            //    connection = PatternList.getConnection();
            //}
            pList = new ArrayList<PatternInstance>();
            Statement statement = connection.createStatement();
            String sql = "select projectid from projects where projecttitle=\"" + project_name + "\"";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String pid = rs.getString("projectid");
                statement = connection.createStatement();
                sql = "select * from patternlist where projectID=" + pid;
                ResultSet rs1 = statement.executeQuery(sql);
                while (rs1.next()) {
                    int patt_id = rs1.getInt("patternID");
                    PatternInstance pi = new PatternInstance(patt_id);
                    pList.add(pi);
                }                
            }
        }  catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public PatternList(String f) {        
            if (connection != null) {
                pList = new ArrayList<PatternInstance>();
                String proj_temp;
                String patt_name="";
                String class_role;
                String class_name;
                StringTokenizer st = null; 
                try {
                    FileInputStream fstream = new FileInputStream(f);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    while ((strLine = br.readLine()) != null)   {
                        st = new StringTokenizer(strLine, ";");
                        proj_temp = st.nextToken();
                        patt_name = st.nextToken();
                        PatternInstance temp_pattern = new PatternInstance(patt_name);
                        while (st.hasMoreTokens()) {
                            class_role = st.nextToken();
                            class_name = st.nextToken();
                            //System.out.println("Creating a new pattern instance for software " + proj_temp + "...");
                            //System.out.println("Pattern name: " + patt_name);
                            //System.out.println("Class name: " + class_name);
                            //System.out.println("Role name: " + class_role);
                            PatternParticipant temp_participant = new PatternParticipant(class_role,class_name);
                            temp_pattern.addParticipant(temp_participant);
                            try {
                                //if (connection == null) {
                                //    return;
                                //}
                                Statement statement = connection.createStatement();
                                String sql = "select subclass from dithierarchy where superclass=\"" + class_name + "\" or superclass like \"%" + class_name + "$%\"";
//                                System.out.println(sql);
                                ResultSet rs = statement.executeQuery(sql);
                                while (rs.next()) {
                                    String subclass = rs.getString("subclass");
//                                    System.out.println("Subclass: " + subclass);
                                    class_role = "Subclass";
                                    class_name = subclass;
                                    temp_participant = new PatternParticipant(class_role,class_name);
                                    temp_pattern.addParticipant(temp_participant);
                                }
                            }  catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        pList.add(temp_pattern);
                    }
                    in.close();
               } catch (Exception e){//Catch exception if any
                    e.printStackTrace();
               }                
          }
    }
    
    public void printAllInstances() {
        for (int i=0; i<pList.size();i++) {
            pList.get(i).printInstance();
        }
    }
    
    public PatternInstance getPattern(int i) {
        return pList.get(i);
    }
    
    public int getSize() {
        return pList.size();
    }
    
    public void closeConnection() {
        try {
            connection.close();
            pList.get(0).closeConnection();
        } catch (SQLException ex) {
            Logger.getLogger(PatternList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeOnDB(long pid) {
        System.out.println("Inserting into DB");
        int patt_id = 0;
        for (int i=0; i<pList.size();i++) {
            try {
                if (connection.isClosed()) {
                    connection = PatternList.getConnection();
                }
                Statement statement = connection.createStatement();
                String sql = "insert into patternlist(projectID) values(\"" + pid + "\")" ;
//                System.out.println(sql);
                statement.executeUpdate(sql);
                statement = connection.createStatement();
                sql = "select max(patternID) as m from patternlist";
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    String maximum = rs.getString("m");
                    patt_id = Integer.parseInt(maximum);
                }
                pList.get(i).writeOnDB(patt_id);                
            }  catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
}
