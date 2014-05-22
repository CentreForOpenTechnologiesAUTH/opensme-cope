package eu.opensme.cope.componentmakers.interfacemaker;

import eu.opensme.cope.componentmakers.IPolicy;
import eu.opensme.cope.componentmakers.visitors.source.ImplementInterfaceVisitor;
import eu.opensme.cope.componentmakers.ComponentMaker;
import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.InterfaceGenerationPolicy;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.componentmakers.visitors.binary.BinaryDependencyVisitor;
import eu.opensme.cope.componentmakers.visitors.binary.StaticDependencyVisitor;
import eu.opensme.cope.componentmakers.visitors.source.ChangeClassNameVisitor;
import eu.opensme.cope.componentmakers.visitors.source.ClassToInterfaceVisitor;
import eu.opensme.cope.componentmakers.visitors.source.FixImportsVisitor;
import eu.opensme.cope.componentmakers.visitors.source.MethodExtractorVisitor;
import eu.opensme.cope.componentmakers.visitors.source.RemoveMethodsVisitor;
import eu.opensme.cope.componentmakers.visitors.source.RemoveInvalidModifiersVisitor;
import eu.opensme.cope.componentmakers.visitors.source.ReplaceTypeWithVisitor;
import japa.parser.ast.CompilationUnit;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

/**
 * This maker can be used for both provided and required Interface making
 * @author sskalist
 */
public class InterfaceMaker extends ComponentMaker {
    
    private BinaryDependencyVisitor dependencyFinder;
    private StaticDependencyVisitor staticFinder;
    private HashSet<String> interfacesUsed;
    private HashSet<String> superClassesUsed;
    private HashSet<String> externalClassesUsed;
    private HashSet<String> staticallyClassesUsed;
    private Map<String, Set<MethodSignature>> calledMethodsOfClass;
    private HashSet<String> made = new HashSet<String>();
    private boolean componentHasGrown;
    private HashMap<String, String> providedInterfaces;
    private HashMap<String, String> requiredInterfaces;
    private HashSet<String> componentMissingClasses;
    private HashSet<String> componentMissingTransformedClasses;
    private HashMap<String, String> replaceTypeWith;
    private HashMap<String, Set<MethodSignature>> methodsOfInterface;
    private InterfaceGenerationPolicy interfaceGenerationPolicy;
    
    public InterfaceMaker() {
        this.dependencyFinder = new BinaryDependencyVisitor();
        this.staticFinder = new StaticDependencyVisitor();
        this.interfacesUsed = new HashSet<String>();
        this.superClassesUsed = new HashSet<String>();
        this.externalClassesUsed = new HashSet<String>();
        this.staticallyClassesUsed = new HashSet<String>();
        this.calledMethodsOfClass = new HashMap<String, Set<MethodSignature>>();
        this.providedInterfaces = new HashMap<String, String>();
        this.requiredInterfaces = new HashMap<String, String>();
        this.componentClasses = new HashSet<String>();
        this.componentMissingClasses = new HashSet<String>();
        this.componentMissingTransformedClasses = new HashSet<String>();
        this.replaceTypeWith = new HashMap<String, String>();
        this.methodsOfInterface = new HashMap<String, Set<MethodSignature>>();
        this.interfaceGenerationPolicy = new InterfaceGenerationPolicy();
    }
    
    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        
        clearComponentInformation();
        clearInterfaces();
        super.makeComponent(componentName, componentFiles);
        for (String fileOrDirectory : componentFiles) {
            makeComponent(new File(fileOrDirectory));
        }
        this.componentHasGrown = true;
        while (this.componentHasGrown) {
            this.componentHasGrown = false;
            findStaticDependencies();
            moveRequiredClassesIntoComponent();
        }
        loadAllCompilationUnits();
        if (this.interfaceGenerationPolicy.isGenerateUnusedClasses()) {
            createInterfacesForUnusedClasses(providedInterfaces, false);
        }
        if (this.interfaceGenerationPolicy.isGenerateExternallyCalledClass()) {
            slice(requiredInterfaces);
            
            replaceTypes();
            fixImports();       
            generateInterfaces(requiredInterfaces);
        }
        generateSource();
        
