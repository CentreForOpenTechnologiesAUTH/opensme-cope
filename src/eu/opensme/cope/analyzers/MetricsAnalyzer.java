/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.DependenciesGenerator;
import java.io.File;
import java.util.Vector;

/**
 *
 * @author auth
 */
public class MetricsAnalyzer extends Analyzer {
    private DependenciesGenerator depGen;

    public MetricsAnalyzer(ReuseProject reuseProject) {
        super(reuseProject);
    }

    @Override
    public boolean analyze() {

        String projectTitle = reuseProject.getProject().getProjecttitle();
        String projectLocation = reuseProject.getProjectLocation();


        try {
            depGen = new DependenciesGenerator(reuseProject/*, projectLocation + File.separator + projectTitle + ".xml"*/);
            depGen.extractMetrics();

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return true;
    }

    @Override
    public void storeData() {
        try {
            depGen.saveMetrics();
            depGen.initializeObjects();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    public Vector<String> getMissingDependencies(){
        return depGen.getMissingDependencies();
    }
}
