/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers.dependencyTypeAnalyzer;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.analyzers.dependencyTypeAnalyzer.SubtypeDependencyEnum;
import eu.opensme.cope.analyzers.dependencyTypeAnalyzer.TypeDependencyEnum;
import eu.opensme.cope.componentmakers.visitors.binary.AllDependencyVisitor;
import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.DependenciesType;
import eu.opensme.cope.recommenders.entities.DependenciesTypeId;
import eu.opensme.cope.util.HibernateUtil;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 *
 * @author sskalist
 */
public class DependencyTypeGenerator {

    private HashMap<String, HashSet<String>> extendsClassOfClass;
    private HashMap<String, HashSet<String>> implementsClassOfClass;
    private HashMap<String, HashSet<String>> fieldDeclarationOfClass;
    private HashMap<String, HashSet<String>> localVariablesOfClass;
    private HashMap<String, HashSet<String>> newInstancesOfClass;
    private HashMap<String, HashSet<String>> parametersOfClass;
    private HashMap<String, HashSet<String>> returnTypesOfClass;
    private HashMap<String, HashMap<String, SubtypeDependencyEnum>> fieldsAccessedOfClassByClass;
    private HashMap<String, HashMap<String, Set<MethodSignature>>> calledMethodsOfClassByClass;
    private ReuseProject reuseProject;
    private HashMap< Long, ClassAnalysis> idToClass;
    private HashMap<String, Long> nameToID;

    public DependencyTypeGenerator(ReuseProject reuseProject) {
        this.reuseProject = reuseProject;

        extendsClassOfClass = new HashMap<String, HashSet<String>>();
        implementsClassOfClass = new HashMap<String, HashSet<String>>();
        fieldDeclarationOfClass = new HashMap<String, HashSet<String>>();
        localVariablesOfClass = new HashMap<String, HashSet<String>>();
        newInstancesOfClass = new HashMap<String, HashSet<String>>();
        returnTypesOfClass = new HashMap<String, HashSet<String>>();
        parametersOfClass = new HashMap<String, HashSet<String>>();
        fieldsAccessedOfClassByClass = new HashMap<String, HashMap<String, SubtypeDependencyEnum>>();
        calledMethodsOfClassByClass = new HashMap<String, HashMap<String, Set<MethodSignature>>>();

        this.idToClass = new HashMap< Long, ClassAnalysis>();
        this.nameToID = new HashMap<String, Long>();
        initialize();
    }

    private void initialize() {
        Set<ClassAnalysis> classes = reuseProject.getProject().getClasses();

        for (ClassAnalysis clazz : classes) {
            idToClass.put(clazz.getClassid(), clazz);
            nameToID.put(clazz.getName(), clazz.getClassid());
        }
        String projectLocation = reuseProject.getProjectLocation().endsWith(File.separator) ? reuseProject.getProjectLocation() : reuseProject.getProjectLocation() + File.separator;
        populateJarClasses(projectLocation + "bin" + File.separator + reuseProject.getProjectJARFilename());
    }

    public void findDependencies() {
        Set<ClassAnalysis> classes = reuseProject.getProject().getClasses();
        for (ClassAnalysis clazz : classes) {
            findDependencies(clazz.getName());
        }
    }

