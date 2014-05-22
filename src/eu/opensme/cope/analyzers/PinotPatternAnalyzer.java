/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers;

import eu.opensme.cope.analyzers.patternanalyzer.PatternList;
import eu.opensme.cope.componentvalidator.util.Utils;
import eu.opensme.cope.domain.ReuseProject;
import gr.pinotParser.PinotParser;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import gr.xmlparser.XMLParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author angor
 */
public class PinotPatternAnalyzer extends Analyzer {

    public PinotPatternAnalyzer(ReuseProject reuseProject) {
        super(reuseProject);
    }

    @Override
    public boolean analyze() {
        String javaHome = System.getProperty("java.home");
        File rtFile = new File(javaHome + File.separator + "lib" + File.separator + "rt.jar");
        if (!rtFile.exists()) {
            System.out.println("File rt.jar does not exists");
            return false;
        }
        String projectSource = reuseProject.getSrcDir();

        ArrayList<String> command = new ArrayList<String>();
        command.add(Utils.getJarFolder() + "lib"
                + File.separator + "pinot" + File.separator + "PREFIX"
                + File.separator + "bin" + File.separator + "pinot.sh");
        command.add(projectSource);
        command.add(rtFile.getAbsolutePath());
        command.add("pinotResults.temp");

        String pinotExecCommand = Utils.getJarFolder() + "lib"
                + File.separator + "pinot" + File.separator + "PREFIX"
                + File.separator + "bin" + File.separator + "pinot.sh"
                + " " + projectSource
                + " " + rtFile.getAbsolutePath()
                + " pinotResults.temp";
        try {
            JOptionPane.showMessageDialog(null, pinotExecCommand, "HELP!!", JOptionPane.WARNING_MESSAGE);
            Process pr = Runtime.getRuntime().exec(command.toArray(new String[0]));
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                System.out.println(line);
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(PinotPatternAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public void storeData() {
        String fullPathJar = reuseProject.getProjectLocation() + File.separator + "bin" + File.separator + "pinotExecuted";
        File pinot = new File(fullPathJar);
        if (pinot.exists()) {
            return;
        } else {
            try {
                boolean pinotRun = new File(fullPathJar).createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(PinotPatternAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        Long projectId = reuseProject.getProject().getProjectid();

        // add code that creates the xml chatzigeorgiou-like file
        // and name it temp.xml

        PinotParser pinParser = new PinotParser();
        try {
            pinParser.parsePinotResultFile(Utils.getJarFolder() + "lib/pinot/PREFIX/bin/pinotResults.temp", "temp.xml", projectId);

        } catch (IOException ex) {
            Logger.getLogger(PinotPatternAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }

        XMLParser xmlp = new XMLParser();
        xmlp.readXML("temp.xml");
        new File("temp.xml").delete();

        PatternList pl = new PatternList("temp.xml.txt");
        pl.writeOnDB(projectId);
        System.out.println("Inserted into DB");
        pl.closeConnection();

        new File("temp.xml.txt").delete();
    }
}
