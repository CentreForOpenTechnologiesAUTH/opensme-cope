/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.recommenders.entities;

import eu.opensme.cope.domain.ReuseProject;
import java.util.ArrayList;

/**
 *
 * @author auth
 */
public class PatternClassCluster extends ClassCluster{
    public PatternClassCluster(String clustersName, ArrayList<ClassClusterPartcipant> clusterParticipants, ReuseProject reuseProject) {
        super(clustersName, clusterParticipants, reuseProject);
    }
    
public boolean isPatternBased() {
    return true;
}

    
}
