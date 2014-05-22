/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author thanasis
 */
public class TraceCodeAdder {
    private HashMap<String,ArrayList<Integer>> tracePositionsMap;

    public TraceCodeAdder(HashMap<String,ArrayList<Integer>> tracePositionsMap) {
        this.tracePositionsMap = tracePositionsMap;
    }

    public void addTraceCode() {
        Iterator<String> iterator = tracePositionsMap.keySet().iterator();
        while (iterator.hasNext()) {
            try {
                int soutCnt = 0;
                String classPath = iterator.next();
                File classFile = new File(classPath);
                FileInputStream fstream = new FileInputStream(classFile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                
                ArrayList<String> array = new ArrayList<String>();
                
                String strLine;
                int line = 1;
                while ((strLine = br.readLine()) != null) {
                    array.add(strLine + "\n");
                    if(tracePositionsMap.get(classPath).contains(line)){
                        soutCnt++;
                        array.add("System.currentTimeMillis();" + "\n"); 
                    }
                    line++;
                }
                in.close();
                FileWriter foutStream = new FileWriter(classFile);
                BufferedWriter out = new BufferedWriter(foutStream);
                for(String newline : array){
                    out.write(newline);
                }
                
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                System.err.println("Error: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    
}
