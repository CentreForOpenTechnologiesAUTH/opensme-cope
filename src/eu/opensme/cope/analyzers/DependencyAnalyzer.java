/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers;

import eu.opensme.cope.analyzers.dependencyTypeAnalyzer.DependencyTypeGenerator;
import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.DependenciesGenerator;
import java.io.File;

/**
 *
 * @author auth
 */
public class DependencyAnalyzer extends Analyzer {
    private DependenciesGenerator depGen;
    
    public DependencyAnalyzer(ReuseProject reuseProject) {
        super(reuseProject);
        try {
            depGen = new DependenciesGenerator(reuseProject);
        } catch (Exception ex) {
        }
    }

    @Override
    public boolean analyze() {
        // Long projectId = reuseProject.getProject().getProjectid();
        String projectTitle = reuseProject.getProject().getProjecttitle();
        String projectJAR = reuseProject.getProjectJARFilename();
        String projectLocation = reuseProject.getProjectLocation();
        depGen.classycleAnalysis(projectLocation + File.separator + "bin" + File.separator + projectJAR);
        //Initialize progress property.
        /*String userDir = System.getProperty("user.home");
        String cmd;
        if (System.getProperty("os.name").contains("Windows")) {
            cmd = "java -jar \"" + userDir + File.separator + "classycle.jar\" -xmlFile=\"" + projectLocation + File.separator + projectTitle + ".xml\" \"" + projectLocation + File.separator + "bin" + File.separator + projectJAR + "\"";
        } else {
            cmd = "java -jar " + userDir + File.separator + "classycle.jar -xmlFile=" + projectLocation + File.separator + projectTitle + ".xml " + projectLocation + File.separator + "bin" + File.separator + projectJAR;
        }
        System.out.println(cmd);
        CommandLineUtil myCL = new CommandLineUtil(cmd);
        myCL.executeInConsole();*/
        return true;
    }

    @Override
    public void storeData() {
        // Long projectId = reuseProject.getProject().getProjectid();
        String projectTitle = reuseProject.getProject().getProjecttitle();
        //String projectJAR = reuseProject.getProjectJARFilename();
        String projectLocation = reuseProject.getProjectLocation();

        try {
            //depGen = new DependenciesGenerator(reuseProject/*, projectLocation + File.separator + projectTitle + ".xml"*/);
            depGen.storeClassycleResultsToDB();
            depGen.extractAndStoreDBClusterSizes();
            //depGen.importFromXMLClassycleAnalysisFile();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
}
