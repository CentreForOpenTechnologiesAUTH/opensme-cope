package eu.opensme.cope.componentmakers;

import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.JavaBinaryFilter;
import eu.opensme.cope.componentmakers.common.DirectoryFilter;
import eu.opensme.cope.componentmakers.common.JavaSourceFilter;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.componentmakers.visitors.FullyQualifiedNameVisitor;
import eu.opensme.cope.domain.GeneratedComponent;
import eu.opensme.cope.domain.ReuseProject;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.TypeDeclaration;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;

/**
 * An abstract class to provide to each implemented ComponentMaker information
 * about the project and output and code generation mechanisms.
 * @author sskalist
 */
public abstract class ComponentMaker implements IComponentMaker {

    private HashSet<String> generatedFilenames;
    /**
     * Maps the filePath with the respective FileOutputStream object
     */
    private HashMap<String, FileOutputStream> fileOutputStreams;
    /**
     * Maps the filePath with the respective OutputStreamWriter object
     */
    private HashMap<String, OutputStreamWriter> outputStreamWriters;
    /**
     * The current OutputStreamWriter
     */
    private OutputStreamWriter currentWriter;
    /**
     * The current filename to which the current currentWriter writes.
     */
    private String currentFilename;
    private static String originalClassPath = System.getProperty("java.class.path");
    /**
     * The path to the current project
     */
//    protected static String projectPath;
    /**
     * Maps the class name with its fully qualified name (eg String-> java.lang.String).
     * It only maps classes that are within the current project, since a text parser
     * cannot get a fully qualified name of a class that is imported as a binary file.
     */
    protected static HashMap<String, HashSet<String>> classNameToQualifiedNames = new HashMap<String, HashSet<String>>();
    /**
     * Maps the fully qualified name of a class with the filename of the .java file.
     * It only maps classes that are within the current project.
     */
    protected static HashMap<String, String> qualifiedNameToSourceFilename = new HashMap<String, String>();
    /**
     * 
     */
    protected static HashMap<String, String> qualifiedNameToBinaryFilename = new HashMap<String, String>();
    /**
     * 
     */
    protected static HashMap<String, String> innerClassToBasicClass = new HashMap<String, String>();
    /**
     * 
     */
    protected HashSet<String> componentClasses;
    /**
     * 
     */
    protected HashSet<String> externalClasses;
    /**
     * 
     */
    protected Map<String, CompilationUnit> classToCompilationUnit;
    /**
     * A visitor that extracts the fully qualified name of a class from a .java file
     */
    private static final FullyQualifiedNameVisitor qualifiedVisitor = new FullyQualifiedNameVisitor();
    /**
     * A list of all the instances of ComponentMaker. It is used in order to 
     * update all the ComponentMakers if the current project changes.
     */
    private static ArrayList<ComponentMaker> makers = new ArrayList<ComponentMaker>();
    /**
     * 
     */
    protected static ReuseProject project;
    /**
     * 
     */
    protected static HashSet<String> projectClasses = new HashSet<String>();
    protected static HashSet<String> projectInterfaces = new HashSet<String>();
    protected static final String generatedFolderName = "generated" + File.separator;
    /**
     * The path suffix for the folder that will contain the generated source files
     */
    protected static final String generatedSourceFolderSuffix = "src" + File.separator;
    /**
     * The path suffix for the folder that will contain the binary files after 
     * the code generation is performed
     */
    protected static final String generatedBinaryFolderSuffix = "bin" + File.separator;
    /**
     * The path suffix for the folder that will contain the libraries after 
     * the code generation is performed
     */
    protected static final String generatedLibraryFolderSuffix = "lib" + File.separator;
    /**
     * he path suffix for the folder that will contain the temporary generated 
     * files.
     */
    protected static final String generatedTemporaryFolderSuffix = "tmp" + File.separator;
    ;
    /**
     * A filter that accepts only .java files.
     * @see FileFilter
     */
    protected static final JavaSourceFilter javaSourceFilesFilter = new JavaSourceFilter();
    /**
     * A filter that accepts only .class files.
     * @see FileFilter
     */
    protected static final JavaBinaryFilter javaBinaryFilesFilter = new JavaBinaryFilter();
    /**
     * A filter that accepts only folders.
     * @see FileFilter
     */
    protected static final DirectoryFilter directoryFilter = new DirectoryFilter();
    protected String componentName;
    protected static HashSet<String> missingSources;
    protected static HashSet<String> missingBinaries;

