/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators;

import eu.opensme.cope.domain.ReuseProject;
import com.sun.tools.javadoc.Main;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author george
 */
public class DocumentationGenerator {

    private ReuseProject reuseProject;

    public DocumentationGenerator(ReuseProject reuseProject) {
        this.reuseProject = reuseProject;
    }

    public void generateDocumentation() {
        if (reuseProject != null) {
            List<String> loptions = new ArrayList<String>();
            loptions.add("-private");
            loptions.add("-doclet");
            loptions.add("org.jboss.apiviz.APIviz");
            loptions.add("-docletpath");
            loptions.add("lib/apiviz-1.3.1.GA.jar");
            loptions.add("-sourceclasspath");
            loptions.add(reuseProject.getProjectLocation() + File.separator + "bin"
                    + File.separator + "classes");
            loptions.add("-classpath");
            loptions.add(reuseProject.getSrcDir());
            loptions.add("-d");
            loptions.add(reuseProject.getProjectLocation() + File.separator + "doc");

            String[] rootPackages = (packagesListForProject()).toArray(new String[0]);
            loptions.addAll(Arrays.asList(rootPackages));
            String[] options = (String[]) loptions.toArray(new String[0]);

            //Call apiviz to generate the documentation for the current project
            Main.execute(options);
            System.out.println("Finished");
        }
    }

    public void expandJARFile() {
        //Expand the jar file so that we can generate package dependency
        //diagrams
        this.expandProjectJarFile();
    }

    private List<String> packagesListForProject() {
        List<String> packages = new ArrayList<String>();
        File f = new File(reuseProject.getSrcDir());
        if (f.isDirectory()) {
            traverse(f, packages, "");
        }
        return packages;
    }

    private void traverse(File f, List<String> packages, String prefix) {
        File[] contents = f.listFiles();
        String temp;
        for (File file : contents) {
            if (file.isDirectory()) {
                if (prefix.isEmpty()) {
                    packages.add(file.getName());
                    temp = file.getName();
                } else {
                    packages.add(prefix + "." + file.getName());
                    temp = prefix + "." + file.getName();
                }
                System.out.println("Will traverse "+file.getName());
                traverse(file, packages, temp);
            }
        }
    }

    //JDepend needs this to generate package dependency diagrams
    private void expandProjectJarFile() {
        if (reuseProject == null) {
            System.out.println("Reuse project is null");
            return;
        }
        try {
            String binDir = reuseProject.getProjectLocation()
                    + File.separator
                    + "bin";
            String jarFile =
                    binDir
                    + File.separator + reuseProject.getProjectJARFilename();
            String destDir = binDir + File.separator + "classes";
            File destDirFile = new File(destDir);
            destDirFile.mkdir();
            java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
            java.util.Enumeration entriesEnum = jar.entries();
            while (entriesEnum.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) entriesEnum.nextElement();
                java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
                if (file.isDirectory()) { // if its a directory, create it
                    f.mkdir();
                    continue;
                }
                java.io.InputStream is = jar.getInputStream(file); // get the input stream
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0) {  // write contents of 'is' to 'fos'
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static File getDocForFile(
            String projectLocation,
            String srcDir,
            String absoluteFilename, boolean isDirectory) {

        String filename = projectLocation + File.separator + "doc";
        //remove from the absolute filename the projectLocation part
        String remainder = absoluteFilename.substring(srcDir.length(), absoluteFilename.length());
        if (isDirectory) {
            filename = filename + remainder + File.separator + "package-summary.html";
        } else {
            int pos = remainder.lastIndexOf(".java");
            if (pos != -1) {
                remainder = remainder.substring(0, pos);
                filename = filename + remainder + ".html";
            }
        }
        System.out.println(filename);
        File docFile = new File(filename);
        if (!docFile.exists()) {
            return null;
        }
        return docFile;
    }
}
