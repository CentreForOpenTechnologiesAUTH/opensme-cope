/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.source;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 *
 * @author sskalist
 */
public class ChangeClassNameVisitor extends VoidVisitorAdapter<String []>{
    @Override
    public void visit(ClassOrInterfaceDeclaration n, String[] arg)
    {
        super.visit(n, arg);
        if(arg.length!=2)
            return;
        if(n.getName().equals(arg[0]))
        {
            n.setName(arg[1]);
        }
    }
}
