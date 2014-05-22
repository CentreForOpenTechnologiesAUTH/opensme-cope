package eu.opensme.cope.factgenerators.historyanalyzer;

/**
 * Created by IntelliJ IDEA.
 * User: george
 * Date: 4/1/2011
 * Time: 6:51 πμ
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileInputStream;

import java.math.BigInteger;
import java.sql.*;
import java.util.Properties;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import eu.opensme.cope.factgenerators.historyanalyzer.svnschema.Log;
import eu.opensme.cope.factgenerators.historyanalyzer.svnschema.Logentry;
import eu.opensme.cope.factgenerators.historyanalyzer.svnschema.Path;
import eu.opensme.cope.factgenerators.historyanalyzer.svnschema.Paths;
import eu.opensme.cope.factgenerators.historyanalyzer.util.DateConversionUtil;

import java.util.Date;

public class SVNDBImporter {
    private Connection connection;
    private String svnlogXMLFilePath;
    private long projectid;
    private String rootPackage;
    private long logid;
    private int projectCount = 0;
    private int logEntriesCount = 0;
    private int logEntriesPathCount = 0;
    private int monitoredClassesInLogEntriesPathsCount = 0;

    public SVNDBImporter(String svnlogXMLFilePath, String projectName, String rootPackage) throws Exception {
        this.svnlogXMLFilePath = svnlogXMLFilePath;
        this.projectid = findIdForProjectName(projectName);
        if (projectid == 0) {
            System.out.println("Project with name " + projectName + " does not exist in the database");
            System.exit(1);
        }
        this.rootPackage = rootPackage;
    }

    private long findIdForProjectName(String projectName) throws Exception {
        connection = this.getConnection();
        long id = 0L;
        if (connection != null) {
            String sql = "Select projectid from projects where projecttitle='" + projectName + "'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                id = resultSet.getLong(1);
            }

        }
        return id;  //To change body of created methods use File | Settings | File Templates.
    }


    public void parseSVNLogXML() throws Exception {
        try {
            logid = this.createLog();
            System.out.println("Log for project with project ID " + projectid +
                    " was saved or verified that already exists in DB with id = " + logid);
            projectCount++;
            
            //TODO: change this to a parameter!            
            JAXBContext jc = JAXBContext.newInstance("eu.opensme.cope.factgenerators.historyanalyzer.svnschema");
            Unmarshaller u = jc.createUnmarshaller();
            Log log = (Log) u.unmarshal(new FileInputStream(
                    svnlogXMLFilePath));
            List<Logentry> logEntries = log.getLogentry();
            System.out.println("Storing log entries in the database...");
            //save the log entries to the database
            this.saveLogEntries(logEntries);
            //save the paths for each log entry to the database
            System.out.println("Storing revision paths in the database...");
            for (Logentry logentry : logEntries) {
                Long logentryid = this.getLogentryId(logentry, logid);
                if (logentryid != 0) {
                    Paths paths = logentry.getPaths();
                    List<Path> pathList = paths.getPath();
                    saveLogEntryPaths(pathList, logentryid);
                } else {
                    System.out.println("Logic error. Log entry ID should not be zero here!");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            if (connection != null) connection.close();
        }

    }


    private long createLog() throws Exception {
        long id = 0L;

        if (connection != null) {
            // first check if a project with this title already exists
            String squery;
            squery = "Select * from logs where projectid="
                    + projectid;
            Statement query = connection.createStatement();
            ResultSet rquery = query.executeQuery(squery);
            int rowCount = this.getRowCount(rquery);
            if (rowCount == 0) {
                String ss = "insert into logs(projectid) values('"
                        + projectid + "')";
                Statement s = connection.createStatement();
                s.executeUpdate(ss);
                // determine the id of the project
                Statement query1 = connection.createStatement();
                ResultSet rquery1 = query1.executeQuery(squery);
                rquery1.next();
                id = rquery1.getLong("logid");
            } else {
                while (rquery.next()) {
                    id = rquery.getLong("logid");
                }
            }
            return id;
        }
        return id;
    }

    private Connection getConnection() throws SQLException {
        Connection con = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", "copeuser");
        connectionProps.put("password", "opensme");
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/dependencies",
                connectionProps);
        return con;
    }

    private int getRowCount(ResultSet set) throws SQLException {
        int rowCount;
        int currentRow = set.getRow(); // Get current row
        rowCount = set.last() ? set.getRow() : 0; // Determine number of rows
        if (currentRow == 0) // If there was no current row
            set.beforeFirst(); // We want next() to go to first row
        else
            // If there WAS a current row
            set.absolute(currentRow); // Restore it
        return rowCount;
    }

    private long saveLogEntries(List<Logentry> logentries) throws Exception {
        System.out.println("Storing log entries in the database...");
        long id = 0;
        if (connection != null) {
            PreparedStatement insertStatement =
                    connection.prepareStatement(
                            "insert into logentries(author, date, revision, message, logid) values(?,?,?,?,?)");
            for (Logentry logentry : logentries) {
                String author = logentry.getAuthor();
                String sdate = logentry.getDate();
                Date date = DateConversionUtil.convertSVNSData2JavaDate(sdate);
                BigInteger revision = logentry.getRevision();
                String message = logentry.getMsg();
                // first check if this revision for this log already exists in the database
                String squery = "Select * from logentries where revision="
                        + revision + " and logid=" + logid;
                Statement query = connection.createStatement();
                ResultSet rquery = query.executeQuery(squery);
                int rowCount = this.getRowCount(rquery);
                if (rowCount == 0) {
                    insertStatement.setString(1, author);
                    insertStatement.setDate(2, new java.sql.Date(date.getTime()));
                    insertStatement.setLong(3, revision.longValue());
                    insertStatement.setString(4, message);
                    insertStatement.setLong(5, logid);
                    insertStatement.executeUpdate();
                    // determine the id of the log entry
                    Statement query1 = connection.createStatement();
                    ResultSet rquery1 = query1.executeQuery(squery);
                    rquery1.next();
                    id = rquery1.getLong("logentryid");
                } else {
                    while (rquery.next()) {
                        id = rquery.getLong("logentryid");
                    }
                }
                System.out.println("Log entry for revision " + revision +
                        " was saved or verified that already exists in DB with id = "
                        + id);
                logEntriesCount++;
            }
        }
        return id;
    }

    private void saveLogEntryPaths(List<Path> paths, long logEntryId) throws SQLException {
        if (connection != null) {

            String sql0 =
                    "select pathid from paths where path=? and logentryid=?";
            String sql1 =
                    "insert into paths(kind, action, path, logentryid, isPathAMonitoredClass, classid) values(?,?,?,?,?,?)";
            String sql2 =
                    "insert into paths(kind, action, path, logentryid, isPathAMonitoredClass) values(?,?,?,?,?)";
            PreparedStatement statement0 = connection.prepareStatement(sql0);
            PreparedStatement statement1 = connection.prepareStatement(sql1);
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            for (Path path : paths) {
                long pathId;
                String pathText = path.getContent();
                statement0.setString(1, pathText);
                statement0.setLong(2, logEntryId);
                ResultSet rs = statement0.executeQuery();

                if (rs.next()) {
                    pathId = rs.getLong(1);
                }
                else {
                    String kind = path.getKind();
                    String action = path.getAction();
                    long classId = this.getClassIdFromPathTextIfExists(pathText);
                    boolean isPathAMonitoredClass = (classId != 0);
                    statement1.setString(1, kind);
                    statement2.setString(1, kind);
                    statement1.setString(2, action);
                    statement2.setString(2, action);
                    statement1.setString(3, pathText);
                    statement2.setString(3, pathText);
                    statement1.setLong(4, logEntryId);
                    statement2.setLong(4, logEntryId);
                    statement1.setBoolean(5, isPathAMonitoredClass);
                    statement2.setBoolean(5, isPathAMonitoredClass);
                    if (isPathAMonitoredClass) {
                        statement1.setLong(6, classId);
                        statement1.executeUpdate();
                    } else {
                        statement2.executeUpdate();
                    }
                    ResultSet saved = statement0.executeQuery();
                    if (saved.next()) pathId = saved.getLong(1); else pathId=0L;
                }
                System.out.println("Path " + pathText + " was saved or verified that already exists in DB with id = "
                        + pathId);
            }
        }
    }


    private long getLogentryId(Logentry logentry, long logid) throws SQLException {
        long logentryid = 0;
        if (connection != null) {
            String text = "Select logentryid from logentries where revision=" + logentry.getRevision() +
                    " and logid=" + logid;
            Statement query = connection.createStatement();
            ResultSet rs = query.executeQuery(text);
            if (rs.next()) {
                return rs.getLong(1);

            }
        }
        return logentryid;
    }

    private long getClassIdFromPathTextIfExists(String pathText) throws SQLException {
        long id = 0;
        if (connection != null) {

            //get the class name from the path text. The function returns the empty string if
            //this path text does not correspond to a java source file
            String className = classNameFinder(pathText);
            if (!("".equals(className))) {
                System.out.println("I'm here!");
                String classIdQuery =
                        "select classid from classes where name='" + className + "' and projectid=" + projectid;
                Statement classIdStatement = connection.createStatement();
                ResultSet classIdRS = classIdStatement.executeQuery(classIdQuery);
                if (classIdRS.next()) {
                    long result =  classIdRS.getLong(1);
                    System.out.println("Found class with id "+result);
                    return result;
                }
            }
        }
        return id;
    }
    //Not called

    private String classNameFinder(String pathText) {
        System.out.println("I'm here");
        String javaExtension =
                pathText.substring(pathText.length() - 4, pathText.length());
        String className = "";
        //if it is a java source file
        if ("java".equals(javaExtension)) {
            //find name starting position
            int startOfClassName = pathText.lastIndexOf(rootPackage);
            if (startOfClassName!=-1) {
                String classPath = pathText.substring(startOfClassName, pathText.length()-5);
                className = classPath.replace('/','.');
                System.out.println("Class name: "+className);
            }
        }
        return className;
    }

    public static void main(String[] args) {
        //get the required parameters from args (svn log xml file, project name, source code folder for monitored classes)
        if (args.length!=3) {
            System.out.println("It appears you have not provided the required parameters");
            System.out.println("Usage: java -jar SVNDBImporter.jar <svn log file path> <project name> <root package>");
            System.out.println("Example: java -jar SVNDBImporter.jar "+
                    "antlog.xml \"Ant 1.7.0 (Core)\" org");
            System.exit(0);
        }
        try {
            SVNDBImporter importer = new SVNDBImporter(args[0],args[1],args[2]);
            importer.parseSVNLogXML();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


    }
}
