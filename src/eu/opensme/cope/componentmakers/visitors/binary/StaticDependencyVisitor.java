/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.visitors.binary;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 *
 * @author sskalist
 */
public class StaticDependencyVisitor extends EmptyVisitor {

    private HashSet<MethodSignature> methodsCalled;
    private HashSet<Method> staticMethodsCalled;

    public StaticDependencyVisitor() {
        methodsCalled = new HashSet<MethodSignature>();
        staticMethodsCalled = new HashSet<Method>();
    }

    public void visit(JavaClass javaClass, Set<MethodSignature> methodsCalled) {
        if (javaClass == null || methodsCalled == null) {
            return;
        }
        this.methodsCalled.clear();
        this.staticMethodsCalled.clear();
        this.methodsCalled.addAll(methodsCalled);
        visitJavaClass(javaClass);
    }

    @Override
    public void visitJavaClass(JavaClass javaClass) {
        super.visitJavaClass(javaClass);
        for(Method method : javaClass.getMethods())
            method.accept(this);
    }

    
    
    @Override
    public void visitMethod(Method method) {
        super.visitMethod(method);
        if (method.isStatic()) {
            staticMethodsCalled.add(method);
        }
    }

    public Set<MethodSignature> getStaticMethodsCalled() {
        List<MethodSignature> toBeRemoved = new ArrayList<MethodSignature>();
        Iterator<MethodSignature> iterator = methodsCalled.iterator();
        while(iterator.hasNext()){
            MethodSignature methodSignature = iterator.next();
            for(Method method:staticMethodsCalled)
                if(!methodSignature.equals(method))
//                    iterator.remove();
                    toBeRemoved.add(methodSignature);
        }
        methodsCalled.removeAll(toBeRemoved);
        return methodsCalled;
    }
}
