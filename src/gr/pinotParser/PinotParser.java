package gr.pinotParser;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author angor
 */
public class PinotParser {

    public static void readPatternsFromFile(String path, PatternList patterns, long projectID) throws IOException {
        System.out.println("\t\t..reading patterns from file " + path);
        BufferedReader reader = new BufferedReader(new FileReader(path));
        ArrayList<String> patternBlock = new ArrayList<String>();
        String currentLine = null;
        boolean flag = false;

        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.contains(" Pattern.")) {
                flag = true;
                patternBlock = new ArrayList<String>();
            }
            patternBlock.add(currentLine);
            if (currentLine.equals("") && flag) {
                patterns.addPatternBlock(patternBlock, projectID);
                flag = false;
            }
        }
        reader.close();
    }

    public void parsePinotResultFile(String pinotResultsPath, String projectFolderPath, long projectID) throws IOException {
        File pinotFile = new File(pinotResultsPath);
        PatternList patterns = new PatternList();
        readPatternsFromFile(pinotFile.getAbsolutePath(), patterns, projectID);
        String xmlFile = projectFolderPath;
        patterns.writeXmlFile(xmlFile);
    }

    public static void main(String args[]) {
    }
}
