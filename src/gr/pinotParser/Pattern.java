package gr.pinotParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angor
 */
public class Pattern {

    private String patternName;
    private boolean patternValidity;
    private static Connection connection;

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

    public Pattern(String _patternName) {
        this.patternName = _patternName;
        this.patternValidity = true;
    }

    public String getPatternName() {
        return this.patternName;
    }

    public boolean isPatternValid() {
        return this.patternValidity;
    }

    public void setValidity(boolean val) {
        this.patternValidity = val;
    }

    public void checkPatternValidity() {
    }

    public String getValidClassFullName(String name, long projectID) 
            throws SQLException {
        String fullName = null;
        try {
            try {
                if (connection == null) {
                    connection = Pattern.getConnection();
                }
                if (connection.isClosed()) {
                    connection = Pattern.getConnection();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PatternList.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
            Statement statement = connection.createStatement();
            String sql = "SELECT name FROM classes"
                    + " WHERE name REGEXP \'\\.+" + name + "$\'"
                    + " AND projectid=" + projectID;
            ResultSet rs = statement.executeQuery(sql);

            // Count the results returned from the query
            int rowCount = 0;
            while (rs.next()) {
                fullName = rs.getString("name");
                rowCount++;
            }
            if (rowCount != 1) {
                fullName = null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
        }
        connection.close();
        return fullName;
    }

    public String getValidSuperClassFullName(String name, long projectID) 
            throws SQLException {
       String superClass = null;
        try {
            try {
                if (connection == null) {
                    connection = Pattern.getConnection();
                }
                if (connection.isClosed()) {
                    connection = Pattern.getConnection();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PatternList.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
            Statement statement = connection.createStatement();
            String sql = "SELECT DISTINCT superclass"
                    + " FROM dithierarchy"
                    + " WHERE subclass=\"" + name + "\""
                    + " AND projectid=" + projectID;
            ResultSet rs = statement.executeQuery(sql);
            // Count the results returned from the query
            int rowCount = 0;
            while (rs.next()) {
                superClass = rs.getString("name");
                rowCount++;
            }
            if (rowCount != 1) {
                superClass = null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
        }
        connection.close();
        return superClass;
    }

    public void addPatternBlock(ArrayList<String> block, long projectID) {
    }

    public String getClassName(String fullLine) {
        return fullLine.substring(0, fullLine.indexOf(" ")).replace(" ", "");
    }

    public ArrayList<String> getClassNamesFromLine(String originalLine) {
        ArrayList<String> temp = new ArrayList<String>();
        String[] classObjectsName = originalLine.split(" ");
        for (int i = 0; i < classObjectsName.length; i++) {
            if (!classObjectsName[i].equals("")) {
                temp.add(classObjectsName[i]);
            }
        }
        return temp;
    }

    public String getParticipantWithXmlFormat(String role, String name) {
        return "<role name=\"" + role + "\" element=\"" + name + "\" />\n";
    }

    public void writePatternToXml(Writer writer) throws IOException {
    }

    public void writePatternToCsv(Writer writer, String projectName) 
            throws IOException {
    }

    public void printPatternStatistics() {
    }

    public void writePatternStatistics(PrintWriter out) {
    }

    public void resetPatternCounters() {
    }
}