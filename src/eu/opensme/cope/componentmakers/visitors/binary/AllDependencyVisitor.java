/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.binary;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.analyzers.dependencyTypeAnalyzer.SubtypeDependencyEnum;
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
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.Type;

/**
 *
 * @author sskalist
 */
public class AllDependencyVisitor extends EmptyVisitor {

    private JavaClass javaClass;
    private Set<String> externalClassesWithinProject;
    private HashSet<String> extendsClass;
    private HashSet<String> implementsClass;
    private HashSet<String> fieldDeclaration;
    private HashSet<String> localVariables;
    private static AllMethodVisitor methodVisitor = new AllMethodVisitor();
    private HashMap<String, Set<MethodSignature>> calledMethodsOfProjectClass;
    private HashMap<String, SubtypeDependencyEnum> fieldsAccessedOfProjectClass;
    private HashSet<String> newInstances;
    private HashSet<String> parameters;
    private HashSet<String> returnTypes;

    public AllDependencyVisitor() {
        extendsClass = new HashSet<String>();
        implementsClass = new HashSet<String>();
        fieldDeclaration = new HashSet<String>();
        newInstances = new HashSet<String>();
        localVariables = new HashSet<String>();
        parameters = new HashSet<String>();
        returnTypes = new HashSet<String>();
        fieldsAccessedOfProjectClass = new HashMap<String, SubtypeDependencyEnum>();
        this.calledMethodsOfProjectClass = new HashMap<String, Set<MethodSignature>>();

    }

    public void visit(JavaClass javaClass, Set<String> externalClassesWithinProject) {
        extendsClass.clear();
        implementsClass.clear();
        fieldDeclaration.clear();
        newInstances.clear();
        localVariables.clear();
        parameters.clear();
        returnTypes.clear();
        fieldsAccessedOfProjectClass.clear();
        this.calledMethodsOfProjectClass.clear();

        this.javaClass = javaClass;
        this.externalClassesWithinProject = externalClassesWithinProject;
        visitJavaClass(javaClass);
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
        populateFieldDependencies();
        populateMethodsDependencies();
        populate();
    }

    private void populateInterfacesDependencies() {
        if (this.javaClass.isInterface()) {
            populateInterfacesDependencies(this.javaClass, this.extendsClass);
        } else {
            populateInterfacesDependencies(this.javaClass, this.implementsClass);
        }

    }

    private void populateInterfacesDependencies(JavaClass javaClass, HashSet<String> interfaceHolder) {
        String[] interfaceNames = javaClass.getInterfaceNames();

        for (int i = 0; i < interfaceNames.length; i++) {
            if (externalClassesWithinProject.contains(interfaceNames[i])) {
                try {
                    JavaClass interfaceClass = Repository.lookupClass(interfaceNames[i]);
                    interfaceHolder.add(interfaceNames[i]); // Add it after finding its binary file
//                    for (Method method : interfaceClass.getMethods()) {
//                        if (method.isAbstract()) {
//                            if (!this.calledMethodsOfProjectClass.containsKey(interfaceClass.getClassName())) {
//                                this.calledMethodsOfProjectClass.put(interfaceClass.getClassName(), new HashSet<MethodSignature>());
//                            }
//                            Type[] parameterTypes = method.getArgumentTypes();
//                            String[] parameterTypeNames = new String[parameterTypes.length];
//                            for (int j = 0; j < parameterTypeNames.length; j++) {
//                                parameterTypeNames[j] = className(parameterTypes[j]);
//                            }
//                            this.calledMethodsOfProjectClass.get(interfaceClass.getClassName()).add(new MethodSignature(method.getName(), parameterTypeNames, className(method.getReturnType())));
//                        }
//                    }
                    populateInterfacesDependencies(interfaceClass, this.extendsClass);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(AllDependencyVisitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void populateSuperTypeDependencies() {

        String superName = this.javaClass.getSuperclassName();
        if (this.externalClassesWithinProject.contains(superName)) {
            this.extendsClass.add(superName);
        }
    }

    private void populateFieldDependencies() {
        Field[] fields = this.javaClass.getFields();
        for (Field field : fields) {
            field.accept(this);
            //visitField(field);
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

        localVariables.addAll(methodVisitor.getLocalVariables());
        newInstances.addAll(methodVisitor.getNewInstances());
        parameters.addAll(methodVisitor.getParameters());
        returnTypes.addAll(methodVisitor.getReturnTypes());

        Map<String, SubtypeDependencyEnum> fieldAccesses = methodVisitor.getFieldAccess();
        for (String className : fieldAccesses.keySet()) {
            if (!fieldsAccessedOfProjectClass.containsKey(className)) {
                fieldsAccessedOfProjectClass.put(className, fieldAccesses.get(className));
            } else {
                if (!fieldsAccessedOfProjectClass.get(className).equals(fieldAccesses.get(className))) {
                    fieldsAccessedOfProjectClass.put(className, SubtypeDependencyEnum.BOTH);
                }
            }
        }

        Map<String, Set<MethodSignature>> calledMethods = methodVisitor.getCalledMethodsOfProjectClass();
        for (String className : calledMethods.keySet()) {
            if (!this.calledMethodsOfProjectClass.containsKey(className)) {
                this.calledMethodsOfProjectClass.put(className, calledMethods.get(className));
            } else {
                this.calledMethodsOfProjectClass.get(className).addAll(calledMethods.get(className));
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
            fieldDeclaration.add(qualifiedName);
        }
    }

    private boolean checkFields() {
        if (javaClass == null || externalClassesWithinProject == null) {
            System.err.println("Fields were not set in " + AllDependencyVisitor.class.toString() + ".");
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

    public Set<String> getNewInstances() {
        return newInstances;
    }

    public HashSet<String> getExtendsClass() {
        return extendsClass;
    }

    public HashSet<String> getFieldDeclaration() {
        return fieldDeclaration;
    }

    public HashMap<String, SubtypeDependencyEnum> getFieldsAccessedOfProjectClass() {
        return fieldsAccessedOfProjectClass;
    }

    public HashSet<String> getImplementsClass() {
        return implementsClass;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public HashSet<String> getLocalVariables() {
        return localVariables;
    }

    public static AllMethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public HashSet<String> getParameters() {
        return parameters;
    }

    public HashSet<String> getReturnTypes() {
        return returnTypes;
    }
}
