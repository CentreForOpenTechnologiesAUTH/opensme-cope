/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.source;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class MethodExtractorVisitor extends VoidVisitorAdapter<Object> {

    private HashSet<MethodSignature> methods;

    public MethodExtractorVisitor() {
        methods = new HashSet<MethodSignature>();
    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        super.visit(n, arg);
        String name = n.getName();
        List<Parameter> parametersList = n.getParameters();
        String parameterTypes[];
        if (parametersList == null) {
            parameterTypes = new String[0];
        } else {
            parameterTypes = new String[parametersList.size()];
            int i = 0;
            for (Parameter parameter : parametersList) {
                parameterTypes[i++] = parameter.getType().toString();
            }
        }
        List<NameExpr> throwsList = n.getThrows();
        String throwsTypes[];
        if (throwsList == null) {
            throwsTypes = new String[0];
        } else {
            throwsTypes = new String[throwsList.size()];
            int i = 0;
            for(NameExpr throwType : throwsList){
                throwsTypes[i++] = throwType.getName();
            }
        }
        String returnType = n.getType().toString();
        MethodSignature method = new MethodSignature(name, parameterTypes, returnType);
        method.setStatic(ModifierSet.isStatic(n.getModifiers()));
        method.setThrows(throwsTypes);
        methods.add(method);
    }

    public Set<MethodSignature> getMethods() {
        return methods;
    }
}
