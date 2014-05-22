/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.source;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author sskalist
 */
public class RemoveInvalidModifiersVisitor extends VoidVisitorAdapter<String> {

    private final String className;

    public RemoveInvalidModifiersVisitor(String className) {
        this.className = className;
    }

    @Override
    public void visit(CompilationUnit n, String typeName) {
        List<TypeDeclaration> types = n.getTypes();
        if (types != null) {
            for (Iterator<TypeDeclaration> it = types.iterator(); it.hasNext();) {
                TypeDeclaration typeDeclaration = it.next();
                if (ModifierSet.isStatic(typeDeclaration.getModifiers())) {
                    it.remove();
                }
            }
            n.setTypes(types);
        }
        super.visit(n, typeName);

    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, String typeName) {
        if (!className.endsWith(n.getName())) {
            return;
        }
        if (n.getName().equals(typeName)) {
            if (ModifierSet.isFinal(n.getModifiers())) {
                n.setModifiers(ModifierSet.removeModifier(n.getModifiers(), ModifierSet.FINAL));
            }
            List<BodyDeclaration> members = n.getMembers();
            if (members != null) {
                for (Iterator<BodyDeclaration> iterator = members.iterator(); iterator.hasNext();) {
                    BodyDeclaration bodyDeclaration = iterator.next();
                    if (bodyDeclaration instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
                        if (ModifierSet.isStatic(method.getModifiers()) || ModifierSet.isPrivate(method.getModifiers())) {
                            iterator.remove();
                        }
                        if (ModifierSet.isFinal(method.getModifiers())) {
                            method.setModifiers(ModifierSet.removeModifier(method.getModifiers(), ModifierSet.FINAL));
                        }
                        if (ModifierSet.isProtected(method.getModifiers())) {
                            method.setModifiers(ModifierSet.removeModifier(method.getModifiers(), ModifierSet.PROTECTED));
                        }
                        if (ModifierSet.isNative(method.getModifiers())) {
                            method.setModifiers(ModifierSet.removeModifier(method.getModifiers(), ModifierSet.NATIVE));
                        }
                        if (ModifierSet.isSynchronized(method.getModifiers())) {
                            method.setModifiers(ModifierSet.removeModifier(method.getModifiers(), ModifierSet.SYNCHRONIZED));
                        }
                    } else if (bodyDeclaration instanceof FieldDeclaration) {
                        iterator.remove();
                    } else if (bodyDeclaration instanceof InitializerDeclaration) {
                        iterator.remove();
                    }
                }
                n.setMembers(members);
                super.visit(n, typeName);
            }
        }
    }

    @Override
    public void visit(MethodDeclaration n, String arg) {
        super.visit(n, arg);
        List<AnnotationExpr> annotations = n.getAnnotations();
        if (annotations != null) {
            Iterator<AnnotationExpr> iterator = annotations.iterator();
            while (iterator.hasNext()) {
                AnnotationExpr annotation = iterator.next();
                if (annotation.getName().toString().equals("Override")) {
                    iterator.remove();
                }
            }
        }
    }
}
