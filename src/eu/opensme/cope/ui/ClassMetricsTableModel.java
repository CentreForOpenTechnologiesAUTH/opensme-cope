/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.Project;
import eu.opensme.cope.util.HibernateUtil;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.sql.*;
import java.util.Properties;

/**
 *
 * @author george
 */
public class ClassMetricsTableModel extends AbstractTableModel {

    private String[] columnNames = {"Class Name", "Type", "Size",
        "Used By", "Uses(I)", "Uses(Ex)", "Layer", "WMC", "DIT", "NOC",
        "CBO", "RFC", "LCOM", "Ca", "NPM", "R", "Pattern", "Cluster Size"
    };
    private List<ClassAnalysis> data;
    private Connection connection;
    private Project project;
    
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
    
    public ClassMetricsTableModel(Project project) {
        this.project = project;
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("from ClassAnalysis as ca Where ca.project=:project and ca.innerclass=false");
        query.setParameter("project", project);
        data = query.list();
        session.close();        
        try {
            connection = this.getConnection();
        } catch (Exception e) {
            //Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    public int getPlace(ClassAnalysis obj) {
        return data.indexOf(obj);
    }
    
    public ClassAnalysis getRowObject(int row) {
        return data.get(row);
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        String patterns = "No involvement in pattrns";
        ClassAnalysis c = data.get(rowIndex);

        try {
            String class_name = c.getName();
            if (connection != null) {                
                Statement statement = connection.createStatement();
                String sql = "select count(*) as m from patternparticipant, patternlist, patterninstance where class=\"" + class_name + "\" and patternparticipant.patternParticiapntId=patterninstance.patternInstanceID and patterninstance.patternId = patternlist.patternID and patternlist.projectID=" + project.getProjectid();
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    String count = rs.getString("m");
                    if (Integer.parseInt(count)>0) {
                        patterns = "Involved in " + count + " patterns";
                    }
                }
            }
        } catch (Exception e) {
            //Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }       
        switch (columnIndex) {
            case 0:
                return c.getName();
            case 1:
                return c.getType();
            case 2:
                return c.getClassSize();
            case 3:
                return c.getUsedby();
            case 4:
                return c.getUsesinternal();
            case 5:
                return c.getUsesexternal();
            case 6:
                return c.getLayer();
            case 7:
                return c.getWMC();
            case 8:
                return c.getDIT();
            case 9:
                return c.getNOC();
            case 10:   
                return c.getCBO();
            case 11:
                return c.getRFC();
            case 12:
                return c.getLCOM();
            case 13:
                return c.getCa();
            case 14:
                return c.getNPM();
            case 15:
                return c.getReusabilityAssessment();
            case 16:
                return patterns;
            case 17:
                return c.getClusterSize();
            default:
                return "Undef. case";
        }
    }
    
    public void setValue(int row, ClassAnalysis obj){
        data.set(row, obj);
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    Object[] exampleValues() {
        int columnCount = this.getColumnCount();
        Object[] exampleValues = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            exampleValues[i] = getValueAt(0, i);
        }
        return exampleValues;
    }
}
