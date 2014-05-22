/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.source;

import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author sskalist
 */
public class ClassToInterfaceVisitor extends VoidVisitorAdapter<String> {

    private String currentClass;
    private final String className;

    public ClassToInterfaceVisitor(String className) {
        this.className = className;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, String arg) {
        if (currentClass == null) {
            currentClass = arg;
        }
        if (n.getName().equals(arg)) {
            List<ClassOrInterfaceType> newExtendsList = new ArrayList<ClassOrInterfaceType>();
            if (n.isInterface()) {
                if (n.getExtends() != null) {
                    newExtendsList.addAll(n.getExtends());
                }
            } else {
                if (n.getImplements() != null) {
                    newExtendsList.addAll(n.getImplements());
                }
            }
            n.setInterface(true);
            if (newExtendsList.isEmpty()) {
                n.setExtends(null);
            } else {
                n.setExtends(newExtendsList);
            }
            n.setImplements(null);

            List<BodyDeclaration> members = n.getMembers();
            if (members != null) {
                for (Iterator<BodyDeclaration> it = members.iterator(); it.hasNext();) {
                    BodyDeclaration bodyDeclaration = it.next();
                    if (bodyDeclaration instanceof ConstructorDeclaration) {
                        it.remove();
                    } else if (bodyDeclaration instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
                        if (ModifierSet.isPublic(method.getModifiers()) && !ModifierSet.isStatic(method.getModifiers()) && !ModifierSet.isFinal(method.getModifiers()) && !ModifierSet.isNative(method.getModifiers()) && !ModifierSet.isSynchronized(method.getModifiers())) {
                            visit(method, arg);
                        }else
                        {
                            it.remove();
                        }
                    } else {
                        it.remove();
                    }
                }
                n.setMembers(members);
            }
        } else {
            if (ModifierSet.isPrivate(n.getModifiers())) {
                n.setModifiers(ModifierSet.removeModifier(n.getModifiers(), ModifierSet.PRIVATE));
            }
            if (ModifierSet.isProtected(n.getModifiers())) {
                n.setModifiers(ModifierSet.removeModifier(n.getModifiers(), ModifierSet.PROTECTED));
            }
        }
    }

    @Override
    public void visit(MethodDeclaration n, String arg) {
        n.setBody(null);
        if (ModifierSet.isPrivate(n.getModifiers())) {
            n.setModifiers(ModifierSet.removeModifier(n.getModifiers(), ModifierSet.PRIVATE));
        }
        if (ModifierSet.isProtected(n.getModifiers())) {
            n.setModifiers(ModifierSet.removeModifier(n.getModifiers(), ModifierSet.PROTECTED));
        }

    }
}
