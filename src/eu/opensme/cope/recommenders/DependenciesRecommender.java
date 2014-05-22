/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.recommenders;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.util.HibernateUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import bunch.api.*;

import eu.opensme.cope.recommenders.entities.ClassCluster;
import eu.opensme.cope.recommenders.entities.ClassClusterPartcipant;
import eu.opensme.cope.recommenders.entities.DependenciesClassCluster;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 * @author george
 */
public class DependenciesRecommender extends Recommender {

    private File mdgFile;

    public DependenciesRecommender(ReuseProject reuseProject) {
        super(reuseProject);
        clustersExportDir = new File(reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "depClusters");
    }

    public void createClusters() throws IOException {
        //first create the dependency graph file
        createMDG();
        //then use this file as an input to the bunch tool
        BunchAPI api = new BunchAPI();
        BunchProperties bp = new BunchProperties();
        bp.setProperty(BunchProperties.MDG_INPUT_FILE_NAME, mdgFile.getAbsolutePath());
        bp.setProperty(BunchProperties.OUTPUT_FORMAT, BunchProperties.TEXT_OUTPUT_FORMAT);

        api.setProperties(bp);
        api.run();
        //after running we have access to cluster graph using the Bunch API
        Hashtable results = api.getResults();
        String sMedLvl = (String) results.get(BunchAPI.MEDIAN_LEVEL_GRAPH);
        Integer iMedLvl = new Integer(sMedLvl);
        BunchGraph bg = api.getPartitionedGraph(iMedLvl.intValue());
        createClusters(bg);
    }

    /**
     * Helper method that creates the MDG file for the bunch tool.
     * This file contains one line per dependee/dependency relation.
     * These lines are formed by data extracted from the database. So to work
     * first the static analysis should be perfomed prior to the dependencies-based
     * cluster recommendation.
     */
    private void createMDG() throws IOException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        String q = "from ClassAnalysis ca where ca.project=:project";
        Query query = session.createQuery(q);
        query.setParameter("project", project);
        List<ClassAnalysis> projectClasses = query.list();
        //for each project class if it is not inner class get its dependencis 
        //with other classes and write a line in the MDG file

        if (!clustersExportDir.exists()) {
            clustersExportDir.mkdirs();
        } else {
            cleanClusterDir(clustersExportDir);
        }

        mdgFile = new File(clustersExportDir + File.separator
                + reuseProject.getProjectName() + ".mdg");
        PrintWriter pw = new PrintWriter(mdgFile);
        for (ClassAnalysis ca : projectClasses) {
            if (!ca.getInnerclass() == true) {
                Set<ClassAnalysis> dependencies =
                        ca.getClassesesForDependency();
                for (ClassAnalysis dep : dependencies) {
                    if (!dep.getInnerclass() == true) {
                        String mdgLine = ca.getName() + " " + dep.getName();
                        pw.println(mdgLine);
                    }
                }
            }
        }
        pw.flush();
        pw.close();
    }

    private void createClusters(BunchGraph bg) {

        clusters = new ArrayList<ClassCluster>();

        Iterator clusts = bg.getClusters().iterator();

        while (clusts.hasNext()) {

            BunchCluster bc = (BunchCluster) clusts.next();

            //create the cluster directory
            File clusterDir = new File(clustersExportDir
                    + File.separator + bc.getName());

            clusterDir.mkdir();

            //extract cluster participants from bunch
            ArrayList<ClassClusterPartcipant> clusterMembers = clusterNodesToClassParticipants(bc);

            //create the class cluster 
            ClassCluster cc = new DependenciesClassCluster(bc.getName(), clusterMembers, reuseProject);

            cc.createPhysicalCluster(clusterDir);

            //exports UMLGraph
            cc.createClustersUMLGraph(clusterDir);

            //adds cluster to collection
            clusters.add(cc);
        }

        //create the cluster directory
        File clusterDir = new File(clustersExportDir + File.separator + "GlueCluster");

        clusterDir.mkdir();

    }

    private ArrayList<ClassClusterPartcipant> clusterNodesToClassParticipants(BunchCluster bc) {

        //For each of the nodes of the cluster create copy the 
        //corresponding source file in the directory of the cluster
        Iterator members = bc.getClusterNodes().iterator();

        ArrayList<ClassAnalysis> cas = new ArrayList<ClassAnalysis>();
        ArrayList<ClassClusterPartcipant> caCollection = new ArrayList<ClassClusterPartcipant>();

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();

        while (members.hasNext()) {
            BunchNode bn = (BunchNode) members.next();
            String className = bn.getName();

            //Find the corresponding ClassAnalysis object
            String q = "from ClassAnalysis ca where ca.project=:project and ca.name=:name";
            Query query = session.createQuery(q);
            query.setParameter("project", project);
            query.setParameter("name", className);

            ClassAnalysis temp = (ClassAnalysis) query.uniqueResult();
            cas.add(temp);
        }
        for (int i = 0; i < cas.size(); i++) {
            double specificity = calculateClassesSpecificity(cas.get(i), cas);
            ClassClusterPartcipant tempCP = new ClassClusterPartcipant(cas.get(i), specificity,"none");
            caCollection.add(tempCP);
        }
        session.close();

        return caCollection;
    }
    
}
