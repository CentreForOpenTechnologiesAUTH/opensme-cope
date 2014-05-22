/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.recommenders;

import eu.opensme.cope.analyzers.patternanalyzer.PatternInstance;
import eu.opensme.cope.analyzers.patternanalyzer.PatternList;
import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.util.HibernateUtil;
import eu.opensme.cope.recommenders.entities.ClassCluster;
import eu.opensme.cope.recommenders.entities.ClassClusterPartcipant;

import java.io.File;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import eu.opensme.cope.recommenders.entities.PatternClassCluster;
import java.util.ArrayList;

/**
 *
 * @author apostolis
 */
public class PatternRecommender extends Recommender {

    public PatternRecommender(ReuseProject reuseProject) {
        super(reuseProject);
        String dirName = reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "patClusters";
        clustersExportDir = new File(dirName);
    }

    private ArrayList<ClassClusterPartcipant> clusterNodesToClassParticipants(PatternInstance pi) {

        //For each of the nodes of the cluster create copy the 
        //corresponding source file in the directory of the cluster

        ArrayList<ClassAnalysis> cas = new ArrayList<ClassAnalysis>();
        ArrayList<ClassClusterPartcipant> caCollection = new ArrayList<ClassClusterPartcipant>();

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();

        for (int i = 0; i < pi.getSize(); i++) {
            String className = pi.getParticipant(i).getClassName();
            String classRole = pi.getParticipant(i).getClassRole();

            //Find the corresponding ClassAnalysis object
            String q = "from ClassAnalysis ca where ca.project=:project and ca.name=:name";
            Query query = session.createQuery(q);
            query.setParameter("project", project);
            query.setParameter("name", className);

            ClassAnalysis temp = (ClassAnalysis) query.uniqueResult();
            cas.add(temp);
            double specificity = calculateClassesSpecificity(cas.get(i), cas);
            ClassClusterPartcipant tempCP = new ClassClusterPartcipant(cas.get(i), specificity, classRole);
            caCollection.add(tempCP);
        }
        session.close();

        return caCollection;
    }

    public void createClusters() {
        clusters = new ArrayList<ClassCluster>();

        PatternList clusts = new PatternList(this.reuseProject);

        for (int i = 0; i < clusts.getSize(); i++) {

            PatternInstance pi = clusts.getPattern(i);

            //create the cluster directory
            File clusterDir = new File(clustersExportDir + File.separator + pi.getName(i));

            clusterDir.mkdir();

            //extract cluster participants from bunch
            ArrayList<ClassClusterPartcipant> clusterMembers = clusterNodesToClassParticipants(pi);

            //create the class cluster 
            ClassCluster cc = new PatternClassCluster(pi.getName(i), clusterMembers, reuseProject);

            cc.createPhysicalCluster(clusterDir);

            //exports UMLGraph
            cc.createClustersUMLGraph(clusterDir);

            //adds cluster to collection
            if (!clusterExist(cc)) {
                clusters.add(cc);
            }
        }
        reuseProject.addClusters(clusters);
    }

    //angor 23/12/2011
    private boolean clusterExist(ClassCluster cluster) {
        for (ClassCluster c : reuseProject.getClusters()) {
            if (c.getName().equals(cluster.getName())) {
                return true;
            }
        }
        return false;
    }
}
