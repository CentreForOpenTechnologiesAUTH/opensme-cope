/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.source;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sskalist
 */
public class ImplementInterfaceVisitor extends VoidVisitorAdapter<String[]> {

    public ImplementInterfaceVisitor() {
    }

    @Override
    public void visit(CompilationUnit n, String arg[]) {
        super.visit(n, arg);
        if (arg.length != 3) {
            return;
        }
        List<ImportDeclaration> imports = n.getImports();
        if (imports == null) {
            imports = new ArrayList<ImportDeclaration>();
        }
        imports.add(new ImportDeclaration(new NameExpr(arg[1]), false, false));
        n.setImports(imports);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, String arg[]) {
        super.visit(n, arg);
        if (arg.length != 3) {
            return;
        }
        if (n.getName().equals(arg[0])) {
            boolean alreadyExists = false;
            List<ClassOrInterfaceType> list;
            if (n.isInterface()) {
                list = n.getExtends();
            } else {
                list = n.getImplements();
            }
            if (list == null) {
                list = new ArrayList<ClassOrInterfaceType>();
            }
            for (ClassOrInterfaceType type : list) {
                if (type.getName().equals(arg[2])) {
                    alreadyExists = true;
                    break;
                }
            }
            if(!alreadyExists)
            {
                list.add(new ClassOrInterfaceType(arg[2]));
                if(n.isInterface())
                {
                    n.setExtends(list);
                }
                else{
                    n.setImplements(list);
                }
            }
        }
    }
}