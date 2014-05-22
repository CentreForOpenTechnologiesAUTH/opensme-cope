package eu.opensme.cope.analyzers.dependenciescoanalyzer.domain;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.ClassCluster;
import eu.opensme.cope.recommenders.entities.ClassClusterPartcipant;
import eu.opensme.cope.recommenders.entities.Frequencies;
import eu.opensme.cope.recommenders.entities.Log;
import eu.opensme.cope.recommenders.entities.Logentry;
import eu.opensme.cope.recommenders.entities.Path;
import eu.opensme.cope.recommenders.entities.Project;
import eu.opensme.cope.util.HibernateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author george
 */
public class ComputeDependenciesChangeFrequencies {

    private Project project;
    private static Set<ClassAnalysis> pClasses;
    private Log pLog;

    public ComputeDependenciesChangeFrequencies(Project project) {
        this.project = project;
        pClasses = this.project.getClasses();
        pLog = this.project.getLog();
    }

    public void computeChangeFrequencies() {
        for (ClassAnalysis ca : pClasses) {
            Set<ClassAnalysis> dependencies = ca.getClassesesForDependency();
            Set<Logentry> logentries = pLog.getLogentries();
            for (ClassAnalysis dependency : dependencies) {
                int noOfRevisions = 0;

                for (Logentry logentry : logentries) {
                    Set<Path> paths = logentry.getPaths();
                    Iterator<Path> piterator = paths.iterator();
                    int counter = 0;
                    while (counter < 2 && piterator.hasNext()) {
                        Path nextPath = piterator.next();
                        if (nextPath.getClassAnalysis() == ca || nextPath.getClassAnalysis() == dependency) {
                            counter++;
                            if (counter == 2) {
                                noOfRevisions++;
                            }
                        }
                    }
                }
//                String output = ca.getName() + "-->" + dependency.getName()
//                        + " changed together in no. of revisions: " + noOfRevisions;
//                System.out.println(output);


                Frequencies test = new Frequencies();
                test.setDependee(dependency.getClassid());
                test.setDependend(ca.getClassid());
                test.setFrequencies(noOfRevisions);
                test.setProjectId(ca.getProject().getProjectid());  //same for both ca and dependency
                test.setDependeePackageId(dependency.getPackageAnalysis().getPackageid());
                test.setDependendPackageId(ca.getPackageAnalysis().getPackageid());
                test.setDependeeLayer(dependency.getLayer());
                test.setDependendLayer(ca.getLayer());

                Session session = HibernateUtil.getSessionFactory().openSession();
                session.save(test);
                session.beginTransaction().commit();
                session.close();

            }
        }
    }

    public static void main(String[] args) {
        //example project names: ArgoUML-0.30.2, Ant 1.7.0 (Core)
        //example project ids: 1, 2

        if (args.length != 1) {

            args[0] = "Ant 1.7.0 (Core)";
            // System.out.println("You must provide the name of the project as argument");
            // System.exit(1);
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        //get the given project's id
        Query q = session.createQuery("from Project where projecttitle='" + args[0] + "'");
        Project project = (Project) q.uniqueResult();
        if (project == null) {
            System.out.println("Could not find project '" + args[0] + "' in the database.");
            System.exit(1);
        }
        ComputeDependenciesChangeFrequencies th = new ComputeDependenciesChangeFrequencies(project);
        th.computeChangeFrequencies();

        //clusterClasses("1");
    }

    public static List getFrequencies(String projectId) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        //get the given project's id
        Query q = s.createQuery("from Frequencies where projectId='" + projectId + "'");
        List<Frequencies> freqs = q.list();
        if (freqs == null) {
            //System.out.println("Could not find project '" + args[0] + "' in the database.");
            System.exit(1);
        }

        s.close();

        return freqs;
    }

    public static ClassAnalysis getClassAnalysis(Long classId) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        //get the given project's id
        Query q = s.createQuery("from ClassAnalysis where classid='" + classId + "'");
        ClassAnalysis ca = (ClassAnalysis) q.uniqueResult();
        if (ca == null) {
            //System.out.println("Could not find project '" + args[0] + "' in the database.");
            System.exit(1);
        }

        s.close();

