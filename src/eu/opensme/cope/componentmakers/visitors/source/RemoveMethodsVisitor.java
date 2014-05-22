package eu.opensme.cope.componentmakers.visitors.source;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class RemoveMethodsVisitor extends VoidVisitorAdapter<Map<String, Set<MethodSignature>>> {

    private String packageName;
    private String className;

    public RemoveMethodsVisitor(String className) {
        this.className = className;
    }
    

    @Override
    public void visit(CompilationUnit n, Map<String, Set<MethodSignature>> arg) {
        this.packageName = n.getPackage().getName().toString();
        super.visit(n, arg);

    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Map<String, Set<MethodSignature>> methodsCalledOfType) {
        super.visit(n, methodsCalledOfType);
        if(!className.endsWith(n.getName()))
            return;
        Set<MethodSignature> methods = methodsCalledOfType.get(this.packageName + "." + n.getName());
        if (methods == null) {
            return;
        }
        List<BodyDeclaration> members = n.getMembers();
        List<BodyDeclaration> toBekept = new ArrayList<BodyDeclaration>();
        if (members != null);
        {
            for (MethodSignature method : methods) {
                if (members.contains(method)) {
                    toBekept.add(members.get(members.indexOf(method)));
                }
            }
            //TODO optimize and handle Constructors as well
            for (Iterator<BodyDeclaration> it = members.iterator(); it.hasNext();) {
                BodyDeclaration body = it.next();
                if (body instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) body;
                    if (!toBekept.contains(method)) {
                        it.remove();
                    }
                }
            }
            return;
        }

    }
}
