package eu.opensme.cope.componentvalidator.binaryvisitor;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import eu.opensme.cope.componentmakers.common.MethodSignature;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

/**
 *
 * @author thanasis
 */
public class ClassBinaryParser extends EmptyVisitor {
 
    private JavaClass javaBinaryClass;
    private Set<MethodSignature> methodsToParse;
    private MethodBinaryParser methodVisitor;
    private static Map<String,JavaClass> binaryClassPathBinaryJavaClassMap = new HashMap<String,JavaClass>();

    public ClassBinaryParser() {
        methodVisitor = new MethodBinaryParser();
    }
       
    
    public void populateJavaClass(String binaryFile, Set<MethodSignature> methods) {
        try {
            if(!binaryClassPathBinaryJavaClassMap.containsKey(binaryFile)){
                javaBinaryClass = new ClassParser(binaryFile).parse();
                binaryClassPathBinaryJavaClassMap.put(binaryFile, javaBinaryClass);
            } else {
                javaBinaryClass = binaryClassPathBinaryJavaClassMap.get(binaryFile);
            }
            methodsToParse = methods;
            visitJavaClass(javaBinaryClass);
        } catch (IOException ex) {
            Logger.getLogger(ClassBinaryParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassFormatException ex) {
            Logger.getLogger(ClassBinaryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void visitJavaClass(JavaClass javaClass) {
        super.visitJavaClass(javaClass);

        populateMethodsDependencies();

    }
    
    private void populateMethodsDependencies() {
        for (Method method : this.javaBinaryClass.getMethods()) {
            MethodSignature methodSignature = getMethodSignature(method);
            if(methodsToParse.contains(methodSignature)){
                method.accept(this);
            }
        }
    }
    
    private MethodSignature getMethodSignature(Method method) {
        Type[] parameterTypes = method.getArgumentTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = JavaBinaryParser.className(parameterTypes[i],false);
        }
        return new MethodSignature(method.getName(), parameterTypeNames, JavaBinaryParser.className(method.getReturnType(),false));
    }
    
    @Override
    public void visitMethod(Method method) {
        super.visitMethod(method);
        methodVisitor.visit(javaBinaryClass, method);
    }
    
    public static List<Method> getInterfaceMethods(String binaryFile){
        try {
            JavaClass binaryClass = new ClassParser(binaryFile).parse();
            return Arrays.asList(binaryClass.getMethods());
        } catch (IOException ex) {
            Logger.getLogger(ClassBinaryParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassFormatException ex) {
            Logger.getLogger(ClassBinaryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.EMPTY_LIST;
    }
}
