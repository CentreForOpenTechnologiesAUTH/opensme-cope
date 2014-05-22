/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.source;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class FixImportsVisitor extends VoidVisitorAdapter<Set<String>[]> {

    @Override
    public void visit(CompilationUnit n, Set<String> arg[]) {
        super.visit(n, arg);
        if (arg == null || arg.length != 2) {
            return;
        }
        Set<String> projectClasses = arg[0];
        Set<String> componentClasses = arg[1];
        List<ImportDeclaration> imports = n.getImports();
        if (imports != null) {
            for (Iterator<ImportDeclaration> iterator = imports.iterator(); iterator.hasNext();) {
                ImportDeclaration importDeclaration = iterator.next();
                if (importDeclaration.isAsterisk()) {
                    String packageName = importDeclaration.getName().toString();
                    packageName = packageName.substring(0, packageName.length() - 1); //remove the *
                    boolean shouldBeRemoved = true;
                    for (String className : projectClasses) {
                        if (!className.startsWith(packageName) || ( className.startsWith(packageName) && componentClasses.contains(className) )) {
                            shouldBeRemoved = false;
                        }
                    }
                    if (shouldBeRemoved) {
                        iterator.remove();
                    }
                }else if (projectClasses.contains(importDeclaration.getName().toString()) && !componentClasses.contains(importDeclaration.getName().toString())) {
                    iterator.remove();
                }
            }
        }
    }
}
