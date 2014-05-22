package eu.opensme.cope.componentmakers.visitors;

import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 *
 * @author sskalist
 */
public class FullyQualifiedNameVisitor extends VoidVisitorAdapter<Object> {
    private String packageName;
    private String className;

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        super.visit(n, arg);
        this.className = n.getName();
    }

    @Override
    public void visit(PackageDeclaration n, Object arg) {
        super.visit(n, arg);
        this.packageName = n.getName().toString();
    }
    
    public String getQualifiedName()
    {
        return packageName +"."+ className;
    }

    public String getClassName() {
        return className;
    }
    
}