    private void findDependencies(String qualifiedName) {
        JavaClass javaClass = null;
        try {
            javaClass = Repository.lookupClass(qualifiedName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DependencyTypeGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (javaClass == null) {
            return;
        }
        AllDependencyVisitor visitor = new AllDependencyVisitor();
        visitor.visit(javaClass, nameToID.keySet());

        if (!extendsClassOfClass.containsKey(qualifiedName)) {
            extendsClassOfClass.put(qualifiedName, new HashSet<String>());
        }
        extendsClassOfClass.get(qualifiedName).addAll(visitor.getExtendsClass());

        if (!implementsClassOfClass.containsKey(qualifiedName)) {
            implementsClassOfClass.put(qualifiedName, new HashSet<String>());
        }
        implementsClassOfClass.get(qualifiedName).addAll(visitor.getImplementsClass());

        if (!fieldDeclarationOfClass.containsKey(qualifiedName)) {
            fieldDeclarationOfClass.put(qualifiedName, new HashSet<String>());
        }
        fieldDeclarationOfClass.get(qualifiedName).addAll(visitor.getFieldDeclaration());

        // New Objects
        if (!newInstancesOfClass.containsKey(qualifiedName)) {
            newInstancesOfClass.put(qualifiedName, new HashSet<String>());
        }
        newInstancesOfClass.get(qualifiedName).addAll(visitor.getNewInstances());

        if (!localVariablesOfClass.containsKey(qualifiedName)) {
            localVariablesOfClass.put(qualifiedName, new HashSet<String>());
        }
        localVariablesOfClass.get(qualifiedName).addAll(visitor.getLocalVariables());

        if (!returnTypesOfClass.containsKey(qualifiedName)) {
            returnTypesOfClass.put(qualifiedName, new HashSet<String>());
        }
        returnTypesOfClass.get(qualifiedName).addAll(visitor.getReturnTypes());

        if (!parametersOfClass.containsKey(qualifiedName)) {
            parametersOfClass.put(qualifiedName, new HashSet<String>());
        }
        parametersOfClass.get(qualifiedName).addAll(visitor.getParameters());

        // Method calls
        if (!calledMethodsOfClassByClass.containsKey(qualifiedName)) {
            calledMethodsOfClassByClass.put(qualifiedName, new HashMap<String, Set<MethodSignature>>());
        }
        Map<String, Set<MethodSignature>> calledMethodsOfProjectClass = visitor.getCalledMethodsOfProjectClass();
        calledMethodsOfClassByClass.get(qualifiedName).putAll(calledMethodsOfProjectClass);;


        if (!fieldsAccessedOfClassByClass.containsKey(qualifiedName)) {
            fieldsAccessedOfClassByClass.put(qualifiedName, new HashMap<String, SubtypeDependencyEnum>());
        }
        Map<String, SubtypeDependencyEnum> fieldsAccessedOfClass = visitor.getFieldsAccessedOfProjectClass();
        Map<String, SubtypeDependencyEnum> get = fieldsAccessedOfClassByClass.get(qualifiedName);

        for (String className : fieldsAccessedOfClass.keySet()) {
            if (!get.containsKey(className)) {
                get.put(className, fieldsAccessedOfClass.get(className));
            } else {
                if (!get.get(className).equals(fieldsAccessedOfClass.get(className))) {
                    get.put(className, SubtypeDependencyEnum.BOTH);
                }
            }
        }
    }

    public void storeData() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction();

        for (String className : this.nameToID.keySet()) {
            if (extendsClassOfClass.containsKey(className)) {
                insertDependencies(className, extendsClassOfClass.get(className), TypeDependencyEnum.Inheritance, SubtypeDependencyEnum.Extends, session);
            }

            if (implementsClassOfClass.containsKey(className)) {
                insertDependencies(className, implementsClassOfClass.get(className), TypeDependencyEnum.Inheritance, SubtypeDependencyEnum.Implements, session);
            }

            if (fieldDeclarationOfClass.containsKey(className)) {
                insertDependencies(className, fieldDeclarationOfClass.get(className), TypeDependencyEnum.TypedDeclaration, SubtypeDependencyEnum.Field, session);
            }

            if (localVariablesOfClass.containsKey(className)) {
                insertDependencies(className, localVariablesOfClass.get(className), TypeDependencyEnum.TypedDeclaration, SubtypeDependencyEnum.LocalVariable, session);
            }

            if (returnTypesOfClass.containsKey(className)) {
                insertDependencies(className, returnTypesOfClass.get(className), TypeDependencyEnum.TypedDeclaration, SubtypeDependencyEnum.ReturnType, session);
            }

            if (parametersOfClass.containsKey(className)) {
                insertDependencies(className, parametersOfClass.get(className), TypeDependencyEnum.TypedDeclaration, SubtypeDependencyEnum.Parameter, session);
            }

            // New Objects
            if (newInstancesOfClass.containsKey(className)) {
                insertDependencies(className, newInstancesOfClass.get(className), TypeDependencyEnum.Instantiation, SubtypeDependencyEnum.Instantiation, session);

            }

            if (calledMethodsOfClassByClass.containsKey(className)) {
                insertDependencies(className, calledMethodsOfClassByClass.get(className), TypeDependencyEnum.MethodCall, session);
            }
            
            if (fieldsAccessedOfClassByClass.containsKey(className)) {
                insertFieldDependencies(className, fieldsAccessedOfClassByClass.get(className), TypeDependencyEnum.FieldAccess, session);
            }
        }

        tx.commit();

        session.close();
    }

    private void insertDependencies(String className, HashSet<String> dependencyClasses, TypeDependencyEnum type, SubtypeDependencyEnum subtype, Session session) {
        if (!this.nameToID.containsKey(className)) {
            return;
        }
        long dependeeClassId = this.nameToID.get(className);
        if (!this.idToClass.containsKey(dependeeClassId)) {
            return;
        }
        ClassAnalysis dependeeClass = this.idToClass.get(dependeeClassId);
        for (String dependencyClassName : dependencyClasses) {
            if (!this.nameToID.containsKey(dependencyClassName)) {
                continue;
            }
            long dependencyClassId = this.nameToID.get(dependencyClassName);
            if (!this.idToClass.containsKey(dependencyClassId)) {
                continue;
            }
            ClassAnalysis dependencyClass = this.idToClass.get(dependencyClassId);

            DependenciesTypeId dependenciesTypeId = new DependenciesTypeId(dependeeClassId, dependencyClassId, type.name(), subtype.name());
            DependenciesType dependenciesType = new DependenciesType(dependenciesTypeId, dependeeClass, dependencyClass, 1);

            session.save(dependenciesType);
        }
    }

    private void insertDependencies(String className, HashMap<String, Set<MethodSignature>> dependencyClasses, TypeDependencyEnum type, Session session) {
        if (!this.nameToID.containsKey(className)) {
            return;
        }
        long dependeeClassId = this.nameToID.get(className);
        if (!this.idToClass.containsKey(dependeeClassId)) {
            return;
        }
        ClassAnalysis dependeeClass = this.idToClass.get(dependeeClassId);
        for (String dependencyClassName : dependencyClasses.keySet()) {
            if (!this.nameToID.containsKey(dependencyClassName)) {
                continue;
            }
            long dependencyClassId = this.nameToID.get(dependencyClassName);
            if (!this.idToClass.containsKey(dependencyClassId)) {
                continue;
            }
            if(dependencyClasses.get(dependencyClassName).isEmpty())
                continue;
            SubtypeDependencyEnum subtype = getType(dependencyClasses.get(dependencyClassName));
            ClassAnalysis dependencyClass = this.idToClass.get(dependencyClassId);

            DependenciesTypeId dependenciesTypeId = new DependenciesTypeId(dependeeClassId, dependencyClassId, type.name(), subtype.name());
            DependenciesType dependenciesType = new DependenciesType(dependenciesTypeId, dependeeClass, dependencyClass, dependencyClasses.get(dependencyClassName).size());

            session.save(dependenciesType);
        }
    }
    
     private void insertFieldDependencies(String className, HashMap<String, SubtypeDependencyEnum> dependencyClasses, TypeDependencyEnum type, Session session) {
        if (!this.nameToID.containsKey(className)) {
            return;
        }
        long dependeeClassId = this.nameToID.get(className);
        if (!this.idToClass.containsKey(dependeeClassId)) {
            return;
        }
        ClassAnalysis dependeeClass = this.idToClass.get(dependeeClassId);
        for (String dependencyClassName : dependencyClasses.keySet()) {
            if (!this.nameToID.containsKey(dependencyClassName)) {
                continue;
            }
            long dependencyClassId = this.nameToID.get(dependencyClassName);
            if (!this.idToClass.containsKey(dependencyClassId)) {
                continue;
            }

            SubtypeDependencyEnum subtype = dependencyClasses.get(dependencyClassName);
            ClassAnalysis dependencyClass = this.idToClass.get(dependencyClassId);

            DependenciesTypeId dependenciesTypeId = new DependenciesTypeId(dependeeClassId, dependencyClassId, type.name(), subtype.name());
            DependenciesType dependenciesType = new DependenciesType(dependenciesTypeId, dependeeClass, dependencyClass, 1);

            session.save(dependenciesType);
        }
    }

    private SubtypeDependencyEnum getType(Set<MethodSignature> methods) {
        SubtypeDependencyEnum subtype = null;
        for (MethodSignature method : methods) {
            if (subtype == null) {
                if (method.isStatic()) {
                    subtype = SubtypeDependencyEnum.Static;
                } else {
                    subtype = SubtypeDependencyEnum.NonStatic;
                }
            }
            if ((subtype.equals(SubtypeDependencyEnum.NonStatic) && method.isStatic()) || (subtype.equals(SubtypeDependencyEnum.Static) && !method.isStatic())) {
                subtype = SubtypeDependencyEnum.BOTH;
                break;
            }
        }
        return subtype;
    }

    private static void populateJarClasses(String jarPath) {
        JarFile jar = null;
        try {
            jar = new JarFile(jarPath);
        } catch (IOException ex) {
            Logger.getLogger(DependencyTypeGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        Enumeration<JarEntry> jarEntries = jar.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry entry = jarEntries.nextElement();
            if (entry.toString().endsWith(".class")) {

                try {
                    JavaClass javaClass = new ClassParser(jarPath, entry.toString()).parse();
                    Repository.addClass(javaClass);
                } catch (IOException ex) {
                    Logger.getLogger(DependencyTypeGenerator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassFormatException ex) {
                    Logger.getLogger(DependencyTypeGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
