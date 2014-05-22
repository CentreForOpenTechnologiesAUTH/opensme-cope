package eu.opensme.cope.componentvalidator.binaryvisitor;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.componentvalidator.util.Utils;
import eu.opensme.cope.domain.GeneratedComponent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.ArrayType;


/**
 *
 * @author thanasis
 */
public class JavaBinaryParser {

    protected static String currentProvidedInterface; 
    public static Map<String,Set<String>> interfacesMethodsToCover;
    protected static List<String> componentClassesQualifiedNames;
    protected static Map<String,Set<MethodSignature>> classesMethodsMapping = new HashMap<String, Set<MethodSignature>>();
    private GeneratedComponent component;
    private static String tmpCompileDirPath;

    
    public JavaBinaryParser(GeneratedComponent component, String tmpPath) {
        try { 
            this.component = component;
            
            tmpCompileDirPath = tmpPath;
            
            componentClassesQualifiedNames = new ArrayList<String>();
            interfacesMethodsToCover = new HashMap<String,Set<String>>();
            List<String> javaFilesList = Utils.getJavaFileList(new File(component.getComponentSourceFolder()));
            
            String oldClassPath = System.getProperty("java.class.path");
        
            Utils.setClassPath("");


            List<String> externalJars = new ArrayList<String>();
            for (String library : component.getComponentLibraries()) {
                Utils.addToClassPath(library);
                externalJars.add(library);
            }

            try {
                Utils.addtoURLClassLoader(externalJars);
            } catch (Exception ex) {
            }
            
            
            Utils.compilePlainScenario("", tmpCompileDirPath, javaFilesList, false);
            
            Utils.setClassPath(oldClassPath);
            
            for(String javaFile : javaFilesList){
                String classQualifiedName = Utils.getThePackage(javaFile, true);
                componentClassesQualifiedNames.add(classQualifiedName);
            }
        } catch (IOException ex) {
            Logger.getLogger(JavaBinaryParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(JavaBinaryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void findInterfaceMethodsForCoverage(){
        for(String providedInterface : component.getProvidedInterfacesMap().keySet()){
            currentProvidedInterface = providedInterface;
            
            HashSet<String> methodsSet = new HashSet<String>();
            for(MethodSignature methodSingature : component.getMethodsOfInterface(providedInterface)){
                String implementedClassQualifiedName = component.getProvidedInterfacesMap().get(providedInterface);
                methodsSet.add(methodSignatureToString(implementedClassQualifiedName, methodSingature));
            }
            interfacesMethodsToCover.put(providedInterface, methodsSet);
            
            String compiledClassDir = qualifiedNameCompiledClassDirectoryMapping(component.getProvidedInterfacesMap().get(providedInterface));
            classesMethodsMapping.put(compiledClassDir,component.getMethodsOfInterface(providedInterface));
            
            while(!classesMethodsMapping.isEmpty()){
                Iterator<String> iterator = classesMethodsMapping.keySet().iterator();
                while(iterator.hasNext()){
                    String binaryClass = iterator.next();
                    parseClasses(binaryClass, classesMethodsMapping.get(binaryClass));
                    classesMethodsMapping.remove(binaryClass);
                    break;
                }
            }
        }
    }
    
    public Map<String,Set<String>> getInterfaceMethodsToCover(){
        return interfacesMethodsToCover;
    }
    
    protected static void parseClasses(String binaryClassPath, Set<MethodSignature> methods){
        new ClassBinaryParser().populateJavaClass(binaryClassPath, methods);
    }
    
    protected static void addMethod(String method){
        interfacesMethodsToCover.get(currentProvidedInterface).add(method);
    }

    protected static String qualifiedNameCompiledClassDirectoryMapping(String qualifiedName) {
        qualifiedName = tmpCompileDirPath + qualifiedName.replace(".", "/") + ".class";
        return qualifiedName;
    }
    
    protected static String className(Type type, boolean qualified) {
        if (type == null) {
            return "PRIMITIVE";
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return className(arrayType.getBasicType(), qualified);
        } else {
            String noDotType = type.toString();
            if(noDotType.contains(".") && !qualified){
                noDotType = noDotType.substring(noDotType.lastIndexOf(".")+1);
            }
            return noDotType;
        }
    }
    
    public static String methodSignatureToString(String className, MethodSignature methodSignature){
        String parameters = "";
        for(int i = 0; i < methodSignature.getParameters().length; i++){
           parameters =  parameters.concat(methodSignature.getParameters()[i]);
           if(i != methodSignature.getParameters().length - 1){
               parameters =  parameters.concat(", ");
           }
        }
        String methodReturn = methodSignature.getReturnType();
        if(methodReturn.contains(".")){
            methodReturn = methodReturn.substring(methodReturn.lastIndexOf(".")+1);
        }
        return className + "." + methodSignature.getName() + "(" + parameters + "): " + methodReturn;
    }
    
}
