/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.domain;

import eu.opensme.cope.recommenders.entities.ClassCluster;
import eu.opensme.cope.recommenders.entities.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.hibernate.HibernateException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author george
 */
public class ReuseProject implements Serializable {

    /**
     * The reuse project folder in the hard disk
     */
    private String projectLocation;
    /**
     * The reuse project name
     */
    private String projectName;
    /**
     * The reuse project jar filename. The JAR file is needed for
     * static analysis tools like classycle and others.
     */
    private String projectJARFilename;
    /**
     * The reuse project repository URL. This is needed for getting
     * the history of commits for the OSS project
     */
    private String repoURL;
    /**
     * The dependencies of this project (a collection of JAR files)
     */
    private List<String> dependenciesJARs;
    /**
     * The source code directory for this project
     */
    private String srcDir;
    /**
     * The project entity in the database for this reuse project
     */
    private transient Project project;
    private List<ClassCluster> clusters;
    private HashMap<String, GeneratedComponent> generatedComponents;
    private static String reuseProjectsLocation = System.getProperty("user.home");
    private boolean staticAnalysisPerformed;
    
    public ReuseProject(String projectLocation, String projectName,
            String projectJARFilename, String repoURL, List<String> dependenciesJARs,
            String srcDir) {
        this.projectJARFilename = projectJARFilename;
        this.projectLocation = projectLocation;
        this.projectName = projectName;
        this.repoURL = repoURL;
        this.dependenciesJARs = dependenciesJARs;
        this.srcDir = srcDir;
        this.clusters = new ArrayList<ClassCluster>();
        this.generatedComponents = new HashMap<String, GeneratedComponent>();
        this.staticAnalysisPerformed = false;
    }

    /**
     * Creates a reuse project by inserting a new project record in the database
     * and by creating a home directory for the reuse project in which:
     * <ul>
     * <li> It writes a property file with the project values (e.g. the project name) </li>
     * <li> It copies the jar file of the project in the Project_home/bin directory </li>
     * <li> It copies the jar files on which the project depends in the Project_home/lib directory </li>
     * </ul>
     * @return true if the project is created succesfully or false if the project directory already exists
     * @throws IOException
     * @throws HibernateException
     */
    public boolean createReuseProject() throws IOException, HibernateException {
        //create the project's directory if it doesn't exist
        File projectDirectory = new File(projectLocation);
        //if the directory exists we don't create it again
        if (projectDirectory.exists()) {
            return false;
        }

        //create a Project entity and save it in the database
        project = new Project();
        project.setProjecttitle(projectName);
        project.save();

        //otherwise we create a new directory
        projectDirectory.mkdir();

        //and in this directory we create a text file with the project's
        //properties
        Properties props = new Properties();
        props.setProperty("projectName", projectName);
        File jarFile = new File(projectJARFilename);
        props.setProperty("projectJARFilename", jarFile.getName());
        projectJARFilename = jarFile.getName();
        props.setProperty("repoURL", repoURL);
        props.setProperty("projectDBID", project.getProjectid().toString());
        String dependenciesString = "";
        List<File> dependenciesJARFiles = new ArrayList<File>();
        for (String dependencyJAR : dependenciesJARs) {
            File f = new File(dependencyJAR);
            dependenciesString += f.getName() + ";";
            dependenciesJARFiles.add(f);
        }
        props.setProperty("dependenciesJARs", dependenciesString);
        props.setProperty("srcDir", srcDir);

        //copy jar file and dependencies jars in the project folder
        String binDirectoryName = projectDirectory.getAbsolutePath() + File.separator + "bin";
        String libDirectoryName = projectDirectory.getAbsolutePath() + File.separator + "lib";
        //create the two directories
        File binDirectory = new File(binDirectoryName);
        File libDirectory = new File(libDirectoryName);
        binDirectory.mkdir();
        libDirectory.mkdir();
        //copy the actual files in the project's directories
        //first copy the application's jar file in the bin directory
        FileInputStream jarFileIS = new FileInputStream(jarFile);
        File jarCopy = new File(projectDirectory.getAbsolutePath() + File.separator + "bin"
                + File.separator + jarFile.getName());
        FileOutputStream jarCopyIS = new FileOutputStream(jarCopy);
        IOUtils.copy(jarFileIS, jarCopyIS);
        this.projectJARFilename = jarCopy.getName();
        props.setProperty("projectJARFilename", jarCopy.getName());
        //then copy all the dependencies in the lib directory
        dependenciesString = "";
        ArrayList<String> newDependenciesJARs = new ArrayList<String>();
        for (File djarFile : dependenciesJARFiles) {
            FileInputStream djarFileIS = new FileInputStream(djarFile);
            File djarCopy = new File(projectDirectory.getAbsolutePath() + File.separator + "lib"
                    + File.separator + djarFile.getName());
            FileOutputStream djarCopyIS = new FileOutputStream(djarCopy);
            IOUtils.copy(djarFileIS, djarCopyIS);
            newDependenciesJARs.add(djarCopy.getName());
            dependenciesString += djarCopy.getName() + ";";
        }
        this.dependenciesJARs = newDependenciesJARs;
        props.setProperty("dependenciesJARs", dependenciesString);
        props.store(new FileOutputStream(new File(projectLocation + File.separator + projectName + ".properties")),
                "Properties of the " + projectName + " project");
        return true;
    }

