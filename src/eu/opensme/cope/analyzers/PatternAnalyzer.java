/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers;

import eu.opensme.cope.analyzers.patternanalyzer.PatternList;
import eu.opensme.cope.domain.ReuseProject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import gr.pattdetection.java.pattern.gui.Console;
import gr.xmlparser.XMLParser;

/**
 *
 * @author auth
 */
public class PatternAnalyzer extends Analyzer {

    public PatternAnalyzer(ReuseProject reuseProject) {
        super(reuseProject);
    }

    @Override
    public boolean analyze() {

        String projectJAR = reuseProject.getProjectJARFilename();
        String projectLocation = reuseProject.getProjectLocation();

        String fullPathJar = projectLocation + File.separator + "bin" + File.separator + projectJAR;

        String destDir = fullPathJar + ".binary";
       // String cmd = "";

        boolean success = new File(destDir).mkdirs();
        if (success == true) {
            // this needs to change for linux systems
            Enumeration enumEntries;
            ZipFile zip;
            String fname;
            try {
                zip = new ZipFile(fullPathJar);
                enumEntries = zip.entries();
                while (enumEntries.hasMoreElements()) {
                    ZipEntry zipentry = (ZipEntry) enumEntries.nextElement();
                    fname = zipentry.getName();
                    if (System.getProperty("os.name").contains("Windows")) {
                        fname = fname.replace('/', '\\');
                        fname = destDir + "\\" + fname;
                    } else {
                        fname = destDir + "/" + fname;
                    }
                    if (zipentry.isDirectory()) {
                        System.out.println("Name of Extract directory : " + fname);
                        (new File(fname)).mkdir();
                        continue;
                    }
                    System.out.println("Name of Extract fille : " + fname);
                    InputStream inStream = zip.getInputStream(zipentry);
                    File outStreamAsFile = new File(fname);
                    File parentFolder = outStreamAsFile.getParentFile();
                    parentFolder.mkdirs();
                    OutputStream outStream = new FileOutputStream(fname);
                    byte[] buf = new byte[1024];
                    int l;
                    while ((l = inStream.read(buf)) >= 0) {
                        outStream.write(buf, 0, l);
                    }
                    inStream.close();
                    outStream.close();
                }
                zip.close();
            } catch (IOException ioe) {
                System.out.println("There is an IoException Occured :" + ioe);
                ioe.printStackTrace();
                return false;
            }
            // this needs to change for linux systems
            String input, dest;;
            input = destDir;
            dest = "temp.xml";
            File inputDir = new File(input);
            File outputXML = new File(dest);
            Console console = new Console(inputDir,outputXML);
            System.out.println("Pattern Detection Done");
        } else {
            System.out.println("Folder Already Exists!!!");
            return false;
        }
        return true;
    }

    @Override
    public void storeData() {
        Long projectId = reuseProject.getProject().getProjectid();
        XMLParser xmlp = new XMLParser();
        xmlp.readXML("temp.xml");

        new File("temp.xml").delete();

        PatternList pl = new PatternList("temp.xml.txt");
        pl.writeOnDB(projectId);
        pl.closeConnection();

        new File("temp.xml.txt").delete();
    }
}
