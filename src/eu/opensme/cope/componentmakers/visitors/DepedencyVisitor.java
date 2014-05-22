/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class DepedencyVisitor extends VoidVisitorAdapter<Set<String>> {

    private HashSet externalClasses;
    private HashMap<String, String> variableToType;
    private HashMap<String, HashSet<String>> calledMethodsOfType;

    public DepedencyVisitor() {
        super();
        externalClasses = new HashSet();
        variableToType = new HashMap<String, String>();
        calledMethodsOfType = new HashMap<String, HashSet<String>>();
    }

    private void addType(ReferenceType referenceType, Set<String> externalClassesWithinProject) {
        if (referenceType.getType() instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType parameterType = (ClassOrInterfaceType) referenceType.getType();
            addType(parameterType, externalClassesWithinProject);
        }
    }
    
    private void addType(ClassOrInterfaceType classOrInterfaceType, Set<String> externalClassesWithinProject) {
        if (externalClassesWithinProject != null && externalClassesWithinProject.contains(classOrInterfaceType.getName())) {
                externalClasses.add(classOrInterfaceType.getName());
            }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Set<String> externalClassesWithinProject) {
        super.visit(n, externalClassesWithinProject);
        List<ClassOrInterfaceType> extentions = n.getExtends();
        if(extentions!=null)
        {
            for(ClassOrInterfaceType type : extentions)
            {
                addType(type, externalClassesWithinProject);
            }
        }
        
        List<ClassOrInterfaceType> implementations = n.getImplements();
        if(implementations!=null)
        {
            for(ClassOrInterfaceType type : implementations)
            {
                addType(type, externalClassesWithinProject);
            }
        }
    }

    
    
    @Override
    public void visit(VariableDeclarationExpr n, Set<String> externalClassesWithinProject) {
        super.visit(n, externalClassesWithinProject);
        if (n.getType() instanceof ReferenceType) {
            ReferenceType referenceType = (ReferenceType) n.getType();
            addType(referenceType, externalClassesWithinProject);
            if (referenceType.getType() instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType complexType = (ClassOrInterfaceType) referenceType.getType();
                for (VariableDeclarator var : n.getVars()) {
                    this.variableToType.put(var.getId().getName(), complexType.getName());
                    System.err.println("Declared Variable " + var.getId().getName() + " of Type " + complexType.getName());
                }
            }
        }

        //variableToType.put(n., null)
    }

    @Override
    public void visit(FieldDeclaration n, Set<String> externalClassesWithinProject) {
        super.visit(n, externalClassesWithinProject);
        if (n.getType() instanceof ReferenceType) {
            ReferenceType referenceType = (ReferenceType) n.getType();
            addType(referenceType, externalClassesWithinProject);
            if (referenceType.getType() instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType complexType = (ClassOrInterfaceType) referenceType.getType();
                for (VariableDeclarator var : n.getVariables()) {
                    this.variableToType.put(var.getId().getName(), complexType.getName());
                }
            }
        }
    }

    @Override
    public void visit(Parameter n, Set<String> externalClassesWithinProject) {
        super.visit(n, externalClassesWithinProject);
        if (n.getType() instanceof ReferenceType) {
            ReferenceType referenceType = (ReferenceType) n.getType();
            addType(referenceType, externalClassesWithinProject);
            if (referenceType.getType() instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType complexType = (ClassOrInterfaceType) referenceType.getType();
                this.variableToType.put(n.getId().getName(), complexType.getName());
            }
        }

    }

    @Override
    public void visit(ClassExpr n, Set<String> externalClassesWithinProject) {
        super.visit(n, externalClassesWithinProject);
        // TODO check for abstract class before printing the error
        System.err.println("External class static Call" + n.toString());
//        if (n.getType() instanceof ReferenceType) {
//            add((ReferenceType) n.getType(), arg);
//        }
    }

    @Override
    public void visit(MethodCallExpr n, Set<String> arg) {
        super.visit(n, arg);
        if (n.getScope() != null) {
            String variableType = this.variableToType.get(n.getScope().toString());
            if (variableType != null) {
                if (!this.calledMethodsOfType.containsKey(variableType)) {
                    this.calledMethodsOfType.put(variableType, new HashSet<String>());
                }
                this.calledMethodsOfType.get(variableType).add(n.getName());
            }
        }

    }

    public HashSet getExternalClasses() {
        return externalClasses;
    }

    public HashMap<String, HashSet<String>> getCalledMethodsOfType() {
        return calledMethodsOfType;
    }
}
