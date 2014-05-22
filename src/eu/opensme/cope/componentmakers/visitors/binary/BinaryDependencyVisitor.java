/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.binary;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.Type;

/**
 *
 * @author sskalist
 */
public class BinaryDependencyVisitor extends EmptyVisitor {

    private JavaClass javaClass;
    private Set<String> externalClassesWithinProject;
    private HashSet<String> projectInterfaces;
    private HashSet<String> superClasses;
    private static MethodVisitor methodVisitor = new MethodVisitor();
    private HashSet<String> externalClassesUsed;
    private HashMap<String, Set<MethodSignature>> calledMethodsOfProjectClass;

    public BinaryDependencyVisitor() {
        this.projectInterfaces = new HashSet<String>();
        this.superClasses = new HashSet<String>();
        this.externalClassesUsed = new HashSet<String>();
        this.calledMethodsOfProjectClass = new HashMap<String, Set<MethodSignature>>();
    }

    public void visit(JavaClass javaClass, Set<String> externalClassesWithinProject) {
        this.projectInterfaces.clear();
        this.superClasses.clear();
        this.externalClassesUsed.clear();
        this.calledMethodsOfProjectClass.clear();

        this.javaClass = javaClass;
        this.externalClassesWithinProject = externalClassesWithinProject;
        visitJavaClass(javaClass);
//        fixMethodCalls();
        this.externalClassesWithinProject = null;
        this.javaClass = null;
    }

    @Override
    public void visitJavaClass(JavaClass javaClass) {
        if (!checkFields()) {
            return;
        }
        super.visitJavaClass(javaClass);

        populateInterfacesDependencies();
        populateSuperTypeDependencies();
        populateInnerClassDependencies();
        populateFieldDependencies();
        populateMethodsDependencies();
        populate();
    }

    private void populateInterfacesDependencies() {
        populateInterfacesDependencies(this.javaClass);
    }

    private void populateInterfacesDependencies(JavaClass javaClass) {
        String[] interfaceNames = javaClass.getInterfaceNames();

        for (int i = 0; i < interfaceNames.length; i++) {
            if (externalClassesWithinProject.contains(interfaceNames[i])) {
                try {
                    JavaClass interfaceClass = Repository.lookupClass(interfaceNames[i]);
                    this.projectInterfaces.add(interfaceNames[i]); // Add it after finding its binary file
                    for (Method method : interfaceClass.getMethods()) {
                        if (method.isAbstract()) {
                            if (!this.calledMethodsOfProjectClass.containsKey(interfaceClass.getClassName())) {
                                this.calledMethodsOfProjectClass.put(interfaceClass.getClassName(), new HashSet<MethodSignature>());
                            }
                            Type[] parameterTypes = method.getArgumentTypes();
                            String[] parameterTypeNames = new String[parameterTypes.length];
                            for (int j = 0; j < parameterTypeNames.length; j++) {
                                parameterTypeNames[j] = className(parameterTypes[j]);
                            }
                            this.calledMethodsOfProjectClass.get(interfaceClass.getClassName()).add(new MethodSignature(method.getName(), parameterTypeNames, className(method.getReturnType())));
                        }
                    }
                    populateInterfacesDependencies(interfaceClass);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(BinaryDependencyVisitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void populateSuperTypeDependencies() {

        String superClassName = this.javaClass.getSuperclassName();
        JavaClass currentClass = this.javaClass;
        while (externalClassesWithinProject.contains(superClassName) && !superClassName.equals("java.lang.Object")) { // TODO: Add check class within project
            try {
                JavaClass superClass = currentClass.getSuperClass();
//                if (externalClassesWithinProject.contains(superClassName)) {
                    populateInterfacesDependencies(superClass);
                    this.superClasses.add(superClassName); // Add it after finding its binary file
//                }

                currentClass = superClass;
                superClassName = currentClass.getSuperclassName();
            } catch (ClassNotFoundException ex) {
                if (externalClassesWithinProject.contains(superClassName)) {
                    throw new UnsupportedOperationException("Could Not find binary file for " + superClassName);
                } else {
                    break;
                }
            }
        }
    }

    private void populateFieldDependencies() {
        Field[] fields = this.javaClass.getFields();
        for (Field field : fields) {
            field.accept(this);
            //visitField(field);
        }
    }

    private void populateInnerClassDependencies() {
        for (Attribute attribute : javaClass.getAttributes()) {
            if (attribute instanceof InnerClasses) {
                attribute.accept(this);
                //visitInnerClasses((InnerClasses)attribute);
            }
        }
    }

    private void populateMethodsDependencies() {
        for (Method method : this.javaClass.getMethods()) {
            method.accept(this);
        }
    }

    private void populate() {
        for (Attribute a : this.javaClass.getAttributes()) {
            a.accept(this);
        }
    }

    @Override
    public void visitMethod(Method method) {
        if (!checkFields()) {
            return;
        }
        super.visitMethod(method);
        methodVisitor.visit(javaClass, method, externalClassesWithinProject);

        this.externalClassesUsed.addAll(methodVisitor.getExternalClassesUsed());

        Map<String, Set<MethodSignature>> calledMethods = methodVisitor.getCalledMethodsOfProjectClass();
        for (String calledMethod : calledMethods.keySet()) {
            if (!this.calledMethodsOfProjectClass.containsKey(calledMethod)) {
                this.calledMethodsOfProjectClass.put(calledMethod, calledMethods.get(calledMethod));
            } else {
                this.calledMethodsOfProjectClass.get(calledMethod).addAll(calledMethods.get(calledMethod));
            }
        }
    }

    @Override
    public void visitField(Field field) {
        if (!checkFields()) {
            return;
        }
        super.visitField(field);
        String qualifiedName = className(field.getType());
        if (this.externalClassesWithinProject.contains(qualifiedName)) {
            this.externalClassesUsed.add(qualifiedName);
        }
    }

    @Override
    public void visitInnerClasses(InnerClasses innerClasses) {
        if (!checkFields()) {
            return;
        }
        super.visitInnerClasses(innerClasses);
        InnerClass[] innerClassesArray = innerClasses.getInnerClasses();
        for (InnerClass innerClass : innerClassesArray) {
            visitInnerClass(innerClass);
        }
    }

    @Override
    public void visitInnerClass(InnerClass innerClass) {
        if (!checkFields()) {
            return;
        }
        super.visitInnerClass(innerClass);
    }

    private boolean checkFields() {
        if (javaClass == null || externalClassesWithinProject == null) {
            System.err.println("Fields were not set in " + BinaryDependencyVisitor.class.toString() + ".");
            System.err.println("Use of method visit(JavaClass, HashSet<String>) is required.");
            return false;
        }
        return true;
    }

    static String className(Type type) {
        if (type.getType() <= Constants.T_VOID) {
            return "java.PRIMITIVE";
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return className(arrayType.getBasicType());
        } else {
            return type.toString();
        }
    }

    public Map<String, Set<MethodSignature>> getCalledMethodsOfProjectClass() {
        return this.calledMethodsOfProjectClass;
    }

    public Set<String> getExternalClassesUsed() {
        return externalClassesUsed;
    }

    public Set<String> getProjectInterfaces() {
        return projectInterfaces;
    }

    public Set<String> getSuperClasses() {
        return superClasses;
    }
}