        return ca;
    }

    public void printList(List<Frequencies> list) {
        for (Frequencies fr : list) {
            System.out.println("Dependend: " + fr.getDependend() + ", Dependee: " + fr.getDependee() + ", Frequency: " + fr.getFrequencies());
        }
    }

    public static void clusterClasses(String projectName) {

        //ids of marked classes
        ArrayList<Long> markedClassesIds = new ArrayList<Long>();

        //retrieves the frequencies for the desired project from db via hibernate
        List fr = getFrequencies(projectName);

        //System.out.println(fr.size());

        //sorts the frequencies list in  descending order (most frequent first)
        Comparator comparator = Collections.reverseOrder();
        Collections.sort(fr, comparator);

        //array list to hold the class clusters
        ArrayList<ClassCluster> clusters = new ArrayList<ClassCluster>();

        int clusterCounter = 0;

        Iterator<Frequencies> frIterator = fr.iterator();
        //for every frequency
        while (frIterator.hasNext()) {

            Frequencies nextFreq = frIterator.next();

            //if clusters array list is empty, add both participants (dependend and dependee)
            if (clusters.isEmpty()) {

                ArrayList<ClassAnalysis> classes = new ArrayList<ClassAnalysis>();

                ClassAnalysis dependend = getClassAnalysis(nextFreq.getDependend());
                markedClassesIds.add(dependend.getClassid());
                ClassAnalysis dependee = getClassAnalysis(nextFreq.getDependee());
                markedClassesIds.add(dependee.getClassid());

                ClassClusterPartcipant sDependend = new ClassClusterPartcipant(dependend, nextFreq.getFrequencies(),"none");
                ClassClusterPartcipant sDependee = new ClassClusterPartcipant(dependee, nextFreq.getFrequencies(),"none");

                ArrayList<ClassClusterPartcipant> samples = new ArrayList<ClassClusterPartcipant>();
                samples.add(sDependee);
                samples.add(sDependend);

                String name = "Cluster" + (clusterCounter++);
                //TODO: The following class was deactivated due to conflicts that happened to classCluster class. If the class is 
                //going to be used again it might not working properly with this class marked out.
                //clusters.add(new ClassCluster(name, samples));


//                Iterator<ClassAnalysis> nextClass = pClasses.iterator();
//
//                //find dependend and dependee and characterize them as marked
//                while (nextClass.hasNext()) {
//                    ClassAnalysis candidateClass = nextClass.next();
//                    if ((candidateClass.getClassid() == nextFreq.getDependee()) || (candidateClass.getClassid() == nextFreq.getDependend())) {
//                        classes.add(candidateClass);
//                        candidateClass.setMarked(true);
//                        candidateClass.setSpecificity(nextFreq.getFrequencies());
//                    }
//                }

//                if (!classes.isEmpty()) {
//                    //create new cluster and add it to clusters ArrayList
//                    String name = "Cluster" + (clusterCounter++);
//                    clusters.add(new ClassCluster(name, classes));
//                }
            } //else if clusters is not empty
            else {

//               ClassAnalysis dependend = new ClassAnalysis();
//               ClassAnalysis dependee = new ClassAnalysis();
//
//                Iterator<ClassAnalysis> caIterator = pClasses.iterator();

                //find dependend and dependee
//                while (caIterator.hasNext()) {
//
//                    ClassAnalysis victim = caIterator.next();
//
//                    if (victim.getClassid() == nextFreq.getDependee()) {
//                        if (victim.getMarked()) {
//                            dependee = victim;
//                        }
//
//                    } else if (victim.getClassid() == nextFreq.getDependend()) {
//                        if (victim.getMarked()) {
//                            dependend = victim;
//                        }
//                    }
//                }

                ClassAnalysis dependend = getClassAnalysis(nextFreq.getDependend());
                ClassAnalysis dependee = getClassAnalysis(nextFreq.getDependee());


                //if both are unmarked
                if ((!markedClassesIds.contains(dependend.getClassid())) && (!markedClassesIds.contains(dependee.getClassid()))) {
                    //if (!dependend.getMarked() && !dependee.getMarked()) {

                    ArrayList<ClassAnalysis> classes = new ArrayList<ClassAnalysis>();

                    markedClassesIds.add(dependend.getClassid());
                    markedClassesIds.add(dependee.getClassid());

//                    dependend.setSpecificity(nextFreq.getFrequencies());
//                    dependee.setSpecificity(nextFreq.getFrequencies());

                    ClassClusterPartcipant sDependend = new ClassClusterPartcipant(dependend, nextFreq.getFrequencies(),"none");
                    ClassClusterPartcipant sDependee = new ClassClusterPartcipant(dependee, nextFreq.getFrequencies(),"none");

                    ArrayList<ClassClusterPartcipant> samples = new ArrayList<ClassClusterPartcipant>();
                    samples.add(sDependee);
                    samples.add(sDependend);

//                    if (!classes.isEmpty()) {
                    //create new cluster and add it to clusters ArrayList
                    String name = "Cluster" + (clusterCounter++);
                    //TODO: The following class was deactivated due to conflicts that happened to classCluster class. If the class is 
                    //going to be used again it might not working properly with this class marked out.
                    //clusters.add(new ClassCluster(name, samples));
//                    }


                } //else if dependend is only marked

                if ((markedClassesIds.contains(dependend.getClassid())) && (!markedClassesIds.contains(dependee.getClassid()))) {
                    // else if (dependend.getMarked() && !dependee.getMarked()) {
                    //search cluster where dependend belongs and add dependee;
                    Iterator<ClassCluster> ccIterator = clusters.iterator();

                    while (ccIterator.hasNext()) {

                        ClassCluster temp = ccIterator.next();



                        if (temp.findClassAnalysisObject(dependend)) {
                            markedClassesIds.add(dependee.getClassid());
                            //dependee.setMarked(true);
                            //dependee.setSpecificity(nextFreq.getFrequencies());

                            temp.getClusterParticipants().add(new ClassClusterPartcipant(dependee, nextFreq.getFrequencies(),"none"));
                            //temp.getClusterParticipants().add(dependee);;
                        }
                    }
                } //else if dependee is only marked
                if ((!markedClassesIds.contains(dependend.getClassid())) && (markedClassesIds.contains(dependee.getClassid()))) {

                    // else if (!dependend.getMarked() && dependee.getMarked()) {
                    //search cluster where dependee belongs and add dependend;
                    Iterator<ClassCluster> ccIterator = clusters.iterator();

                    while (ccIterator.hasNext()) {

                        ClassCluster temp = ccIterator.next();

                        if (temp.findClassAnalysisObject(dependee)) {
                            markedClassesIds.add(dependend.getClassid());
                            //dependend.setMarked(true);
                            //dependend.setSpecificity(nextFreq.getFrequencies());
                            temp.getClusterParticipants().add(new ClassClusterPartcipant(dependend, nextFreq.getFrequencies(),"none"));
                            //temp.getClusterParticipants().add(dependend);
                        }
                    }
                }
            }
        }

        System.out.println(clusters.size());

        for (int j = 0; j < clusters.size(); j++) {
            //System.out.println("ping");
            clusters.get(j).exportCSV();
        }

    }
}
