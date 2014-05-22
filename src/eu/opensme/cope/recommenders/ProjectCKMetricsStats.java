/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.recommenders;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.Project;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author george
 */
public class ProjectCKMetricsStats {
    private static Map<String, Integer> stats= null;

    public static Double getReusabilityAssessmentForClass(ClassAnalysis ca, Project project) {
        /*double WMCF = ((double)(ca.getWMC() - getStat("WMCMIN",project)))
                / (getStat("WMCMAX",project)-getStat("WMCMIN",project));
        double DITF = ((double)(ca.getDIT() - getStat("DITMIN",project)))
                / (getStat("DITMAX",project)-getStat("DITMIN",project));
        double NOCF = ((double)(ca.getNOC() - getStat("NOCMIN",project)))
                / (getStat("NOCMAX",project)-getStat("NOCMIN",project));
        double CBOF = ((double)(ca.getCBO() - getStat("CBOMIN",project)))
                / (getStat("CBOMAX",project)-getStat("CBOMIN",project));
        double RFCF = ((double)(ca.getRFC() - getStat("RFCMIN",project)))
                / (getStat("RFCMAX",project)-getStat("RFCMIN",project));
        double LCOMF = ((double)(ca.getLCOM() - getStat("LCOMMIN",project)))
                / (getStat("LCOMMAX",project)-getStat("LCOMMIN",project));
        double assessment = (1.0/6)*(1-WMCF);
        assessment += (1.0/6) * (1-DITF);
        assessment += (1.0/6) * NOCF;
        assessment += (1.0/6) * (1-CBOF);
        assessment += (1.0/6) * (1-RFCF);
        assessment += (1.0/6) * (1-LCOMF);
        return assessment;*/
        double lCBO = 8.753 * Math.log10(ca.getCBO()+1);
        double lDIT = 2.505 * Math.log10(ca.getDIT()+1);
        double lWMC = -1.922 * Math.log10(ca.getWMC()+1);
        double lRFC = 0.892 * Math.log10(ca.getRFC()+1);
        double lLCOM = -0.399 * Math.log10(ca.getLCOM()+1);
        double lNOC = -1.080 * Math.log10(ca.getNOC()+1);
        double assessment = -1 * (lCBO+lDIT+lWMC+lRFC+lLCOM+lNOC);
        
        return assessment;
    }

//    public static Integer getStat(String key, Project project) {
//        //if the project has been set
//        if (project != null) {
//            //and if the stats have not already been set
//            if (stats==null) {
//                //compute it
//                produceStats(project);
//            }
//            return stats.get(key);
//        }
//        return null;
//    }

//    private static void produceStats(Project project) {
//        //SessionFactory factory = HibernateUtil.getSessionFactory();
//        //Session session = factory.openSession();
//        /*
//         *
//         *  DON'T KNOW WHAT's WRONG WITH THIS QUERY or maybe i'm just too tired
//         * to see
//            "select new Map ( max(ca.WMC) as WMCMAX, min(ca.WMC) as WMCMIN, "+
//            "max(ca.DIT) as DITMAX, min(ca.DIT) as DITMIN, max(ca.NOC) as NOCMAX, min(ca.NOC) as NOCMIN, "+
//            "max(ca.CBO) as CBOMAX, min(ca.CBO) as CBOMIN, max(ca.RFC) as RFCMAX, min(ca.RFC) as RFCMIN, "+
//            "max(ca.LCOM) as LCOMMAX, min(ca.LCOM) as LCOMMIN) "+
//            "from ClassAnalysis as ca where ca.project=:project and ca.innerclass=false"
//         */
//            
//        //Query query = session.createQuery(
//          //      "select (max(ca.WMC) as WMCMAX) from ClassAnalysis ca where ca.project=:project and ca.innerclass=false"
//          // );
//        //query.setParameter("project", project);
//        //stats = (Map<String, Integer>) query.uniqueResult();
//        //Object results = query.uniqueResult();
//        //session.close();
//        try {
//            Connection con = getConnection();
//            Statement s = con.createStatement();
//            ResultSet r = s.executeQuery(
//                    "select "+
//                    "max(wmc), min(wmc), "+
//                    "max(dit), min(dit), "+
//                    "max(noc), min(noc), "+
//                    "max(cbo), min(cbo), "+
//                    "max(rfc), min(rfc), "+
//                    "max(lcom), min(lcom) "+
//                    "from classes where projectid="+project.getProjectid()+" and innerclass="+0);
//            stats = new HashMap<String, Integer>();
//            r.next();
//            stats.put("WMCMAX", r.getInt(1)); stats.put("WMCMIN", r.getInt(2));
//            stats.put("DITMAX", r.getInt(3)); stats.put("DITMIN", r.getInt(4));
//            stats.put("NOCMAX", r.getInt(5)); stats.put("NOCMIN", r.getInt(6));
//            stats.put("CBOMAX", r.getInt(7)); stats.put("CBOMIN", r.getInt(8));
//            stats.put("RFCMAX", r.getInt(9)); stats.put("RFCMIN", r.getInt(10));
//            stats.put("LCOMMAX", r.getInt(11)); stats.put("LCOMMIN", r.getInt(12));
//            con.close();
//        }
//        catch (SQLException e) {
//            //System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }

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
/*
    public static void main(String[] args) {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();
        String q =
            "from Project p where p.projectid=10";
        Query query = session.createQuery(q);
        Project p = (Project) query.uniqueResult();
        session.close();
        ProjectCKMetricsStats statistics = new ProjectCKMetricsStats(p);
        
        System.out.println("WMCm = "+statistics.getStat("WMCMIN"));
        System.out.println("WMCM = "+statistics.getStat("WMCMAX"));
        System.out.println("DITm = "+statistics.getStat("DITMIN"));
        System.out.println("DITM = "+statistics.getStat("DITMAX"));
        System.out.println("NOCm = "+statistics.getStat("NOCMIN"));
        System.out.println("NOCM = "+statistics.getStat("NOCMAX"));
        System.out.println("CBOm = "+statistics.getStat("CBOMIN"));
        System.out.println("CBOM = "+statistics.getStat("CBOMAX"));
        System.out.println("RFCm = "+statistics.getStat("RFCMIN"));
        System.out.println("RFCM = "+statistics.getStat("RFCMAX"));
        System.out.println("LCOMm = "+statistics.getStat("LCOMMIN"));
        System.out.println("LCOMM = "+statistics.getStat("LCOMMAX"));
        
    }*/
}
