/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.core.Util;

import java.util.HashMap;
import eu.opensme.cope.componentvalidator.core.DaikonParser.DaikonModel;
import eu.opensme.cope.componentvalidator.core.GraphModel.Graphs;
import eu.opensme.cope.componentvalidator.core.ProMParser.WorkflowLog;
import eu.opensme.cope.componentvalidator.core.genmodel.genEFSM;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author barius
 */
public class TraceParser {

    private BufferedWriter stream = null;
    private String extractedDir;

    public TraceParser(String extractedDir) {
        super();
        this.extractedDir = extractedDir;
    }

    public TraceMappings parse() throws Exception {
        DaikonModel dm = DaikonParse(extractedDir + "trace.txt", extractedDir +"DaikonTrace.dtrace");
        HashMap<String, String> statesHashMap = dm.getStatesHashMap();
        HashMap<String, String> variablesHashMap = dm.getVariablesHashMap();
        ProMParse(extractedDir + "trace.txt", extractedDir + "PromTrace.xml", statesHashMap, variablesHashMap);

        System.out.println("java -cp " + System.getProperty("java.class.path")
                + " -Xmx256m daikon.Daikon --nohierarchy " + extractedDir + "DaikonTrace.dtrace");
        
        ArrayList<String> command = new ArrayList<String>();
        command.add("java");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("-Xmx256m");
        command.add("daikon.Daikon");
        command.add("--nohierarchy");
        command.add(extractedDir + "DaikonTrace.dtrace");
        
//        Process p = Runtime.getRuntime().exec("java -cp " + System.getProperty("java.class.path")
//                + " -Xmx256m daikon.Daikon --nohierarchy " + extractedDir + "DaikonTrace.dtrace");
        Process p = Runtime.getRuntime().exec(command.toArray(new String[0]));
        int exitCode = p.waitFor();
        if(exitCode != 0){
            System.out.println("Exit error code: " + exitCode);
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        String BR = System.getProperty("line.separator");
        while ((line = input.readLine()) != null) {
            BufferedWriter stream2 = setupStream();
            try {
                stream2.write(line + BR);
                stream2.flush();
            } catch (IOException e) {
            }
        }
        
        genEFSM.setExtracedDir(extractedDir);
       genEFSM genEfsm = new genEFSM(statesHashMap, variablesHashMap);
       
       TraceMappings tm = new TraceMappings();
       tm.setStatesHashMap(statesHashMap);
       tm.setVariablesHashMap(variablesHashMap);
       return tm;
    }

    public static WorkflowLog ProMParse(String input, String output, HashMap<String, String> statesHashMap, HashMap<String, String> variablesHashMap) {
        WorkflowLog wfl = new WorkflowLog();
        wfl = wfl.readFile(wfl, input);
        wfl.ProMExtract(output, statesHashMap, variablesHashMap);

        return wfl;
    }

    public static DaikonModel DaikonParse(String input, String output) {
        Graphs grs = new Graphs();
        grs = grs.readFile(grs, input);
        DaikonModel dm = grs.getDaikonModel();
        dm.DaikonExtract(output);

        return dm;
    }

    private BufferedWriter setupStream() {
        if (stream != null) {
            return stream;
        }

        FileWriter fstream = null;
        try {
            fstream = new FileWriter(extractedDir + "DaikonTrace.txt");
        } catch (IOException e) {
        }

        stream = new BufferedWriter(fstream);
        return stream;
    }
}
