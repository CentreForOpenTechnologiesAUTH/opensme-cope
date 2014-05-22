/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators.dependenciesgenerator;

import classycle.Analyser;
import classycle.ClassAttributes;
import classycle.NameAndSourceAttributes;
import classycle.PackageAttributes;
import classycle.PackageVertex;
import classycle.graph.AtomicVertex;
import classycle.graph.NameAttributes;
import classycle.graph.StrongComponent;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.PackageAnalysis;
import eu.opensme.cope.recommenders.entities.Project;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author econst
 */
public class ClassycleHandler {

    private Analyser analyser;
    private Project project;
    private HashMap<String,ArrayList<PackageAnalysis>> packageDependendees = new HashMap<String,ArrayList<PackageAnalysis>>();
    private HashMap<String,ArrayList<PackageAnalysis>> packageDependencies = new HashMap<String,ArrayList<PackageAnalysis>>();
    private HashMap<String,ArrayList<ClassAnalysis>> classDependendees = new HashMap<String,ArrayList<ClassAnalysis>>();
    private HashMap<String,ArrayList<ClassAnalysis>> classDependencies = new HashMap<String,ArrayList<ClassAnalysis>>();

    public ClassycleHandler(Analyser an, Project proj) {
        this.analyser = an;
        project = proj;
    }

    public ArrayList<ClassAnalysis> getClassAnalysisObjects() {
        ArrayList<ClassAnalysis> classes = new ArrayList<ClassAnalysis>();
        StrongComponent[] cycles = analyser.getCondensedClassGraph();
        AtomicVertex[] graph = analyser.getClassGraph();
        Map map = analyser.getClassLayerMap();
        List list = analyser.getTrueCycles(cycles);
        for (int i = 0; i < graph.length; i++) {
            AtomicVertex vertex = graph[i];
            Integer layerIndex = (Integer) map.get(vertex);
            ClassAnalysis temp = getClassAnalysis(vertex, analyser.getCycleFor(vertex, list), layerIndex == null ? -1 : layerIndex.intValue());
            classes.add(temp);
        }
        return classes;
    }
    
    public ArrayList<PackageAnalysis> getPackageAnalysisObjects(){
        ArrayList<PackageAnalysis> packages = new ArrayList<PackageAnalysis>();
        StrongComponent[] cycles = analyser.getCondensedPackageGraph();
        AtomicVertex[] graph = analyser.getPackageGraph();
        Map map = analyser.getPackageLayerMap();
        List list = analyser.getTrueCycles(cycles);
        for (int i=0;i<graph.length;i++){
            PackageVertex vertex = (PackageVertex) graph[i];
            Integer layerIndex = (Integer) map.get(vertex);
            PackageAnalysis temp = this.getPackageAnalysis(vertex, analyser.getCycleFor(vertex, list), layerIndex == null ? -1 : layerIndex.intValue());
            packages.add(temp);
        }
        return packages;
    }
    
    private PackageAnalysis getPackageAnalysis(PackageVertex vertex, StrongComponent cycle, int layerIndex){
        PackageAnalysis temp = new PackageAnalysis();
        PackageAttributes attributes = (PackageAttributes) vertex.getAttributes();
        temp.setProject(project);
        temp.setName(attributes.getName());
        temp.setSources(attributes.getSources());
        temp.setPackageSize(Integer.valueOf(String.valueOf(attributes.getSize())));
        temp.setUsedBy(Integer.valueOf(String.valueOf(vertex.getNumberOfIncomingArcs())));
        int usesInternal = 0;
        int usesExternal = 0;
        
                //Package Internal and external uses
        
        ArrayList<PackageAnalysis> packagesForDependee = new ArrayList<PackageAnalysis>();
        ArrayList<PackageAnalysis> packagesForDependency = new ArrayList<PackageAnalysis>();

        for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++) {
            if (((PackageVertex) vertex.getHeadVertex(i)).isGraphVertex()) {
                PackageAnalysis t1 = new PackageAnalysis();
                t1.setName(((PackageAttributes) vertex.getHeadVertex(i).getAttributes()).getName());
                packagesForDependency.add(t1);
                usesInternal++;
            } else {
                usesExternal++;
            }
        }
        packageDependencies.put(temp.getName(), packagesForDependency);

