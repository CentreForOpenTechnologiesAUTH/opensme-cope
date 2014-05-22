/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators;

import classycle.Analyser;
import classycle.util.TrueStringPattern;
import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.dependenciesgenerator.ClassycleHandler;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.PackageAnalysis;
import eu.opensme.cope.util.HibernateUtil;
import gr.spinellis.ckjm.ClassObject;
import gr.spinellis.ckjm.MetricsFilter;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DependenciesGenerator {

    private Connection connection;
    //private String classycleXMLFilePath;
    private long projectId;
    private String projectJAR;
    private final String originalClassPath = System.getProperty("java.class.path");
    private List<String> depJars;
    private Vector<String> missingDepJars;
    private ReuseProject reuseProject;
    private MetricsFilter mf;
    
    private ArrayList<ClassAnalysis> classes;
    private ArrayList<PackageAnalysis> packages;
    
    private ClassycleHandler classycle;
    
    HashMap<Long,Set<Long>> clusters = new HashMap<Long,Set<Long>>();
    /**
     * @param classycleXMFilePath The path of the classycle XML file should be provided. This
     *                            file will be parsed and the data will be stored in the
     *                            database after the execution of the parse method
     * @throws SQLException
     */
    public DependenciesGenerator(ReuseProject project/*, String classycleXMFilePath*/) throws Exception {
        try {
            connection = this.getConnection();
            if (connection != null) {
                //this.classycleXMLFilePath = classycleXMFilePath;
                this.projectId = project.getProject().getProjectid();
                this.projectJAR = project.getProjectJARFilename();
                this.depJars = project.getDependenciesJARs();
                this.reuseProject = project;
            }
        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }
    }

    public void saveMetrics() {
        Vector<ClassObject> classes = mf.getClassObjects();
        this.updateClassRecords(classes, projectId);
    }

    /**
     * Method called to extract CK metrics
     * @param jarName Path of jar to be analyzed as String
     * @param depJars Array of File with dependencies
     */
    public void extractMetrics() {
        String jarName = reuseProject.getProjectLocation()
                + File.separator + "bin" + File.separator + projectJAR;

        extendClassPath(jarName);
        try {
            mf = new MetricsFilter(jarName);
//            Vector<ClassObject> classes = mf.getClassObjects();
//            this.updateClassRecords(classes, projectId);
            missingDepJars = mf.getDependenciesNotFound();

            //    if (missingDepJars.size() > 0) {
            //this.visualizeMissingDependencies(depNotFound);
            //      System.out.println("Could not find the following jars for the CK metrics extraction: " + depJars);
            // }

            HashMap<String, String> ditHierarchy = mf.getDITDependencies();
            this.addDITHierarchy(ditHierarchy, projectId);

        } catch (Exception e) {
            e.getMessage();
        } finally {
            System.setProperty("java.class.path", originalClassPath);
        }
    }

    public Vector<String> getMissingDependencies() {
        return missingDepJars;
    }

    private void extendClassPath(String jarName) {
        //Import jar file dependencies from reuse project lib folder        
        File fold = new File(reuseProject.getProjectLocation()
                + File.separator + "lib");
        File[] depJarsFiles = fold.listFiles();
        String finalClasspath = originalClassPath;
        finalClasspath += File.pathSeparator;
        finalClasspath += jarName;
        if (depJarsFiles != null
                && depJarsFiles.length > 0) {
            for (int j = 0; j < depJarsFiles.length; j++) {
                finalClasspath += File.pathSeparator;
                finalClasspath += depJarsFiles[j].getAbsolutePath();
            }
        }
        try {
            File temp = new File(jarName);
            ZipFile zf1 = new ZipFile(temp);
            Enumeration e1 = zf1.entries();
            while (e1.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e1.nextElement();
                if (ze.getName().endsWith(".class")) {
                    finalClasspath += File.pathSeparator;
                    //If run in Windows, change / in path to \
                    if (System.getProperty("os.name").contains("Windows")) {
                        finalClasspath += temp.getParent() + "\\" + ze.getName().replace("/", "\\");
                    } else {
                        finalClasspath += temp + "/" + ze.getName();
                    }
                }
            }
            zf1.close();
        } catch (ZipException ee) {
            System.out.println(ee.getMessage());
            System.out.println("Error setting classpath");
        } catch (IOException ee) {
        }
        for (int j = 0; j < depJarsFiles.length; j++) {
            try {
                ZipFile zf = new ZipFile(depJarsFiles[j]);
                Enumeration e = zf.entries();
                while (e.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) e.nextElement();
                    if (ze.getName().endsWith(".class")) {
                        finalClasspath += File.pathSeparator;
                        //If run in Windows, change / in path to \
                        if (System.getProperty("os.name").contains("Windows")) {
                            finalClasspath += depJarsFiles[j].getParent() + "\\" + ze.getName().replace("/", "\\");
                        } else {
                            finalClasspath += depJarsFiles[j].getParent() + "/" + ze.getName();
                        }
                    }
                }
                zf.close();
            } catch (ZipException ee) {
                System.out.println(ee.getMessage());
                System.out.println("Error setting classpath");
            } catch (IOException ee) {
            }
        }

            System.setProperty("java.class.path", finalClasspath);

    }

    public void updateClassRecords(Vector<ClassObject> classObjects, long projectID) {
        try {
            if (connection == null) {
                return;
            }
            Statement statement = connection.createStatement();
            for (int i = 0; i < classObjects.size(); i++) {
                String sql = "UPDATE classes SET WMC=" + classObjects.elementAt(i).getWMC() + ", DIT ="
                        + classObjects.elementAt(i).getDIT() + ", NOC=" + classObjects.elementAt(i).getNOC() + ",CBO=" + classObjects.elementAt(i).getCBO() + ", RFC="
                        + classObjects.elementAt(i).getRFC() + ", LCOM=" + classObjects.elementAt(i).getLCOM() + ", Ca=" + classObjects.elementAt(i).getCa() + ", NPM="
                        + classObjects.elementAt(i).getNPM() + " WHERE projectID=" + String.valueOf(projectID) + " AND name='"
                        + classObjects.elementAt(i).getClassName() + "'";
                statement.addBatch(sql);
                //int updated = statement.executeUpdate(sql);
            }
            statement.executeBatch();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void classycleAnalysis(String jarPath) {
        String[] classFiles = new String[1];
        classFiles[0] = jarPath;
        try {
            Analyser analyser = new Analyser(classFiles,
                    new TrueStringPattern(),
                    null,
                    false);
            analyser.readAndAnalyse(false);

            classycle = new ClassycleHandler(analyser,reuseProject.getProject());
            classes = classycle.getClassAnalysisObjects();
            packages = classycle.getPackageAnalysisObjects();
           
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void extractAndStoreDBClusterSizes() {
        //<ClassID,ClusterSize>
        HashMap<Long, Integer> clusterSizes = new HashMap<Long, Integer>();
        if (connection != null) {
            try {
                //first check if a package with this name and projectid already exists in the database
                String squery = "Select classid from classes where projectid=" + reuseProject.getProject().getProjectid();
                Statement query = connection.createStatement();
                ResultSet rquery = query.executeQuery(squery);
                Set<Long> innerClasses = getInnerClasses(reuseProject.getProject().getProjectid());
                while (rquery.next()) {
                    Long classid = rquery.getLong("classid");                   
                    clusterSizes.put(classid, getClusterSize(classid,innerClasses));
                }
                //Store in db
            Statement statement = connection.createStatement();
            Set<Long> keys = clusterSizes.keySet();
            for (Long key : keys){
                String sql = "UPDATE classes SET ClusterSize=" + clusterSizes.get(key) + " WHERE classid=" + String.valueOf(key);
                statement.addBatch(sql);
                //int updated = statement.executeUpdate(sql);                
            }
            statement.executeBatch();
            connection.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private int getClusterSize(Long classid,Set<Long> innerClasses) {
        Set<Long> visited = new HashSet<Long>();
        
        visited = getClusterSize(visited, classid);
        
        Set<Long> dependencies = new HashSet<Long>();
        for (Long in : visited){
            if (!innerClasses.contains(in))
                dependencies.add(in);
        }
        
        this.clusters.put(classid, dependencies);
        //after getting all the dependencies the return them
        return dependencies.size();
    }

    private Set<Long> getClusterSize(Set<Long> visited,Long classid) {
        if (visited.contains(classid))
            return visited;

        if (clusters.containsKey(classid)){
            visited.addAll(clusters.get(classid));
            return visited;
        }

        visited.add(classid);
        try {
            String squery1 = "Select dependency from classinternaldependencies where dependee=" + classid;
            Statement query1 = connection.createStatement();
            ResultSet rquery1 = query1.executeQuery(squery1);
            while (rquery1.next()) {
                    visited = getClusterSize(visited, rquery1.getLong("dependency"));
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return visited;
    }
    
    //used in Jaccard Similarity (akritiko)
    public Set<Long> getClusterIDs(Long classid,Long projectid) {
        
        Set<Long> innerClasses = getInnerClasses(projectid);
        
        Set<Long> visited = new HashSet<Long>();
        
        visited = getClusterSize(visited, classid);
        
        Set<Long> dependencies = new HashSet<Long>();
        for (Long in : visited){
            if (!innerClasses.contains(in))
                dependencies.add(in);
        }
        
        this.clusters.put(classid, dependencies);
        //after getting all the dependencies the return them
        return dependencies;
    }
    
    public Set<Long> getInnerClasses(long projectid) {
        Set<Long> inner = new HashSet<Long>();
        if (connection != null) {
            try {
                String squery1 = "Select classid,innerclass from classes where projectid=" + projectid;
                Statement query1 = connection.createStatement();
                ResultSet rquery1 = query1.executeQuery(squery1);
                while(rquery1.next())
                    if (rquery1.getBoolean("innerclass"))
                        inner.add(rquery1.getLong("classid"));
            } catch (Exception ee) {
            }
        }
        return inner;
    }
    /*
    public int getParentClassID(int innnerClassId){
        if (connection != null) {
            try {
                //first check if a package with this name and projectid already exists in the database
                String squery1 = "Select classid from classes where projectid=" + reuseProject.getProject().getProjectid() + " and name='" + getClassAnalysis(innnerClassId).getName().substring(0, getClassAnalysis(innnerClassId).getName().indexOf("$")) +"'";
                Statement query1 = connection.createStatement();
                ResultSet rquery1 = query1.executeQuery(squery1);
                while(rquery1.next())
                    return rquery1.getInt("classid");
            } catch (Exception ee) {
            }
        }
        
        return -1;
    }*/

    //Classycle related methods
    //public void importFromXMLClassycleAnalysisFile() throws Exception {
    public void storeClassycleResultsToDB() throws Exception {
    int packagesCount = 0;
    int packageDependenciesCount = 0;
    int classCount = 0;
    int dependenciesCount = 0;
    

    Set<PackageAnalysis> packagesSet = new HashSet();
        /*for (PackageAnalysis p : packages) {
            try {
                long packageId = this.addPackage(p, projectId);
                p.setPackageid(packageId);
                packagesCount++;
            } catch (Exception e) {
                System.out.println("Package Exception: " + e.getMessage());
            }
        }*/
    packagesCount = packages.size();
    this.addPackagesToDB(packages, projectId);
    packages = this.initializePackageIDS(packages, projectId);


        System.out.println("Storing package internal dependencies in the database...");
        HashMap<Long,Set<Long>> deps1 = new HashMap<Long,Set<Long>>();
        for (PackageAnalysis p : packages) {
            Set<Long> packageDeps = new HashSet();
            try {
                String packageName = p.getName();
                long packageId = this.getPackageId(packageName, projectId);
                if (packageId != 0) {
                    ArrayList<PackageAnalysis> refs = classycle.getPackageDependencies(packageName);
                    for (PackageAnalysis ref : refs) {
                       // if ("usesInternal".equals(ref.getType())) {
                            String dependencyName = ref.getName();
                            long refPackageId = this.getPackageId(dependencyName, projectId);
                            if (refPackageId != 0) {
                                //store the dependee - dependency pair
                                //int affectedRows = this.storePackageDependency(packageId, refPackageId);
                                packageDeps.add(refPackageId);
                                //                                System.out.println("\t" + dependencyName);
                                packageDependenciesCount++;
                            } else {
                                System.out.println("Package Dependencies Wrong logic! refPackageId should not be 0 here!"+dependencyName);
                            }
                       // }
                    }    
                    deps1.put(packageId, packageDeps);
                } else {
                    System.out.println("Wrong logic! packageId should not be 0 here!"+packageName);
                }
            } catch (Exception e) {
                System.out.println("Package Dependencies Exception: " + e.getMessage());
            }
        }        
        this.storePackageDependencies(deps1);
    System.out.println("Storing classes in the database...");
/*
    for (ClassAnalysis c : classes){
        try{
            long classid = this.addClass(c, projectId);
            c.setClassid(classid);
            classCount++;
        }catch (Exception e){
            System.out.println("Class Exception: " + e.getMessage()+"-"+c.getName());
        }
    }
    */
    classCount = classes.size();
    this.addClassesToDB(classes, projectId);
    classes = this.initializeClassIDS(classes, projectId);
    
    System.out.println("Storing class internal dependencies in the database...");

    HashMap<Long,Set<Long>> deps = new HashMap<Long,Set<Long>>();
        for (ClassAnalysis c : classes) {
            Set<Long> classDeps = new HashSet();
            try {
                String className = c.getName();
                //System.out.println("Dependencies of class " + className + " with the following internal classes:");
                long classId = this.getClassId(className, projectId);
                if (classId != 0) {
                    ArrayList<ClassAnalysis> refs = classycle.getClassDependencies(className);
                    for (ClassAnalysis ref : refs) {
                       // if ("usesInternal".equals(ref.getType())) {
                            String dependencyName = ref.getName();
                            long refClassId = this.getClassId(dependencyName, projectId);
                            if (refClassId != 0) {
                                //store the dependee - dependency pair
                                //int affectedRows = this.storeDependency(classId, refClassId);
                                classDeps.add(refClassId);
                                //                                System.out.println("\t" + dependencyName);
                                dependenciesCount++;
                            } else {
                                System.out.println("Class Dependencies Wrong logic! refClassId should not be 0 here!"+dependencyName);
                            }
                    //    }
                    }
                    deps.put(classId, classDeps);
                } else {
                    System.out.println("Class Dependencies  Wrong logic! classId should not be 0 here!"+className);
                }
                // System.out.println("were saved in the database or already existed there");
            } catch (Exception e) {
                System.out.println("Class Dependencies Exception: " + e.getMessage());
            }
        }
        this.storeClassDependencies(deps);
        System.out.println("****************************************");
        System.out.println("Total packages             : " + packagesCount);
        System.out.println("Total package dependencies : " + packageDependenciesCount);
        System.out.println("Total classes              : " + classCount);
        System.out.println("Total class dependencies   : " + dependenciesCount);
        System.out.println("****************************************");
    }
    
        private void storePackageDependencies(HashMap<Long,Set<Long>> deps){
        //check if this dependency already exists in the database
        Set<Long> dependeees = deps.keySet();
        try {
            Statement s = connection.createStatement();
            String sql = "insert into packageinternaldependencies values";
            for (Long dep : dependeees) {
                Set<Long> dependencies = deps.get(dep);
                for (Long dependency : dependencies) {
                    sql += "(" + dep + ", " + dependency + "),";
                }
            }
                sql = sql.substring(0, sql.length()-1);
                int result = s.executeUpdate(sql);            
        } catch (Exception ee) {
            //ee.printStackTrace();
        }
    }    
        private void storeClassDependencies(HashMap<Long,Set<Long>> deps){
        //check if this dependency already exists in the database
        Set<Long> dependeees = deps.keySet();
        try {
            Statement s = connection.createStatement();
            String sql = "insert into classinternaldependencies values";
            for (Long dep : dependeees) {
                Set<Long> dependencies = deps.get(dep);
                for (Long dependency : dependencies) {
                    sql += "(" + dep + ", " + dependency + "),";
                }
            }
                sql = sql.substring(0, sql.length()-1);
                int result = s.executeUpdate(sql);            
        } catch (Exception ee) {
            //ee.printStackTrace();
        }
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public void initializeObjects() {
                try{
                    Set<ClassAnalysis> cl = new HashSet();
        //now look for the id of this class in the database
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("from ClassAnalysis where projectid=:projectid");
        query.setParameter("projectid", reuseProject.getProject().getProjectid());
        List<ClassAnalysis> classesResults = query.list();
        session.close();
        for (ClassAnalysis c : classesResults) {
            cl.add(c);
        }
        reuseProject.getProject().setClasses(cl);
        
        query = session.createQuery("from PackageAnalysis where projectid=:projectid");
        query.setParameter("projectid", reuseProject.getProject().getProjectid());
        List<PackageAnalysis> packagesResults = query.list();
        session.close();
        Set<PackageAnalysis> pl = new HashSet();
        for (PackageAnalysis c : packagesResults) {
            pl.add(c);
        }
        reuseProject.getProject().setPackages(pl);
          }catch (Exception ee){
            
        }
    }

    private ClassAnalysis getClassAnalysis(int classid) {
        try{
        //now look for the id of this class in the database
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("from ClassAnalysis where classid=:classid");
        query.setParameter("classid", classid);
        ClassAnalysis ca = (ClassAnalysis) query.uniqueResult();
        session.close();
        return ca;
          }catch (Exception ee){
            
        }
        System.out.println("Failed at class:"+classid);
        return null;
        }
    
    private PackageAnalysis getPackageAnalysis(String packageName){
        try{
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            //get the given project's id
            Query q = session.createQuery("select from PackageAnalysis where name=:name");
            q.setParameter("name", packageName);
            PackageAnalysis pa = (PackageAnalysis) q.uniqueResult();
            session.close();
            return pa;
        }catch (Exception ee){
            
        }
        System.out.println("Failed at package:"+packageName);
        return null;
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

    //private long addPackage(ClassyclePackage p, long projectid) throws SQLException {
    private long addPackage(PackageAnalysis p, long projectid) throws SQLException {
        long id = 0;
        if (connection != null) {
            String name = p.getName();
            //first check if a package with this name and projectid already exists in the database
            String squery = "Select * from packages where projectid=" + projectid
                    + " and name='" + name + "'";
            Statement query = connection.createStatement();
            ResultSet rquery = query.executeQuery(squery);
            int rowCount = this.getRowCount(rquery);
            if (rowCount == 0) {
                String sources = projectJAR;
                Integer size = p.getPackageSize();
                Integer usedBy = p.getUsedBy();
                Integer usesInternal = p.getUsesInternal();
                Integer usesExternal = p.getUsesExternal();
                Integer layer = p.getLayer();
                String ss = "insert into packages(name, sources, size, usedBy, usesInternal, usesExternal, layer, projectid) values('"
                        + name + "' ,'" + sources + "', " + size + ", " + usedBy + ", " + usesInternal + ", " + usesExternal + ", " + layer + ", " + projectid + ")";
                Statement s = connection.createStatement();
                s.executeUpdate(ss);
                // determine the id of the package
                Statement query1 = connection.createStatement();
                ResultSet rquery1 = query1.executeQuery(squery);
                rquery1.next();
                id = rquery1.getLong("packageid");
            } else {
                while (rquery.next()) {
                    id = rquery.getLong("packageid");
                }
            }
        }
        return id;
    }
    
    private ArrayList<PackageAnalysis> initializePackageIDS(ArrayList<PackageAnalysis> pack,long projectid){
        if (connection != null) {
            try{
            HashMap<String,Long> ids = new HashMap<String,Long>();
            //first check if a package with this name and projectid already exists in the database
            String squery = "Select packageid,name from packages where projectid=" + projectid;
            Statement query = connection.createStatement();
            ResultSet rquery = query.executeQuery(squery);
            while (rquery.next()) {
                ids.put(rquery.getString("name"),rquery.getLong("packageid"));
            }
            
            for (int i=0;i<pack.size();i++){
                pack.get(i).setPackageid(ids.get(pack.get(i).getName()));
            }
            }catch(Exception ee){
                
            }
        }
        
        return pack;
    }
    
    private ArrayList<ClassAnalysis> initializeClassIDS(ArrayList<ClassAnalysis> pack,long projectid){
        if (connection != null) {
            try{
            HashMap<String,Long> ids = new HashMap<String,Long>();
            //first check if a package with this name and projectid already exists in the database
            String squery = "Select classid,name from classes where projectid=" + projectid;
            Statement query = connection.createStatement();
            ResultSet rquery = query.executeQuery(squery);
            while (rquery.next()) {
                ids.put(rquery.getString("name"),rquery.getLong("classid"));
            }
            
            for (int i=0;i<pack.size();i++){
                pack.get(i).setClassid(ids.get(pack.get(i).getName()));
            }
            }catch(Exception ee){
                
            }
        }
        
        return pack;
    }    
    
    private void addPackagesToDB(ArrayList<PackageAnalysis> pack,long projectid){
        //check if this dependency already exists in the database
        try {
            Statement s = connection.createStatement();
            String sql1 = "select packageid,name from packages where projectid="+projectid;
            ResultSet rs = s.executeQuery(sql1);
            HashMap<String,Long> existing = new HashMap<String,Long>();
            while (rs.next()){
                existing.put(rs.getString("name"),rs.getLong("packageid"));
            }
            String sql = "insert into packages(name, sources, size, usedBy, usesInternal, usesExternal, layer, projectid) values";
            boolean changed = false;
            for (PackageAnalysis pa : pack) {
                if (!existing.containsKey(pa.getName())){
                    changed = true;
                    sql += "('"
                        + pa.getName() + "' ,'" + pa.getSources() + "', " + pa.getPackageSize() + ", " + pa.getUsedBy() + ", " + pa.getUsesInternal() + ", " + pa.getUsesExternal() + ", " + pa.getLayer() + ", " + projectid + "),";
                }
                else{
                    String update = "UPDATE packages SET name='"+pa.getName()+"',sources='"+pa.getSources()+"',"+
                            "size="+pa.getPackageSize()+",usedby="+pa.getUsedBy()+",usesInternal="+pa.getUsesInternal()
                                    +",usesExternal="+pa.getUsesExternal()+",layer="+pa.getLayer()+",projectid="+projectid+" where packageid="
                                    +existing.get(pa.getName());
                    s.addBatch(update);                    
                }
            }
            if (changed){
                sql = sql.substring(0, sql.length()-1);
                int result = s.executeUpdate(sql); 
            }
                s.executeBatch();                
                 
        } catch (Exception ee) {
            ee.printStackTrace();
        }        
    }
    
    private void addClassesToDB(ArrayList<ClassAnalysis> pack,long projectid){
        //check if this dependency already exists in the database
        try {
            Statement s = connection.createStatement();
            String sql1 = "select classid,name from classes where projectid="+projectid;
            ResultSet rs = s.executeQuery(sql1);
            HashMap<String,Long> existing = new HashMap<String,Long>();
            while (rs.next()){
                existing.put(rs.getString("name"),rs.getLong("classid"));
            }
            boolean changed = false;
            String sql = "insert into classes(name, sources, type, innerclass, size, usedby, usesinternal, usesexternal, layer, projectid, packageid) values";
            for (ClassAnalysis ca : pack) {
                if (!existing.containsKey(ca.getName())){
                    changed = true;
                    sql += " ('"
                        + ca.getName()
                        + "','"
                        + ca.getSources()
                        + "','"
                        + ca.getType()
                        + "',"
                        + ca.getInnerclass()
                        + ","
                        + ca.getClassSize()
                        + ","
                        + ca.getUsedby()
                        + ","
                        + ca.getUsesinternal()
                        + ","
                        + ca.getUsesexternal()
                        + ","
                        + ca.getLayer()
                        + ","
                        + projectid
                        + ","
                        + getPackageIdFromClassName(ca.getName(), projectid)
                        + "),";
                }else{
                    String update = "UPDATE classes SET name='"+ca.getName()+"',sources='"+ca.getSources()+"',type='"+ca.getType()+"',"
                            + "innerclass="+ca.getInnerclass()+",size="+ca.getClassSize()+",usedby="+ca.getUsedby()+",usesinternal="+ca.getUsesinternal()
                                    +",usesexternal="+ca.getUsesexternal()+",layer="+ca.getLayer()+",projectid="+projectid+",packageid="+getPackageIdFromClassName(ca.getName(), projectid)+" where classid="
                                    +existing.get(ca.getName());
                    s.addBatch(update);
                }
            }
            if (changed){
                sql = sql.substring(0, sql.length()-1);
                int result = s.executeUpdate(sql); 
            }            
                
                s.executeBatch();
                
                
        } catch (Exception ee) {
            ee.printStackTrace();
        }        
    }    

    //private long addClass(ClassycleClass c, long projectid) throws SQLException {
    private long addClass(ClassAnalysis c, long projectid) throws SQLException {
        long id = 0;
        if (connection != null) {
            String name = c.getName();
            // first check if a class with this class name already exists in the
            // database
            String squery = "Select * from classes where projectid="
                    + projectid + " and name='" + name + "'";
            Statement query = connection.createStatement();
            ResultSet rquery = query.executeQuery(squery);
            int rowCount = this.getRowCount(rquery);
            if (rowCount == 0) {
                String sources = projectJAR;
                String stype = c.getType();
                boolean innerclass = c.getInnerclass();
                Long size = c.getClassSize();
                Long usedBy = c.getUsedby();
                Long usesInternal = c.getUsesinternal();
                Long usesExternal = c.getUsesexternal();
                Long layer = c.getLayer();
                long packageId = this.getPackageIdFromClassName(name, projectid);
                String ss = "insert into classes(name, sources, type, innerclass, size, usedby, usesinternal, usesexternal, layer, projectid, packageid) values('"
                        + name
                        + "','"
                        + sources
                        + "','"
                        + stype
                        + "',"
                        + innerclass
                        + ","
                        + size
                        + ","
                        + usedBy
                        + ","
                        + usesInternal
                        + ","
                        + usesExternal
                        + ","
                        + layer
                        + ","
                        + projectid
                        + ","
                        + packageId
                        + ")";
                Statement s = connection.createStatement();
                s.executeUpdate(ss);
                // determine the id of the class
                Statement query1 = connection.createStatement();
                ResultSet rquery1 = query1.executeQuery(squery);
                rquery1.next();
                id = rquery1.getLong("classid");
            } else {
                while (rquery.next()) {
                    id = rquery.getLong("classid");
                }
                //REMOVE THIS
                long packageId = this.getPackageIdFromClassName(name, projectid);
                String updateSql = "Update classes set packageid=" + packageId
                        + " where classid=" + id;
                Statement updateStatement = connection.createStatement();
                updateStatement.executeUpdate(updateSql);
            }
        }
        return id;
    }

    private long getClassId(String className, long projectid) throws SQLException {
        long classId = 0;
        if (connection != null) {
            String text = "Select classid from classes where name='" + className + "' and projectid=" + projectid;
            Statement query = connection.createStatement();
            ResultSet rs = query.executeQuery(text);
            if (rs.next()) {
                return rs.getLong(1);

            }
        }
        return classId;
    }

    private long getPackageId(String packageName, long projectid) throws SQLException {
        long packageId = 0;
        if (connection != null) {
            String text = "Select packageid from packages where name='" + packageName + "' and projectid=" + projectid;
            Statement query = connection.createStatement();
            ResultSet rs = query.executeQuery(text);
            if (rs.next()) {
                return rs.getLong(1);

            }
        }
        return packageId;
    }

    private long getPackageIdFromClassName(String className, Long projectid) {
        try {
            int lastDotPosition = className.lastIndexOf('.');
            String packageName;
            if (lastDotPosition>=0)
                packageName = className.substring(0, lastDotPosition);
            else 
                packageName = "(default package)";
            String sql = "Select packageid from packages where name='" + packageName + "' and projectid=" + projectid;
            Statement query = connection.createStatement();
            ResultSet result = query.executeQuery(sql);
            if (result.next()) {
                return result.getLong(1);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Determines the number of rows in a <code>ResultSet</code>. Upon exit, if
     * the cursor was not currently on a row, it is just before the first row in
     * the result set (a call to {@link ResultSet#next()} will go to the first
     * row).
     *
     * @param set The <code>ResultSet</code> to check (must be scrollable).
     * @return The number of rows.
     * @throws SQLException If the <code>ResultSet</code> is not scrollable.
     */
    private int getRowCount(ResultSet set) throws SQLException {
        int rowCount;
        int currentRow = set.getRow(); // Get current row
        rowCount = set.last() ? set.getRow() : 0; // Determine number of rows
        if (currentRow == 0) // If there was no current row
        {
            set.beforeFirst(); // We want next() to go to first row
        } else // If there WAS a current row
        {
            set.absolute(currentRow); // Restore it
        }
        return rowCount;
    }

    private int storeDependency(long dependee, long dependency) throws SQLException {
        int affectedRows = 0;
        if (connection != null) {
            //check if this dependency already exists in the database
            long classId = 0;

            String text = "Select * from classinternaldependencies where dependee=" + dependee + " and dependency=" + dependency;
            Statement query = connection.createStatement();
            ResultSet rs = query.executeQuery(text);
            if (this.getRowCount(rs) == 0) {
                Statement s = connection.createStatement();
                String sql = "insert into classinternaldependencies values(" + dependee + ", " + dependency + ")";
                affectedRows = s.executeUpdate(sql);
            }
        }
        return affectedRows;
    }

    private void addDITHierarchy(HashMap<String, String> ditHier, long projectid) throws SQLException {
        long id = 0;
        int aa;
        if (connection != null) {
            Iterator it = ditHier.keySet().iterator();
            while (it.hasNext()) {
                String superclass = it.next().toString();
                String subclass = ditHier.get(superclass);
                String ss = "insert into dithierarchy(superclass, subclass, projectid) values('" + superclass + "' ,'" + subclass + "',  " + projectid + ")";
                // System.out.println(ss);
                Statement s = connection.createStatement();
                aa = s.executeUpdate(ss);
                //System.out.println("added lines: " + aa);
            }
        }
    }

    private int storePackageDependency(long dependee, long dependency) throws SQLException {
        int affectedRows = 0;
        if (connection != null) {
            //check if this dependency already exists in the database
            long packageId = 0;

            String text = "Select * from packageinternaldependencies where dependee=" + dependee + " and dependency=" + dependency;
            Statement query = connection.createStatement();
            ResultSet rs = query.executeQuery(text);
            if (this.getRowCount(rs) == 0) {
                Statement s = connection.createStatement();
                String sql = "insert into packageinternaldependencies values(" + dependee + ", " + dependency + ")";
                affectedRows = s.executeUpdate(sql);
            }
        }
        return affectedRows;
    }
    /*
    public static void main(String[] args) {
    //get the classycle XML file path from args
    if (args.length != 1) {
    System.out.println("It appears you have not provided a classycle analysis XML file path");
    System.out.println("Usage: java -jar classycledbimporter.jar <classycle analysis file path>");
    System.exit(0);
    }
    try {
    ClassycleDBImporter importer = new ClassycleDBImporter(args[0]);
    importer.importFromXMLClassycleAnalysisFile();
    } catch (Exception e) {
    System.out.println(e.getMessage());
    e.printStackTrace();
    }
    
    }*/
}

    /*JAXBContext jc = JAXBContext.newInstance("eu.opensme.cope.factgenerators.dependenciesgeneratorxmlschema");
    Unmarshaller u = jc.createUnmarshaller();
        Classycle classycle = (Classycle) u.unmarshal(new FileInputStream(
        classycleXMLFilePath));*/


        /*ClassyclePackages packages = classycle.getPackages();
        List<ClassyclePackage> listOfPackages = packages.getPackage();
        for (ClassyclePackage p : listOfPackages) {
        try {
        long packageId = this.addPackage(p, projectId);
        //System.out.println("ClassyclePackage " + p.getName() + " was saved or verified that already exists in DB with id = "
        //+ packageId);
        packagesCount++;
        } catch (Exception e) {
        System.out.println("Exception: " + e.getMessage());
        }
        }*/
        /*System.out.println("Storing package internal dependencies in the database...");
        for (ClassyclePackage p : listOfPackages) {
            try {
                String packageName = p.getName();
                //                System.out.println("Dependencies of package " + packageName + " with the following internal packages:");
                long packageId = this.getPackageId(packageName, projectId);
                if (packageId != 0) {
                    List<PackageRef> refs = p.getPackageRef();
                    for (PackageRef ref : refs) {
                        if ("usesInternal".equals(ref.getType())) {
                            String dependencyName = ref.getName();
                            long refPackageId = this.getPackageId(dependencyName, projectId);
                            if (refPackageId != 0) {
                                //store the dependee - dependency pair
                                int affectedRows = this.storePackageDependency(packageId, refPackageId);
                                //                                System.out.println("\t" + dependencyName);
                                packageDependenciesCount++;
                            } else {
                                System.out.println("Wrong logic! refPackageId should not be 0 here!");
                            }
                        }
                    }
                } else {
                    System.out.println("Wrong logic! packageId should not be 0 here!");
                }
                //                System.out.println("were saved in the database or already existed there");
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }*/
    /*Classes classesElement = classycle.getClasses();
    List<ClassycleClass> classes = classesElement.getClazz();
    for (ClassycleClass c : classes) {
    try {
    long classId = this.addClass(c, projectId);
    //              System.out.println("ClassycleClass " + c.getName() + " was saved or verified that already exists in DB with id = "
    //                        + classId);
    classCount++;
    } catch (Exception e) {
    System.out.println("Exception: " + e.getMessage());
    }
    }*/
    /*for (ClassycleClass c : classes) {
    try {
    String className = c.getName();
    //System.out.println("Dependencies of class " + className + " with the following internal classes:");
    long classId = this.getClassId(className, projectId);
    if (classId != 0) {
    List<ClassRef> refs = c.getClassRef();
    for (ClassRef ref : refs) {
    if ("usesInternal".equals(ref.getType())) {
    String dependencyName = ref.getName();
    long refClassId = this.getClassId(dependencyName, projectId);
    if (refClassId != 0) {
    //store the dependee - dependency pair
    int affectedRows = this.storeDependency(classId, refClassId);
    //                                System.out.println("\t" + dependencyName);
    dependenciesCount++;
    } else {
    System.out.println("Wrong logic! refClassId should not be 0 here!");
    }
    }
    }
    } else {
    System.out.println("Wrong logic! classId should not be 0 here!");
    }
    // System.out.println("were saved in the database or already existed there");
    } catch (Exception e) {
    System.out.println("Exception: " + e.getMessage());
    }
    }*/