    /**
     * The default constructor for the ComponentMaker. It initializes the class
     * fields and ands the ComponentMaker instance.
     * @param project 
     */
    public ComponentMaker() {

        this.fileOutputStreams = new HashMap<String, FileOutputStream>();
        this.outputStreamWriters = new HashMap<String, OutputStreamWriter>();
        this.generatedFilenames = new HashSet<String>();
        if (!ComponentMaker.makers.add(this)) {
            System.err.println("Caution ComponentMaker was not added to the List. Some features may not finction correctly or raise exceptions.");
        }
        this.classToCompilationUnit = new HashMap<String, CompilationUnit>();
        this.externalClasses = new HashSet<String>();
    }

    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        if (project == null) {
            System.err.println();
            throw new ReuseProjectNotSetException();
        }
        if (componentClasses == null) {
            componentClasses = new HashSet<String>();
        }
        clearComponentInformation();
        populateComponentClasses(componentFiles);

        externalClasses.addAll(ComponentMaker.qualifiedNameToSourceFilename.keySet());
        externalClasses.retainAll(projectClasses);
        externalClasses.removeAll(this.componentClasses);
        this.componentName = componentName;
        project.removeGeneratedComponent(componentName);
    }

    private void clearComponentInformation() {
        externalClasses.clear();
        classToCompilationUnit.clear();
        fileOutputStreams.clear();
        outputStreamWriters.clear();
    }

    /**
     * 
     * @param directoryOrFile 
     */
    private static void populateProjectClasses(File directoryOrFile) {
        if (directoryOrFile.isFile() && javaSourceFilesFilter.accept(directoryOrFile)) {

            try {
                String sourceQualifiedName = qualifiedNameOfRelativeSourceFile(relativePathOfSourceFile(directoryOrFile.getCanonicalPath()));
                if (!qualifiedNameToBinaryFilename.containsKey(sourceQualifiedName)) {
                    qualifiedNameToSourceFilename.put(sourceQualifiedName, relativePathOfSourceFile(directoryOrFile.getCanonicalPath()));
                } else {
                    JavaClass clazz = new ClassParser(ComponentMaker.getJarPath(), sourceFileToBinaryFile(directoryOrFile.getCanonicalPath())).parse();

                    String qualifiedName = clazz.getClassName();

                    String className = qualifiedName.replace(clazz.getPackageName() + ".", "");
                    if (!classNameToQualifiedNames.containsKey(className)) {
                        classNameToQualifiedNames.put(className, new HashSet<String>());
                    }
                    classNameToQualifiedNames.get(className).add(qualifiedName);
                    qualifiedNameToSourceFilename.put(qualifiedName, ComponentMaker.relativePathOfSourceFile(directoryOrFile.getCanonicalPath()));
                    qualifiedNameToBinaryFilename.put(qualifiedName, clazz.getFileName());
                    projectClasses.add(qualifiedName);
                    if(clazz.isInterface())
                        projectInterfaces.add(qualifiedName);
                }
            } catch (IOException ex) {
                Logger.getLogger(ComponentMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (directoryOrFile.isDirectory()) {
            for (File file : directoryOrFile.listFiles()) {
                populateProjectClasses(file);
            }
        }
    }

    private static void populateProjectClasses(String projectPath) {
        File projectDirectory = new File(projectPath);
        if (projectDirectory.exists()) {
            populateProjectClasses(projectDirectory);
        } else {
            System.err.println("Error: Project path \"" + projectPath + "\"does not exist. ");
        }
    }

    private static void populateJarClasses(String jarPath) {
        JarFile jar = null;
        try {
            jar = new JarFile(jarPath);
        } catch (IOException ex) {
            Logger.getLogger(ComponentMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        Enumeration<JarEntry> jarEntries = jar.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry entry = jarEntries.nextElement();
            if (entry.toString().endsWith(".class")) {
                if (!qualifiedNameToBinaryFilename.containsValue(entry.toString())) {
                    try {
                        JavaClass javaClass = new ClassParser(ComponentMaker.getJarPath(), entry.toString()).parse();
//                        if (isAnonymousClass(clazz.getClassName())) {
//                            return;
//                        }
                        ComponentMaker.qualifiedNameToBinaryFilename.put(javaClass.getClassName(), javaClass.getFileName());
                        if (javaClass.getClassName().contains("$")) {
                            innerClassToBasicClass.put(javaClass.getClassName(), javaClass.getClassName().substring(0, javaClass.getClassName().indexOf("$")));
                            ComponentMaker.qualifiedNameToSourceFilename.put(javaClass.getClassName(), binaryFileToSourceFile(javaClass.getFileName()));
                            projectClasses.add(javaClass.getClassName());
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ComponentMaker.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassFormatException ex) {
                        Logger.getLogger(ComponentMaker.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
    }

    private static void checkSourceAndBinaryConsistency() {
        Set<String> binaryClasses = qualifiedNameToBinaryFilename.keySet();
        Set<String> sourceClasses = qualifiedNameToSourceFilename.keySet();

        Set<String> currentSet = new HashSet<String>(binaryClasses);
        currentSet.removeAll(sourceClasses);
        currentSet.removeAll(innerClassToBasicClass.keySet());
        for (String binaryClass : currentSet) {
            System.err.println("The class " + binaryClass + " is contained in the binary files, but was not found in the source files.");
        }
        ComponentMaker.missingSources = new HashSet<String>(currentSet);
        currentSet = new HashSet<String>(sourceClasses);
        currentSet.removeAll(binaryClasses);

        for (String sourceClass : currentSet) {
            System.err.println("The class " + sourceClass + " is contained in the source files, but was not found in the binary files.");
        }
        ComponentMaker.missingBinaries = new HashSet<String>(currentSet);
    }

    //TODO: FIX THIS!
    protected static boolean isAnonymousClass(String className) {
        return className.substring(className.lastIndexOf("$") + 1).matches("^[0-9]+$");//("\\d+");
    }

    protected void addClassToComponent(String className) {
        if (!ComponentMaker.projectClasses.contains(className)) {
            return;
        }
        externalClasses.remove(className);
        componentClasses.add(className);
        if (className.contains("$")) {
            //Substring is used for optimization reasons
            addClassToComponent(className.substring(0, className.indexOf("$")));
        } else {
            for (String clazz : qualifiedNameToBinaryFilename.keySet()) {
                if (clazz.startsWith(className + "$")) {
                    externalClasses.remove(className);
                    componentClasses.add(className);
                }
            }
        }
    }

    protected static String qualifiedNameOfRelativeSourceFile(String path) {
        String sourceQualifiedName = path.replace(File.separator, ".");
        sourceQualifiedName = sourceQualifiedName.substring(0, sourceQualifiedName.length() - 5); //removing the ".java" extension.
        return sourceQualifiedName;
    }

    protected static String relativePathOfSourceFile(String path) {
        String lowerPath = path.toLowerCase();
        String sourcePath = ComponentMaker.getSourcePath().toLowerCase();
        if (!lowerPath.contains(sourcePath)) {
            return path;
        }
        return path.substring(sourcePath.length()).replace("\\", File.separator);
    }

    protected static String sourceFileToBinaryFile(String path) {
        String jarPath = relativePathOfSourceFile(path);
        jarPath = jarPath.substring(0, jarPath.length() - 4) + "class";
        return jarPath;
    }

    protected static String binaryFileToSourceFile(String sourcePath) {

        if (sourcePath.contains("$")) {
            sourcePath = sourcePath.substring(0, sourcePath.indexOf("$")) + ".java";
        } else {
            sourcePath = sourcePath.substring(0, sourcePath.length() - 5) + "java";
        }
        return sourcePath;
    }

    public static boolean isProjectClass(String className) {
        if (className.contains(".")) {
            return ComponentMaker.qualifiedNameToSourceFilename.containsKey(className);
        } else if (className.contains(File.separator)) {
            return ComponentMaker.qualifiedNameToSourceFilename.values().contains(className);
        } else {
            return ComponentMaker.classNameToQualifiedNames.containsKey(className);
        }
    }

    public void setCurrentFile(String filename) {
        this.currentWriter = this.outputStreamWriters.get(filename);
        if (this.currentWriter == null) {
            addFile(filename);
            this.currentWriter = this.outputStreamWriters.get(filename);
        }
        this.currentFilename = filename;
    }

    public void endCurrentFile() {
        if (this.currentWriter == null) {
            return;
        }
        try {
            currentWriter.close();
            fileOutputStreams.get(this.currentFilename).close();
            return;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String getBaseFilepath(String fullpath, String packageOrFilepath) {
        return fullpath.substring(0, fullpath.lastIndexOf(packageOrFilepath)) + ".." + File.separator;
    }

    public static String getClassFilename(String fullpath, String classFilepath) {
        return fullpath.substring(fullpath.lastIndexOf(classFilepath)
                + classFilepath.length() + 1);
    }

    public static String indent(int tabs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            //tab 
            sb.append("\t");
        }
        return sb.toString();
    }

    public void addFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream filestream = new FileOutputStream(file);
            this.fileOutputStreams.put(filename, filestream);
            this.outputStreamWriters.put(filename, new OutputStreamWriter(filestream));
            this.generatedFilenames.add(filename);
            return;
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void out(String outputString) {
        try {
            if (this.currentWriter == null) {
                return;
            }
            this.currentWriter.write(outputString, 0, outputString.length());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void out(String filename, String outputString) {
        try {
            OutputStreamWriter writer = this.outputStreamWriters.get(filename);
            if (writer == null) {
                return;
            }
            writer.write(outputString, 0, outputString.length());
            return;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String getProjectPath() {
        return project.getProjectLocation();
    }

    public static void setProject(ReuseProject project) {
        ComponentMaker.project = project;
        ComponentMaker.clearProjectData();
        String classPath = originalClassPath + File.pathSeparator + ComponentMaker.getJarPath();
        for (String jar : project.getDependenciesJARs()) {
            classPath += File.pathSeparator + ComponentMaker.getLibFolder() + jar;
        }
        System.setProperty("java.class.path", classPath);
        ComponentMaker.populateJarClasses(ComponentMaker.getJarPath());
        ComponentMaker.populateProjectClasses(ComponentMaker.getSourcePath());
        ComponentMaker.checkSourceAndBinaryConsistency();
    }

    @Override
    public Set<String> getGeneratedFiles() {
        return this.generatedFilenames;
    }

    public String getGeneratedFolderPath() {
        return ComponentMaker.getProjectLocation() + generatedFolderName + this.componentName + File.separator;
    }

    public String getGeneratedSourcePath() {
        return getGeneratedFolderPath() + generatedSourceFolderSuffix;
    }

    public String getGeneratedBinaryPath() {
        return getGeneratedFolderPath() + generatedBinaryFolderSuffix;
    }

    public String getGeneratedLibraryPath() {
        return getGeneratedFolderPath() + generatedLibraryFolderSuffix;
    }

    public String getGeneratedTemporaryPath() {
        return getGeneratedFolderPath() + generatedTemporaryFolderSuffix;
    }

    public static String getProjectLocation() {
        return project.getProjectLocation().endsWith(File.separator) ? project.getProjectLocation() : project.getProjectLocation() + File.separator;
    }

    public static String getJarPath() {
        return ComponentMaker.getProjectLocation() + "bin" + File.separator + ComponentMaker.project.getProjectJARFilename();
    }

    public static String getSourcePath() {
        return project.getSrcDir().endsWith(File.separator) ? project.getSrcDir() : project.getSrcDir() + File.separator;
    }

    public static String getLibFolder() {
        return ComponentMaker.getProjectLocation() + "lib" + File.separator;
    }

    protected static void clearProjectData() {
        ComponentMaker.projectClasses.clear();
        ComponentMaker.projectClasses.clear();
        ComponentMaker.qualifiedNameToBinaryFilename.clear();
        ComponentMaker.qualifiedNameToSourceFilename.clear();
        ComponentMaker.classNameToQualifiedNames.clear();
        ComponentMaker.innerClassToBasicClass.clear();
    }

    private void populateComponentClasses(Set<String> componentFilenames) {
        componentClasses.clear();
        Set<String> relativeFilenames = new HashSet<String>();
        for (String filename : componentFilenames) {
            relativeFilenames.add(relativePathOfSourceFile(filename));
        }
        for (String className : projectClasses) {
            if (qualifiedNameToSourceFilename.containsKey(className)) {
                if (relativeFilenames.contains(qualifiedNameToSourceFilename.get(className))) {
                    componentClasses.add(className);
                }
            }
        }
    }

    protected void generateCompilationUnit(CompilationUnit javaSource, boolean forceGeneration) {
        String qualifiedName = javaSource.getPackage().getName().toString();
        for (TypeDeclaration type : javaSource.getTypes()) {
            if (type.getName() != null) {
                qualifiedName += "." + type.getName();
                break;
            }
        }
        if (!classToCompilationUnit.containsKey(qualifiedName)) {
            classToCompilationUnit.put(qualifiedName, javaSource);
        }
        String targetRelativePath = qualifiedName.replace(".", File.separator);
        targetRelativePath = targetRelativePath + ".java";
        String relativeFolder = targetRelativePath.substring(0, targetRelativePath.lastIndexOf(File.separator));
        String targetPath = getGeneratedSourcePath() + targetRelativePath;
        if (!forceGeneration) {
            if (this.generatedFilenames.contains(targetPath)) {
                return;
            }
        }
        File generationFolder = new File(getGeneratedSourcePath() + relativeFolder);
        if (!generationFolder.exists()) {
            generationFolder.mkdirs();
        }
        File generatedFile = new File(targetPath);
        if (generatedFile.exists()) {
            generatedFile.delete();
        }
        try {
            generatedFile.createNewFile();


        } catch (IOException ex) {
            Logger.getLogger(ComponentMaker.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if(forceGeneration){
            if(outputStreamWriters.containsKey(targetPath))
                outputStreamWriters.remove(targetPath);
        }
        setCurrentFile(targetPath);
        out(javaSource.toString());
        endCurrentFile();
        //TODO: CompilationUnit Removal + clearing
    }

    protected CompilationUnit getCompilationUnitOfClass(String className) throws SourceFileNotFoundException {
        if (this.classToCompilationUnit.containsKey(className)) {
            return this.classToCompilationUnit.get(className);
        }

        CompilationUnit classOrInterface = createNewCompilationUnitOfClass(className);
        if (classOrInterface != null) {
            this.classToCompilationUnit.put(className, classOrInterface);
        }
        return classOrInterface;
    }

    protected CompilationUnit createNewCompilationUnitOfClass(String className) throws SourceFileNotFoundException {
        String sourceRelativePath = ComponentMaker.qualifiedNameToSourceFilename.get(className);
        String sourcePath = ComponentMaker.getSourcePath() + sourceRelativePath;
        CompilationUnit classOrInterface = null;
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            throw new SourceFileNotFoundException(className, sourcePath);
        }
        try {
            classOrInterface = JavaParser.parse(new File(sourcePath));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classOrInterface;
    }

    protected void clearComponentFolder() {
        File directory = new File(this.getGeneratedFolderPath());
        if (directory.exists()) {
            deleteFolder(directory);
        }
    }

    private void deleteFolder(File folder) {
        for (String fileOrFolderName : folder.list()) {
            File fileOrFolder = new File(folder.getAbsoluteFile() + File.separator + fileOrFolderName);
            if (fileOrFolder.exists()) {
                if (fileOrFolder.isDirectory()) {
                    deleteFolder(fileOrFolder);
                } else {
                    deleteFile(fileOrFolder);
                }
            }
        }
        folder.delete();
    }

    private void deleteFile(File file) {
        file.delete();
    }

    protected void copyLibraries(GeneratedComponent component) {
        List<String> libs = ComponentMaker.project.getDependenciesJARs();
        for (String lib : libs) {
            if (lib.isEmpty()) {
                continue;
            }
            String relativePath = "lib" + File.separator + lib;
            try {
                String sourceLibaryPath = ComponentMaker.getProjectLocation() + relativePath;
                String targetLibraryPath = ComponentMaker.getProjectLocation() + generatedFolderName + this.componentName + File.separator + relativePath;
                FileUtils.copyFile(new File(sourceLibaryPath), new File(targetLibraryPath));
                component.addLibrary(targetLibraryPath);
            } catch (IOException ex) {
                Logger.getLogger(ComponentMaker.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    protected static void registerComponent(GeneratedComponent component) {
        ComponentMaker.project.addGeneratedComponent(component);
    }

    private void clearInformation() {
        this.fileOutputStreams.clear();
        this.outputStreamWriters.clear();
    }

    public void setGeneratedFilenames(Set<String> generatedFilenames) {
        this.generatedFilenames = new HashSet<String>(generatedFilenames);
    }
    
    @Override
    public void setPolicy(IPolicy policy){
        // Do nothing
    }
}