    public static ReuseProject openReuseProject(Project p) {
        String pdir = ReuseProject.getReuseProjectsLocation() + p.getProjecttitle();
        //get the home directory of the given project
        File projectDirectory = new File(pdir);
        if (!projectDirectory.exists()) {
            return null;
        }
        try {
            Properties props = new Properties();
            props.load(
                    new FileInputStream(
                    pdir + File.separator + p.getProjecttitle() + ".properties"));

            String pName = props.getProperty("projectName");
            String pJarFilename = props.getProperty("projectJARFilename");
            String prepoURL = props.getProperty("repoURL");
            String projectDBID = props.getProperty("projectDBID");
            String pJarDependencies = props.getProperty("dependenciesJARs");
            String[] pAJarDependencies = pJarDependencies.split(";");
            List<String> pLJarDependencies = Arrays.asList(pAJarDependencies);
            String psrcDir = props.getProperty("srcDir");
            ReuseProject reuseProject;

            reuseProject = ReuseProject.deserialize(pdir, pName);
            if (reuseProject == null) {
                reuseProject = new ReuseProject(pdir, pName,
                        pJarFilename, prepoURL, pLJarDependencies, psrcDir);
            }
            reuseProject.setProject(p);
            return reuseProject;
        } catch (IOException ioe) {
            return null;
        }
    }
    
    public static void serialize(ReuseProject reuseProject) {
        ObjectOutputStream out = null;
        try {
            File file = new File(reuseProject.getProjectLocation() + File.separator + reuseProject.getProjectName() + ".dat");
            if(file.exists())
                file.delete();
            file.createNewFile();
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(reuseProject);
            out.close();
        } catch (IOException ex) {
            System.out.println("Serialization not performed");
        }
    }

    public static ReuseProject deserialize(String projectDir, String projectTitle) {
        ObjectInputStream in = null;
        ReuseProject reuseProject = null;
        try {
            in = new ObjectInputStream(new FileInputStream(projectDir + File.separator + projectTitle + ".dat"));
            reuseProject = (ReuseProject) in.readObject();
            in.close();
        } catch (IOException ex) {
            System.out.println("Deserialization not performed");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return reuseProject;
    }

    public GeneratedComponent getComponent(String componentName) {
        if (this.generatedComponents.containsKey(componentName)) {
            return this.generatedComponents.get(componentName);
        } else {
            return null;
        }
    }

    public Set<String> getComponentNames() {
        return this.generatedComponents.keySet();
    }

    public void addGeneratedComponent(GeneratedComponent component) {
        this.generatedComponents.put(component.getComponentName(), component);
    }

    public void removeGeneratedComponent(String componentName) {
        this.generatedComponents.remove(componentName);
    }
    
    public String getProjectJARFilename() {
        return projectJARFilename;
    }

    public boolean isStaticAnalysisPerformed() {
        return staticAnalysisPerformed;
    }

    public void setStaticAnalysisPerformed(boolean staticAnalysisPerformed) {
        this.staticAnalysisPerformed = staticAnalysisPerformed;
    }

    public void setProjectJARFilename(String projectJARFilename) {
        this.projectJARFilename = projectJARFilename;
    }

    public String getProjectLocation() {
        return projectLocation;
    }

    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public List<String> getDependenciesJARs() {
        return dependenciesJARs;
    }

    public void setDependenciesJARs(List<String> dependenciesJARs) {
        this.dependenciesJARs = dependenciesJARs;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public List<ClassCluster> getClusters() {
        return clusters;
    }

    public void addClusters(List<ClassCluster> clusters) {
        this.clusters.addAll(clusters);
    }

    public void addCluster(ClassCluster cluster) {
        for (ClassCluster aCluster : clusters) {
            if (aCluster.getName().equals(cluster.getName())) {
                clusters.remove(aCluster);
                break;
            }
        }
        clusters.add(cluster);
    }

    public static String getReuseProjectsLocation() {
        return reuseProjectsLocation.endsWith(File.separator) ? reuseProjectsLocation : reuseProjectsLocation + File.separator;
    }

    public static void setReuseProjectsLocation(String reuseProjectsLocation) {
        ReuseProject.reuseProjectsLocation = reuseProjectsLocation;
    }
    
    public HashMap<String, GeneratedComponent> getGeneratedComponents() {
        return generatedComponents;
    }
}
