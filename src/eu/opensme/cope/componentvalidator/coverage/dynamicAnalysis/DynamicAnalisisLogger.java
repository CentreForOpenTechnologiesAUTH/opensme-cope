/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.dynamicAnalysis;

import eu.opensme.cope.componentvalidator.coverage.cfg.CfgMethod;
import eu.opensme.cope.componentvalidator.coverage.cfg.CfgNode;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thanasis
 */
public class DynamicAnalisisLogger {
    
   private String logPath;
   private ArrayList<String> executionScenarioLog;
   private HashMap<String,ArrayList<ArrayList<Integer>>> tracesPerMethodMap;

    public DynamicAnalisisLogger(String logPath) {
        executionScenarioLog = new ArrayList<String>();
        this.logPath = logPath;
        tracesPerMethodMap = new HashMap<String, ArrayList<ArrayList<Integer>>>();
    }   
   
   public void parseLog(){
       try {
            DataInputStream in = null;

               File traceFile=new File(logPath);
               while(!traceFile.exists()){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DynamicAnalisisLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
               }
                in = new DataInputStream(new FileInputStream(traceFile));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int lastLine = -1;
                while ((strLine = br.readLine()) != null)   {
                    String[] splitedLine = strLine.split("\\*");
                    if((splitedLine[1].equals("entering") || splitedLine[1].equals("exiting")) || Integer.parseInt(splitedLine[0]) != lastLine){
                        executionScenarioLog.add(strLine);
                        lastLine = Integer.parseInt(splitedLine[0]);
                    }
                }
                in.close();
        }  catch (FileNotFoundException ex) {
            Logger.getLogger(DynamicAnalisisLogger.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (IOException ex) {
            Logger.getLogger(DynamicAnalisisLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   
//   public void findTracesPerMethod2(){
//       for(int i = 0; i < executionScenarioLog.size(); i++){
//           if(executionScenarioLog.get(i).contains("*exiting*")){
//               ArrayList<Integer> traces = new ArrayList<Integer>();
//               ArrayList<String> toRemove = new ArrayList<String>();
//               int lastEntered = -1;
//               while(!executionScenarioLog.get(i).contains("*entering*")){
//                   String line = executionScenarioLog.get(i);
//                   if(!line.contains("*exiting*")){
//                       if(lastEntered != Integer.parseInt(line.split("\\*")[0])){
//                           traces.add(Integer.parseInt(line.split("\\*")[0]));
//                           lastEntered = Integer.parseInt(line.split("\\*")[0]);
//                       }
//                   }
//                   toRemove.add(line);
//                   i--;
//               }
//               //traces.add(Integer.parseInt(line.split("\\*")[0]));
//               toRemove.add(executionScenarioLog.get(i));
//               String methodSignature = executionScenarioLog.get(i).split("\\*")[2];
//               Collections.reverse(traces);
//               Collections.reverse(toRemove);
//               if(!tracesPerMethodMap.containsKey(methodSignature)){
//                       ArrayList<ArrayList<Integer>> methodTraces = new ArrayList<ArrayList<Integer>>();
//                       methodTraces.add(traces);
//                       tracesPerMethodMap.put(methodSignature, methodTraces);
//                   } else {
//                       if(!tracesPerMethodMap.get(methodSignature).contains(traces)){
//                           tracesPerMethodMap.get(methodSignature).add(traces);
//                       }
//                   }
//               executionScenarioLog.removeAll(toRemove);
//               i--;
//           }
//       }
//   }
   
   public void findTracesPerMethod(){
       int logsize = -1;
       boolean stop = false;
       int beginLine = 0;
       int endLine = 0;
       while(!executionScenarioLog.isEmpty() && !stop){
           ListIterator<String> iterator = executionScenarioLog.listIterator();
           int indexOfLastEntering = 0;
           int i = 0;
           while(iterator.hasNext()){
                String line = iterator.next();
               if(line.contains("*exiting*")){
                   ArrayList<Integer> traces = new ArrayList<Integer>();
                   int lastEntry = -1;
                   endLine = Integer.parseInt(executionScenarioLog.get(i).split("\\*")[0]);
                   for(int j = indexOfLastEntering+1; j<i; j++){
                       int codeline = Integer.parseInt(executionScenarioLog.get(j).split("\\*")[0]);
                       if(codeline != lastEntry && codeline > beginLine){
                           traces.add(codeline);
                           lastEntry = codeline;
                       }
                   }
                   for(int j = i; j>=indexOfLastEntering; j--){
                       executionScenarioLog.remove(j);
                   }
                   String methodSignature = line.split("\\*")[2];
                   if(!tracesPerMethodMap.containsKey(methodSignature)){
                       ArrayList<ArrayList<Integer>> methodTraces = new ArrayList<ArrayList<Integer>>();
                       if(!methodTraces.contains(traces)){
                           methodTraces.add(traces);
                       }
                       tracesPerMethodMap.put(methodSignature, methodTraces);
                   } else {
                       if(!tracesPerMethodMap.get(methodSignature).contains(traces)){
                           tracesPerMethodMap.get(methodSignature).add(traces);
                       }
                   }
                   break;
               } else if(line.contains("*entering*")){
                   beginLine = Integer.parseInt(executionScenarioLog.get(i).split("\\*")[0]);
                   indexOfLastEntering = i;
               }
               i++;
           }
           if(logsize == executionScenarioLog.size()){
               stop = true;
           }
           logsize = executionScenarioLog.size();
       }
   }
   
   public void setMethodExecutionScenario(CfgMethod cfgMethod){
       String method = cfgMethod.toString();
       ArrayList<CfgNode> cfgArray = new ArrayList<CfgNode>(cfgMethod.getCfg());
       Collections.sort(cfgArray);
       if(tracesPerMethodMap.containsKey(method)){
           for(ArrayList<Integer> list : tracesPerMethodMap.get(method)){
               ArrayList<CfgNode> newTrace = new ArrayList<CfgNode>();
               for(int line : list){
                    CfgNode node = matchLineToNode(line,cfgMethod.getCfg());    
                    if(node != null){
                        if(node.isExpressionStmt() || node.isReturnStmt()){
                            newTrace.addAll(findBeforeBlock(node, cfgMethod.getCfg(), newTrace));
                            newTrace.add(node);
                        }
                    }                   
               }
               ArrayList<Integer> fullFlow = new ArrayList<Integer>();
               ListIterator<CfgNode> iterator = newTrace.listIterator();
                while(iterator.hasNext()){
                   CfgNode node = iterator.next();
                   if(iterator.nextIndex() == 1){
                       fullFlow.add(node.getCfgHashCode());
                   }
                   else{
                       iterator.previous();
                       CfgNode prevNode = iterator.previous();
                       if(prevNode.getConnectedNodes().contains(node)){
                           fullFlow.add(node.getCfgHashCode());
                       }
                       else{
                           fullFlow.addAll(getDirections(prevNode,node, true));
                       }
                       iterator.next();
                       iterator.next();
                   }
                }
                if(!newTrace.isEmpty()){
                    fullFlow.addAll(getDirections(newTrace.get(newTrace.size()-1),cfgArray.get(cfgArray.size()-1), true));
                    if(cfgArray.get(0) != newTrace.get(0)){
                        fullFlow.addAll(0,getDirections(cfgArray.get(0),newTrace.get(0), false));
                    }
                }
                fullFlow = removeDuplicates(fullFlow);
                if(!fullFlow.isEmpty()){
                    cfgMethod.addExecutionScenario(fullFlow);
                }
           }
       }
   }
   
   private ArrayList<Integer> removeDuplicates(ArrayList<Integer> fullFlow){
       ArrayList<Integer> newList = new ArrayList<Integer>();
       for(int element : fullFlow){
           if(newList.isEmpty()){
               newList.add(element);
           }else if(newList.get(newList.size()-1) != element){
               newList.add(element);
           }
       }
       return newList;
   }
   
   public ArrayList<Integer> getDirections(CfgNode start, CfgNode finish, boolean removefirst) {
        Map<CfgNode, Boolean> vis = new HashMap<CfgNode, Boolean>();

        Map<CfgNode, CfgNode> prev = new HashMap<CfgNode, CfgNode>();
        LinkedList directions = new LinkedList();
        Queue<CfgNode> q = new LinkedList();
        CfgNode current = start;
        q.add(current);
        vis.put(current, true);
        while(!q.isEmpty()){
            current = q.remove();
            if (current.equals(finish)){
                break;
            }else{
                for(CfgNode node : current.getConnectedNodes()){
                    if((!vis.containsKey(node)) && ( !node.isExpressionStmt() || node.equals(start) || node.equals(finish) ) ){
                        q.add(node);
                        vis.put(node, true);
                        prev.put(node, current);
                    }
                }
            }
        }
        if (!current.equals(finish)){
            System.out.println("can't reach destination start: " +start.toString()+"  end: "+finish.toString());
        }
        for(CfgNode node = finish; node != null; node = prev.get(node)) {
            directions.add(node.getCfgHashCode());
        }
        Collections.reverse(directions);
        if(removefirst){
            directions.removeFirst();
        }
        return new ArrayList<Integer>(directions);
    }

    private CfgNode matchLineToNode(int line, TreeSet<CfgNode> cfg) {
        ArrayList<CfgNode> matchedNodes = new ArrayList<CfgNode>();
        for(CfgNode node : cfg){
            if(node.getBeginLine() == line){
                matchedNodes.add(node);
            }
        }
        if(matchedNodes.size() == 1){
            return matchedNodes.get(0);
        } else if(matchedNodes.size() > 1){
            for(int i = 0; i < matchedNodes.size(); i++){
                if(matchedNodes.get(i).isStatement()){
                    return matchedNodes.get(i);
                }
            }
            return null;
        } else {
            return null;
        }
    }
    
    private ArrayList<CfgNode> findAfterBlock(CfgNode node, TreeSet<CfgNode> cfg){
        ArrayList<CfgNode> newCfg = new ArrayList<CfgNode>(cfg);
        Collections.sort(newCfg);
        ArrayList<CfgNode> after = new ArrayList<CfgNode>();
        while(node.getConnectedNodes().size() == 1){
                after.add(node.getConnectedNodes().get(0));
                node = node.getConnectedNodes().get(0);
        }
        return after;
    }
    
    private ArrayList<CfgNode> findBeforeBlock(CfgNode node, TreeSet<CfgNode> cfg, ArrayList<CfgNode> newTrace){
        ArrayList<CfgNode> newCfg = new ArrayList<CfgNode>(cfg);
        Collections.sort(newCfg);
        ArrayList<CfgNode> before = new ArrayList<CfgNode>();
        int indexOfNode = newCfg.indexOf(node);
        while(indexOfNode-1 >= 0){
            CfgNode prevNode = newCfg.get(--indexOfNode);
            if(prevNode.getConnectedNodes().size() == 1 && prevNode.getConnectedNodes().get(0).equals(node) && prevNode.isExpressionStmt()) {
                if(!newTrace.isEmpty() && newTrace.get(newTrace.size()-1) == prevNode){
                    break;
                }
                before.add(prevNode);
                node = prevNode;
            }
            else{
                break;
            }
        }
        Collections.reverse(before);
        return before;
    }
}
