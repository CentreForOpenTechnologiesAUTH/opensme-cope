package eu.opensme.cope.recommenders.entities;

import eu.opensme.cope.domain.ReuseProject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.tools.javadoc.*;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.Writer;

/**
 *
 * {@author Apostolos Kritikos} <akritiko@csd.auth.gr>
 */
public abstract class ClassCluster implements Serializable{

    private ArrayList<ClassClusterPartcipant> clusterParticipants;
    private String name;
    private ReuseProject reuseProject;

    /**
     * Empty constructor
     */
    public ClassCluster() {
    }

    /**
     * 
     * @param name
     * @param clusterParticipants
     * @param reuseProject 
     */
    public ClassCluster(String name, ArrayList<ClassClusterPartcipant> clusterParticipants, ReuseProject reuseProject) {
        this.name = name;
        this.clusterParticipants = clusterParticipants;
        this.reuseProject = reuseProject;
    }

    /**
     * 
     * @return 
     */
    public ArrayList<ClassClusterPartcipant> getClusterParticipants() {
        return clusterParticipants;
    }

    /**
     * 
     * @param clusterParticipants 
     */
    public void setClusterParticipants(ArrayList<ClassClusterPartcipant> clusterParticipants) {
        this.clusterParticipants = clusterParticipants;
    }

    public abstract boolean isPatternBased();
    
    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return 
     */
    public ReuseProject getReuseProject() {
        return reuseProject;
    }

    /**
     * 
     * @param reuseProject 
     */
    public void setReuseProject(ReuseProject reuseProject) {
        this.reuseProject = reuseProject;
    }
    
    //TODO: to be deleted if not needed!
    public void exportCSV() {
        String path = "/home/krap/" + this.name + ".csv";

        try {
            FileWriter writer = new FileWriter(path);

            writer.append("Class name");
            writer.append(", ");
            writer.append("Package");
            writer.append(", ");
            writer.append("Layer");
            writer.append(", ");
            writer.append("Frequency");
            writer.append("\n");

            for (int i = 0; i < this.clusterParticipants.size(); i++) {
                writer.append(clusterParticipants.get(i).getClassAnalysis().getName());
                writer.append(", ");
                writer.append("" + clusterParticipants.get(i).getClassAnalysis().getPackageAnalysis().getPackageid());
                writer.append(", ");
                writer.append("" + clusterParticipants.get(i).getClassAnalysis().getLayer());
                writer.append(", ");
                writer.append("" + clusterParticipants.get(i).getSpecificity());
                writer.append("\n");
            }

            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(ClassCluster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param clusterDir
     * @param reuseProject 
     */
    public String createPhysicalCluster(File clusterDir) {
        //physically create the directory
        clusterDir.mkdir();

        String s = null;

        for (int i = 0; i < this.clusterParticipants.size(); i++) {
            ClassAnalysis currentClassAnalysis = clusterParticipants.get(i).getClassAnalysis();
            try {
                s = currentClassAnalysis.copySrcFileToComponentFile(currentClassAnalysis, reuseProject.getSrcDir(), clusterDir.getAbsolutePath());
            } catch (IOException ex) {
                logMessage("Class: " + clusterParticipants.get(i).getClassAnalysis() + " not found", new File(clusterDir.getAbsolutePath() + File.separator + "log.txt"));
            }
        }
        return s;
    }

    public void createClustersUMLGraph(File clusterDir) {

        List<String> umlGraphOptions = new ArrayList<String>();
        umlGraphOptions.add("-private");
        umlGraphOptions.add("-doclet");
        umlGraphOptions.add("org.umlgraph.doclet.UmlGraph");
        //umlGraphOptions.add("-inferdep");
        //umlGraphOptions.add("-inferrel");
        umlGraphOptions.add("-quiet");
        umlGraphOptions.add("-hide");
        umlGraphOptions.add("java.*");
        umlGraphOptions.add("-d");
        umlGraphOptions.add(clusterDir.getAbsolutePath());

        //copy the classAnalysis file to the cluster directory
        //String s = createPhysicalCluster(clusterDir);
        for (ClassClusterPartcipant cp : this.clusterParticipants) {
            String filename = clusterDir.getAbsolutePath()+
                    File.separator+"src"+File.separator;
            String className=cp.getClassAnalysis().getName();
            className = className.replace(".", File.separator);
            filename+=className+".java";
            umlGraphOptions.add(filename);

        }


        //Produce UML diagrams for cluster
        //Spinelis UMLGraph output
        String[] umlGraphOptionsArray = umlGraphOptions.toArray(new String[0]);
        Main.execute(umlGraphOptionsArray);
        Runtime rt = Runtime.getRuntime();
        String[] args1 = {
            "dot", "-Tpng", clusterDir.getAbsolutePath() + File.separator + "graph.dot",
            "-o", clusterDir.getAbsolutePath() + File.separator + "graph.png"};
        Process p = null;
        try {
            p = rt.exec(args1);
            p.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(ClassCluster.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClassCluster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TODO: should this become a separate class in util package || could we use Logger / Log4j to replace this functionality?
    public void logMessage(String message, File logPath) {
        Writer output = null;

        try {
            output = new BufferedWriter(new FileWriter(logPath));
            output.append(message);
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(ClassCluster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TODO: to be removed if ComputeDependenciesChangeFrequencies is not going to be used again
    public boolean findClassAnalysisObject(ClassAnalysis victim) {
        for (int i = 0; i < clusterParticipants.size(); i++) {
            if (clusterParticipants.get(i).getClassAnalysis().equals(victim)) {
                return true;
            }
        }
        return false;
    }
}