        temp.setUsesExternal(Integer.valueOf(String.valueOf(usesExternal)));
        temp.setUsesInternal(Integer.valueOf(String.valueOf(usesInternal)));
        
        temp.setLayer(Integer.valueOf(layerIndex));
        
        for (int i = 0, n = vertex.getNumberOfIncomingArcs(); i < n; i++) {
            PackageAnalysis t1 = new PackageAnalysis();
            t1.setName(((PackageAttributes) vertex.getTailVertex(i).getAttributes()).getName());
            packagesForDependee.add(t1);
        }
        //temp.setPackagesForDependee(packagesForDependee); 
        packageDependendees.put(temp.getName(), packagesForDependee);
        
        String[] classesStr = attributes.getClasses();
        Set<ClassAnalysis> classes = new HashSet(0);
        for (int i=0;i<classesStr.length;i++){
            ClassAnalysis t = new ClassAnalysis();
            t.setName(classesStr[i]);
            classes.add(t);
        }
        temp.setClasses(classes);
        return temp;
    }

    private ClassAnalysis getClassAnalysis(AtomicVertex vertex, StrongComponent cycle,
            int layerIndex) {
        ClassAnalysis temp = new ClassAnalysis();
        NameAndSourceAttributes attributes = (NameAndSourceAttributes) vertex.getAttributes();
        temp.setProject(project);
        //Class name
        temp.setName(attributes.getName());
        //Class size
        temp.setClassSize(Long.valueOf(String.valueOf(attributes.getSize())));
        //Sources
        temp.setSources(attributes.getSources());
        if (attributes instanceof ClassAttributes) {
            ClassAttributes ca = (ClassAttributes) attributes;
            //Class type
            temp.setType(ca.getType());
            boolean inner = ca.isInnerClass() ? true : false;
            //Class innerclass
            temp.setInnerclass(inner);
        } else {
            temp.setType(ClassAttributes.UNKNOWN);
            temp.setInnerclass(null);
        }
        //Class used by
        temp.setUsedby(Long.valueOf(String.valueOf(vertex.getNumberOfIncomingArcs())));

        int usesInternal = 0;
        int usesExternal = 0;

        //Class Internal and external uses
        ArrayList<ClassAnalysis> classesesForDependee = new ArrayList<ClassAnalysis>();
        //  Set<Path> paths = new HashSet(0);
        ArrayList<ClassAnalysis> classesesForDependency = new ArrayList<ClassAnalysis>();

        for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++) {
            if (((AtomicVertex) vertex.getHeadVertex(i)).isGraphVertex()) {
                ClassAnalysis t1 = new ClassAnalysis();
                t1.setName(((NameAttributes) vertex.getHeadVertex(i).getAttributes()).getName());
                classesesForDependency.add(t1);
                usesInternal++;
            } else {
                usesExternal++;
            }
        }
        //temp.setClassesesForDependency(classesesForDependency);
        this.classDependencies.put(temp.getName(), classesesForDependency);

        temp.setUsesexternal(Long.valueOf(String.valueOf(usesExternal)));
        temp.setUsesinternal(Long.valueOf(String.valueOf(usesInternal)));
        //Class layer
        temp.setLayer(Long.valueOf(String.valueOf(layerIndex)));

        for (int i = 0, n = vertex.getNumberOfIncomingArcs(); i < n; i++) {
            ClassAnalysis t1 = new ClassAnalysis();
            t1.setName(((NameAttributes) vertex.getTailVertex(i).getAttributes()).getName());
            classesesForDependee.add(t1);
        }
        //temp.setClassesesForDependee(classesesForDependee);
        this.classDependendees.put(temp.getName(), classesesForDependee);
        return temp;
    }
    
    public ArrayList<ClassAnalysis> getClassDependencies(String className){
        return this.classDependencies.get(className);
    }
    
    public ArrayList<ClassAnalysis> getClassDependendees(String className){
        return this.classDependendees.get(className);
    }
    
    public ArrayList<PackageAnalysis> getPackageDependencies(String packageName){
        return this.packageDependencies.get(packageName);
    }
    
    public ArrayList<PackageAnalysis> getPackageDependendees(String packageName){
        return this.packageDependendees.get(packageName);
    }
            
}
