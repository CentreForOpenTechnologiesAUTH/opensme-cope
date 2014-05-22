/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.domain.GeneratedComponent;
import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.DependenciesGenerator;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.util.HibernateUtil;
import eu.opensme.cope.util.WannabeComponent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author krap
 */
public class ComponentSimilarityFinder {
    
    private ReuseProject reuseProject;
    private GeneratedComponent pivotComponent;
    private int minLayer;
    private int maxLayer;
    private List<WannabeComponent> potentialSuperComponents;
    private List<WannabeComponent> potentialSubComponents;
    
    
    public ComponentSimilarityFinder(ReuseProject reuseProject, int minLayer, int maxLayer, List<WannabeComponent> potentialSuperComponents, List<WannabeComponent> potentialSubComponents)
    {
        this.reuseProject = reuseProject;
        this.minLayer = minLayer;
        this.maxLayer = maxLayer;
        this.potentialSuperComponents = potentialSuperComponents;
        this.potentialSubComponents = potentialSubComponents;
    }
    
    public void setMaxLayer(int maxLayer){
        this.maxLayer = maxLayer;
    }
    
    
    public void setMinLayer(int minLayer){
        this.minLayer = minLayer;
    }
    
    public Object[] findSuperAndSubComponents() {
        
        DefaultListModel superModelList = new DefaultListModel();
        DefaultListModel subModelList = new DefaultListModel();

        //Create dependency generator instance in order to retrieve the cluster member classes
        DependenciesGenerator myDG = null;
        try {
            myDG = new DependenciesGenerator(reuseProject);

        } catch (Exception ex) {
            Logger.getLogger(ComponentSuggestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        ClassAnalysis centerClass = classNameToClassAnalysis(pivotComponent.getComponentCentreClass());

        //superComponentsList related variables
        int list2Index = 0;
        //superComponentsList.setModel(superModelList);

        //subComponentsList related variables
        int list3Index = 0;
        //subComponentsList.setModel(subModelList);

        List<ClassAnalysis> candidateClasses = new ArrayList<ClassAnalysis>();
        for (int i = minLayer; i <= maxLayer; i++) {
            if (classesFromLayer(centerClass.getLayer() + i) != null) {
                candidateClasses.addAll(classesFromLayer(centerClass.getLayer() + i));
            }
        }

        //Find similar components

        //Get all members (ClassIDs) of the cluster for the selected component
        Set<Long> selectedDependenciesSet = myDG.getClusterIDs(centerClass.getClassid(), reuseProject.getProject().getProjectid());

        Iterator<ClassAnalysis> itClasses2 = candidateClasses.iterator();
        while (itClasses2.hasNext()) {
            ClassAnalysis temp = itClasses2.next();
            //Get all members (ClassIDs) of the cluster for the wannabe component
            Set<Long> wannabeDependenciesSet = myDG.getClusterIDs(temp.getClassid(), reuseProject.getProject().getProjectid());
            //Find relationship
            int relationship = isSubSet(selectedDependenciesSet, wannabeDependenciesSet);
            //If super component
            if (relationship == 1) {
                WannabeComponent wannabeTemp = new WannabeComponent("super", massClassIDToClassAnalysis(wannabeDependenciesSet), temp);
                //add it to the super wannabe components 
                potentialSuperComponents.add(wannabeTemp);
                superModelList.add(list2Index, temp.getName());
                list2Index++;
            } else if (relationship == 0) {
                WannabeComponent wannabeTemp = new WannabeComponent("sub", massClassIDToClassAnalysis(wannabeDependenciesSet), temp);
                potentialSubComponents.add(wannabeTemp);
                subModelList.add(list3Index, temp.getName());
                list3Index++;
            }
        }
        
        DefaultListModel [] listModelArray = new DefaultListModel[2];
        listModelArray[0] = superModelList;
        listModelArray[1] = subModelList;
        
        return listModelArray;

        //System.out.println("finished similarities discovery");
    }   
    
    public ClassAnalysis classNameToClassAnalysis(String className) {
        ClassAnalysis trove = null;

        try {
            Long projectId = reuseProject.getProject().getProjectid();
            String query = "select from ClassAnalysis where name='" + className + "' and projectid='"+projectId+"'";
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            //get the given project's id
            Query q = session.createQuery(query);
            trove = (ClassAnalysis) q.uniqueResult();
            session.close();

        } catch (HibernateException he) {
            System.out.println(he.getStackTrace());
        }

        return trove;
    }
    
    private List<ClassAnalysis> classesFromLayer(Long layer) {
        List<ClassAnalysis> results = new ArrayList<ClassAnalysis>();
        List<ClassAnalysis> tempResults = new ArrayList<ClassAnalysis>();

        try {
            String query = "select from ClassAnalysis where layer='" + layer + "' AND projectid='" + reuseProject.getProject().getProjectid() + "'";
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            //get the given project's id
            Query q = session.createQuery(query);
            tempResults = q.list();
            session.close();

        } catch (HibernateException he) {
            System.out.println(he.getStackTrace());
        }

        Iterator<ClassAnalysis> itClasses = tempResults.iterator();
        while (itClasses.hasNext()) {
            ClassAnalysis tempClass = itClasses.next();
            if (!tempClass.getName().contains("$")) {
                results.add(tempClass);
            }
        }

        return results;
    }
    
    /**
     * <p>Checks if a component is a part of another component. More
     * specifically it uses the java files' list of two components and if the
     * files of one component are all included in the other's java files' list
     * then one component is part of the other.</p>
     *
     * @param l1 the first set (Component A's Java Files' List)
     * @param l2 the second set (Component B's Java Files' List)
     *
     * @return 0 - If component A is part of component B <br/>1 - If component B
     * is part of component A <br/>2 - If components A and B are unrelated
     */
    private int isSubSet(Set<Long> l1, Set<Long> l2) {

        if (l1.containsAll(l2) && l1.size() != l2.size()) {
            return 0;
        } else if (l2.containsAll(l1) && l1.size() != l2.size()) {
            return 1;
        } else {
            return 2;
        }
    }
    
    private List<ClassAnalysis> massClassIDToClassAnalysis(Set<Long> classIDs) {
        List<ClassAnalysis> classes = new ArrayList<ClassAnalysis>();

        Iterator<Long> itClassIDs = classIDs.iterator();
        while (itClassIDs.hasNext()) {
            classes.add(classIDToClassAnalysis(itClassIDs.next()));
        }
        return classes;
    }
    
    private ClassAnalysis classIDToClassAnalysis(Long classID) {
        ClassAnalysis trove = null;

        try {
            Long projectId = reuseProject.getProject().getProjectid();
            String query = "select from ClassAnalysis where classid='" + classID + "' and projectid='"+projectId+"'";
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            //get the given project's id
            Query q = session.createQuery(query);
            trove = (ClassAnalysis) q.uniqueResult();
            session.close();

        } catch (HibernateException he) {
            System.out.println(he.getStackTrace());
        }

        return trove;
    }
    
    /**
     * (currently unused)
     * 
     * <p>The Jaccard index, also known as the Jaccard similarity coefficient
     * (originally coined coefficient de communauté by Paul Jaccard), is a
     * statistic used for comparing the similarity and diversity of sample sets.
     * </p> <p> Jaccard coefficient measures similarity between sample sets, and
     * is defined as the size of the intersection divided by the size of the
     * union of the sample sets. </p> <p> For our purposes the aforementioned
     * sets are identical to the set of java files each of a {@link ClassCentricComponent}
     * object contains. </p> <p> J(A,B) = Intersection(A,B) / Union(A,B) </p>
     *
     * @param l1 the first set (Component A's Java Files' List)
     * @param l2 the second set (Component B's Java Files' List)
     *
     * @return double
     */
    public double calculateJaccardIndex(List<Long> l1, List<Long> l2) {

        double jaccardIndex = 0.0;

        Set<Long> union = new HashSet<Long>(l1);
        union.addAll(l2);

        Set<Long> intersection = new HashSet<Long>(l1);
        intersection.retainAll(l2);

        jaccardIndex = (double) intersection.size() / union.size();
        return jaccardIndex;
    }
    
    public List<ClassAnalysis> findSelectedComponentClasses(String centerClassName) {
        DependenciesGenerator myDG = null;
        ClassAnalysis centerClass = null;

        centerClass = classNameToClassAnalysis(centerClassName);

        try {
            myDG = new DependenciesGenerator(reuseProject);

        } catch (Exception ex) {
            Logger.getLogger(ComponentSuggestionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        Set<Long> selectedComponentClassesIDs = myDG.getClusterIDs(centerClass.getClassid(), reuseProject.getProject().getProjectid());

        return massClassIDToClassAnalysis(selectedComponentClassesIDs);
    }
    
    public List<Long> findDifferentLayers() {
        List<Long> results = new ArrayList<Long>();

        try {
            String query = "SELECT DISTINCT ca.layer FROM ClassAnalysis as ca WHERE projectid='" + reuseProject.getProject().getProjectid() + "'";
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            //get the given project's id
            Query q = session.createQuery(query);
            results = q.list();
            session.close();

        } catch (HibernateException he) {
            System.out.println(he.getStackTrace());
        }
        Collections.sort(results);
        return results;
    }
    
    public List<ClassAnalysis> findDependencies(ClassAnalysis ca) {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();
        Long projectId = reuseProject.getProject().getProjectid();
        Query q = session.createQuery(
                "select elements(ca.classesesForDependency) from ClassAnalysis ca where ca = :ca and projectid=:projectid");
        q.setEntity("ca", ca);
        q.setEntity("projectid", projectId);
        List<ClassAnalysis> neighbors = q.list();
        session.close();

        return neighbors;
    }

    public GeneratedComponent getPivotComponent() {
        return pivotComponent;
    }

    public void setPivotComponent(GeneratedComponent pivotComponent) {
        this.pivotComponent = pivotComponent;
    }
}