        clearComponentInformation();
        for (String className : externalClassesUsed) {
            make(ComponentMaker.qualifiedNameToSourceFilename.get(className));
        }
        slice(providedInterfaces);
        generateInterfaces(providedInterfaces);
    }
    
    private void loadAllCompilationUnits() throws SourceFileNotFoundException {
        HashSet<String> allClasses = new HashSet<String>();
        allClasses.addAll(this.componentClasses);
        allClasses.addAll(this.externalClassesUsed);
        for (String className : allClasses) {
            getCompilationUnitOfClass(className);
        }
    }
    
    private void clearComponentInformation() {
        interfacesUsed.clear();
        superClassesUsed.clear();
        externalClassesUsed.clear();
        calledMethodsOfClass.clear();
        componentMissingClasses.clear();
        componentMissingTransformedClasses.clear();
        replaceTypeWith.clear();
        staticallyClassesUsed.clear();
        made.clear();
    }
    
    private void clearInterfaces() {
        providedInterfaces.clear();
        requiredInterfaces.clear();
        methodsOfInterface.clear();
    }
    
    private void makeComponent(File fileOrDirectory) throws BinaryFileNotFoundException {
        if (fileOrDirectory.isFile()) {
            make(fileOrDirectory.getAbsolutePath());
        } else if (fileOrDirectory.isDirectory()) {
            for (File file : fileOrDirectory.listFiles()) {
                makeComponent(file);
            }
        }
    }
    
    private void make(String filePath) throws BinaryFileNotFoundException {
        JavaClass javaClass = null;
        String qualifiedName = ComponentMaker.qualifiedNameOfRelativeSourceFile(relativePathOfSourceFile(filePath));
        
        if (!qualifiedNameToBinaryFilename.containsKey(qualifiedName)) {
            throw new BinaryFileNotFoundException(qualifiedName, filePath);
        }
        
        try {
            javaClass = new ClassParser(getJarPath(), ComponentMaker.sourceFileToBinaryFile(filePath)).parse();
        } catch (IOException ex) {
            Logger.getLogger(InterfaceMaker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassFormatException ex) {
            Logger.getLogger(InterfaceMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (javaClass == null) {
            return;
        }
        
        if (this.made.contains(filePath)) {
            return;
        }
        this.made.add(filePath);
        populateDependencies(javaClass);
    }
    
    private void populateDependencies(JavaClass javaClass) {
        dependencyFinder.visit(javaClass, externalClasses);
        this.interfacesUsed.addAll(dependencyFinder.getProjectInterfaces());
        this.superClassesUsed.addAll(dependencyFinder.getSuperClasses());
        this.externalClassesUsed.addAll(dependencyFinder.getExternalClassesUsed());
        
        Map<String, Set<MethodSignature>> calledMethods = dependencyFinder.getCalledMethodsOfProjectClass();
        for (String calledMethod : calledMethods.keySet()) {
            if (!this.calledMethodsOfClass.containsKey(calledMethod)) {
                this.calledMethodsOfClass.put(calledMethod, calledMethods.get(calledMethod));
            } else {
                this.calledMethodsOfClass.get(calledMethod).addAll(calledMethods.get(calledMethod));
            }
        }
    }
    
    private void findStaticDependencies() {
        HashSet<String> calledClasses = new HashSet<String>();
        calledClasses.addAll(calledMethodsOfClass.keySet());
        for (String className : calledClasses) {
            JavaClass javaClass = null;
            try {
                javaClass = Repository.lookupClass(className);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(InterfaceMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (javaClass == null) {
                return;
            }
            staticFinder.visit(javaClass, calledMethodsOfClass.get(className));
            if (!staticFinder.getStaticMethodsCalled().isEmpty()) {
                this.staticallyClassesUsed.add(className);
                populateDependencies(javaClass);
            }
        }
    }
    
    private void moveRequiredClassesIntoComponent() {
        moveInterfacesIntoComponent();
        moveSuperTypesIntoComponent();
        moveStaticTypesIntoComponent();
    }
    
    private void moveInterfacesIntoComponent() {
        moveSetIntoComponent(this.interfacesUsed);
    }
    
    private void moveSuperTypesIntoComponent() {
        moveSetIntoComponent(this.superClassesUsed);
    }
    
    private void moveStaticTypesIntoComponent() {
        moveSetIntoComponent(this.staticallyClassesUsed);
    }
    
    private void moveSetIntoComponent(HashSet<String> classes) {
        HashSet<String> remainingClasses = null;
        remainingClasses = ((HashSet<String>) classes.clone());
        
        for (String javaClass : remainingClasses) {
            if (!componentClasses.contains(javaClass)) {
                componentHasGrown = true;
                super.addClassToComponent(javaClass);
                //removeAll is used because inner classes bring the whole source class
                externalClassesUsed.removeAll(componentClasses);
                externalClasses.removeAll(componentClasses);
                this.superClassesUsed.removeAll(componentClasses);
                this.interfacesUsed.removeAll(componentClasses);
            }
        }
    }
    
    private void slice(Map<String, String> interfaceSet) throws SourceFileNotFoundException {
        moveRequiredClassesIntoComponent();
        externalClasses.removeAll(componentClasses);
        externalClassesUsed.removeAll(componentClasses);
        HashSet<String> externalClassesUsedClone = (HashSet<String>) externalClassesUsed.clone();
        for (String javaClass : externalClassesUsedClone) {
            if (this.superClassesUsed.contains(javaClass) || this.interfacesUsed.contains(javaClass)) {
                System.err.println("Found external class within super/interface class");
                continue;
            }
            if (javaClass.contains("$")) {
                continue;
            }
            try {
                createInterface(javaClass, interfaceSet);
            } catch (SourceFileNotFoundException ex) {
                this.componentMissingTransformedClasses.add(ex.getClassName());
                //Propagate exception
                throw new SourceFileNotFoundException(ex.getClassName(), ex.getSourcePath());
            }
        }
        
    }
    
    private void createInterface(String javaClass, Map<String, String> interfaceSet) throws SourceFileNotFoundException {
        CompilationUnit interfaceClass = getCompilationUnitOfClass(javaClass);
        String className = javaClass.substring(Math.max(javaClass.lastIndexOf("."), javaClass.lastIndexOf("$")) + 1);
        String packageName = javaClass.substring(0, javaClass.lastIndexOf(".") + 1);
        String interfaceName = className;
        new RemoveMethodsVisitor(javaClass).visit(interfaceClass, calledMethodsOfClass);
        if (!projectInterfaces.contains(javaClass)) {
            interfaceName = "I" + className;
            new RemoveInvalidModifiersVisitor(className).visit(interfaceClass, className);
            new ClassToInterfaceVisitor(className).visit(interfaceClass, className);
            new ChangeClassNameVisitor().visit(interfaceClass, new String[]{className, interfaceName});
            componentClasses.add(packageName + interfaceName);
            classToCompilationUnit.put(packageName + interfaceName, interfaceClass);
            if (!staticallyClassesUsed.contains(javaClass)) {
                this.replaceTypeWith.put(packageName + className, packageName + interfaceName);
            }
            if (interfaceSet.equals(this.requiredInterfaces)) {
                if (classToCompilationUnit.containsKey(packageName + className)) {
                    classToCompilationUnit.remove(packageName + className);
                }
            } else if (interfaceSet.equals(this.providedInterfaces)) {
                String args[] = new String[]{className, packageName + interfaceName, interfaceName};
                new ImplementInterfaceVisitor().visit(getCompilationUnitOfClass(javaClass), args);
            }
            
        }
        MethodExtractorVisitor methodExtractor = new MethodExtractorVisitor();
        methodExtractor.visit(interfaceClass, null);
        this.methodsOfInterface.put(packageName + interfaceName, methodExtractor.getMethods());
        interfaceSet.put(packageName + interfaceName, packageName + className);
        
        return;
    }
    
    public void generateProvidedInterface(String classFilename) throws SourceFileNotFoundException {
        String qualifiedName = ComponentMaker.qualifiedNameOfRelativeSourceFile(classFilename.replaceFirst(ComponentMaker.getSourcePath(), ""));
        String className = qualifiedName.substring(Math.max(qualifiedName.lastIndexOf("."), qualifiedName.lastIndexOf("$")) + 1);
        String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf(".") + 1);
        String interfaceName = className;
        if (!providedInterfaces.containsKey(packageName + interfaceName)) {
            interfaceName = "I" + className;
            CompilationUnit interfaceClass = createNewCompilationUnitOfClass(qualifiedName);
            CompilationUnit javaClass = getCompilationUnitOfClass(qualifiedName);
            new RemoveInvalidModifiersVisitor(className).visit(interfaceClass, className);
            new ClassToInterfaceVisitor(className).visit(interfaceClass, className);
            new ChangeClassNameVisitor().visit(interfaceClass, new String[]{className, interfaceName});
            classToCompilationUnit.put(packageName + interfaceName, interfaceClass);
            providedInterfaces.put(packageName + interfaceName, packageName + className);
            String args[] = new String[]{className, packageName + interfaceName, interfaceName};
            new ImplementInterfaceVisitor().visit(javaClass, args);
            MethodExtractorVisitor methodExtractor = new MethodExtractorVisitor();
            methodExtractor.visit(interfaceClass, null);
            this.methodsOfInterface.put(packageName + interfaceName, methodExtractor.getMethods());
            generateCompilationUnit(javaClass, true);
            generateCompilationUnit(interfaceClass, true);
        }
    }
    
    private void replaceTypes() throws SourceFileNotFoundException {
        HashSet<String> allClasses = new HashSet<String>();
        allClasses.addAll(externalClassesUsed);
        allClasses.addAll(componentClasses);
        for (String javaClass : allClasses) {
            CompilationUnit unit = getCompilationUnitOfClass(javaClass);
            new ReplaceTypeWithVisitor(this.replaceTypeWith).visit(unit, null);
        }
    }
    
    private void fixImports() throws SourceFileNotFoundException {
        HashSet<String> allClasses = new HashSet<String>();
        allClasses.addAll(componentClasses);
        allClasses.addAll(this.requiredInterfaces.keySet());
        for (String javaClass : allClasses) {
            CompilationUnit unit = getCompilationUnitOfClass(javaClass);
            new FixImportsVisitor().visit(unit, new Set[]{ComponentMaker.projectClasses, allClasses});
        }
    }
    
    private void generateInterfaces(HashMap<String, String> interfaceSet) throws SourceFileNotFoundException {
        for (String interfaceName : interfaceSet.keySet()) {
            CompilationUnit compilationUnitOfClass = getCompilationUnitOfClass(interfaceName);
            if (compilationUnitOfClass == null) {
                continue;
            }
            super.generateCompilationUnit(compilationUnitOfClass, false);
        }
    }
    
    private void createInterfacesForUnusedClasses(HashMap<String, String> interfaceSet, boolean isRequired) throws SourceFileNotFoundException {
        HashSet<String> unusedClasses = new HashSet<String>(componentClasses);
        for (String className : componentClasses) {
            JavaClass javaClass = null;
            try {
                javaClass = Repository.lookupClass(className);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(InterfaceMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (javaClass == null) {
                continue;
            }
            HashSet<String> classes = new HashSet<String>(componentClasses);
            classes.remove(className);
            BinaryDependencyVisitor binaryDependencyVisitor = new BinaryDependencyVisitor();
            binaryDependencyVisitor.visit(javaClass, classes);
            unusedClasses.removeAll(binaryDependencyVisitor.getCalledMethodsOfProjectClass().keySet());
        }
        
        for (String javaClass : unusedClasses) {
            if (javaClass.contains("$")) {
                continue;
            }
            String className = javaClass.substring(Math.max(javaClass.lastIndexOf("."), javaClass.lastIndexOf("$")) + 1);
            String packageName = javaClass.substring(0, javaClass.lastIndexOf(".") + 1);
            String interfaceName = className;
            if (!projectInterfaces.contains(javaClass) && !interfaceSet.containsKey(javaClass)) {
                interfaceName = "I" + className;
                CompilationUnit interfaceClass = createNewCompilationUnitOfClass(javaClass);
                new RemoveInvalidModifiersVisitor(className).visit(interfaceClass, className);
                new ClassToInterfaceVisitor(className).visit(interfaceClass, className);
                new ChangeClassNameVisitor().visit(interfaceClass, new String[]{className, interfaceName});
                classToCompilationUnit.put(packageName + interfaceName, interfaceClass);
                if (isRequired) {
                    if (classToCompilationUnit.containsKey(packageName + className)) {
                        classToCompilationUnit.remove(packageName + className);
                        System.err.println("Creating required when provided was meant.");
                    }
                } else {
                    String args[] = new String[]{className, packageName + interfaceName, interfaceName};
                    new ImplementInterfaceVisitor().visit(getCompilationUnitOfClass(javaClass), args);
                }
            }
            CompilationUnit interfaceClass = getCompilationUnitOfClass(packageName + interfaceName);
            MethodExtractorVisitor methodExtractor = new MethodExtractorVisitor();
            methodExtractor.visit(interfaceClass, null);
            this.methodsOfInterface.put(packageName + interfaceName, methodExtractor.getMethods());
            interfaceSet.put(packageName + interfaceName, packageName + className);
            
        }
    }
    
    private void generateSource() {
        for (String componentClass : componentClasses) {
            CompilationUnit componentUnit = null;
            try {
                componentUnit = getCompilationUnitOfClass(componentClass);
            } catch (SourceFileNotFoundException ex) {
                this.componentMissingClasses.add(componentClass);
            }
            if (componentUnit == null) {
                continue;
            }
            HashSet<String> allClasses = new HashSet<String>();
            allClasses.addAll(componentClasses);
            new FixImportsVisitor().visit(componentUnit, new Set[]{ComponentMaker.projectClasses, allClasses});
            super.generateCompilationUnit(componentUnit, false);
        }
    }
    
    public Map<String, String> getProvidedInterfaces() {
        return providedInterfaces;
    }
    
    public Map<String, String> getRequiredInterfaces() {
        return requiredInterfaces;
    }
    
    public HashMap<String, Set<MethodSignature>> getMethodsOfInterface() {
        return methodsOfInterface;
    }
    
    @Override
    public void setPolicy(IPolicy policy) {
        super.setPolicy(policy);
        if (policy instanceof InterfaceGenerationPolicy) {
            this.interfaceGenerationPolicy = (InterfaceGenerationPolicy) policy;
        }
    }

    public InterfaceGenerationPolicy getPolicy() {
        return interfaceGenerationPolicy;
    }
    
}
