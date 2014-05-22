/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.cfg;

import eu.opensme.cope.componentvalidator.coverage.pathCoverageAlgorithms.JJPath;
import eu.opensme.cope.componentvalidator.util.Utils;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;

/**
 *
 * @author thanasis
 */
public class CfgMethod {
    private TreeSet<CfgNode> cfg;
    private TreeSet<CfgNode> waitingList;
    private String className;
    private String packageName;
    private String returnType;
    private String methodsSignature;
    private HashMap<Integer,JJPath> pathsToBeCovered;
    private ArrayList<ArrayList<Integer>> executionScenario;
    private String sourceCode;
    private int beginLine;
    private int endLine;
    private double coverage;
    private double statementCoverage;
    private int stmtCount;
    private int coveredStmtCount;
    private String htmlFileName;
    private String methodName;
    private int coveredJJPaths;

    public CfgMethod(String className, String packageName, String returnType, String methodSignature, String sourceCode, int beginLine, int endLine) {
        this.className = className;
        this.packageName = packageName;
        this.returnType = returnType;
        this.methodsSignature = methodSignature;
        this.sourceCode = sourceCode;
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.htmlFileName = packageName + "." + className + "." +methodsSignature.replace(" ", "") + "_" + returnType + ".html";
        if(methodsSignature.contains("(")){
            this.methodName = methodsSignature.substring(0,methodsSignature.indexOf("("));
        }
        cfg = new TreeSet<CfgNode>();
        waitingList = new TreeSet();
        pathsToBeCovered = new HashMap<Integer,JJPath>();
        executionScenario = new ArrayList<ArrayList<Integer>>();
    }
    
    public void setSourceCode(String sourceCode){
        this.sourceCode = sourceCode;
    }
    
    public void setMethodName(String methodName){
        this.methodName = methodName;
    }
    
    public void setCoveredJJPaths(int coveredJJPaths){
        this.coveredJJPaths = coveredJJPaths;
    }
    
    public void setReturnType(String returnType){
        this.returnType = returnType;
    }
    
    public void setHtmlFileName(String htmlFileName){
        this.htmlFileName = htmlFileName.replace(" ", "");
    }
    
    public void setBeginLine(int beginLine){
        this.beginLine = beginLine;
    }
    
    public void setEndLine(int endLine){
        this.endLine = endLine;
    }
    
    public void setPathToBeCovered(HashMap<Integer,JJPath> pathsToBeCovered){
        this.pathsToBeCovered = pathsToBeCovered;
    }
    
    public void addExecutionScenario(ArrayList<Integer> exec){
        this.executionScenario.add(exec);
    }
    
    public void addNodeToCfg(CfgNode node){
        cfg.add(node);
    }
    
    public void addNodeToWaitingList(CfgNode node){
        waitingList.add(node);
    }
    
    public void removeNodeFromCfg(CfgNode node){
        cfg.remove(node);
    }
    
    public void removeNodeFromWaitingList(CfgNode node){
        waitingList.remove(node);
    }
    
    public int getWaitingListSize(){
        return waitingList.size();
    }
    
    public TreeSet<CfgNode> getCfg(){
        return cfg;
    }
    
    public TreeSet<CfgNode> getWaitingList(){
        return waitingList;
    }
    
    public CfgNode getFirstFromWaitingList(){
        return waitingList.pollFirst();
    }
    
    public String getMethodName(){
        return methodName;
    }
    
    public int getBeginLine(){
        return beginLine;
    }
    
    public int getEndLine(){
        return endLine;
    }
    
    public String getReturnType(){
        return returnType;
    }
    
    public String getSourceCode(){
        return sourceCode;
    }
    
    public String getHtmlFileName(){
        return htmlFileName;
    }
    
    public double getCoverage(){
        return coverage;
    }
    
    public int getStmtCount(){
        return stmtCount;
    }
    
    public int getCoveredStmtCount(){
        return coveredStmtCount;
    }
    
    public int getCoveredJJPaths(){
        return coveredJJPaths;
    }
    
    public int getTotalJJPaths(){
        return pathsToBeCovered.size();
    }
    
    public HashMap<Integer, JJPath> getPathsToBeCovered(){
        return pathsToBeCovered;
    }
    
    public double getStmtCoverage(){
        return statementCoverage;
    }
    
    public CfgNode readFirstFromWaitingList(){
        return waitingList.first();
    }
    
    public void combineLists(){
        cfg.addAll(waitingList);
        waitingList = null;
    }
    
    @Override
    public String toString(){
        if(!returnType.equals("")){
            return packageName + "." + className + "." + methodsSignature + ": " + returnType;
        } else {
            return packageName + "." + className + "." + methodsSignature;
        }
    }
    
    public String getClassQualifiedName(){
        return packageName + "." + className;
    }
    
    public void printExecutionScenarios(){
        System.out.println("\n"+this.toString());
        int i = 1;
        for(ArrayList<Integer> list : executionScenario){
            System.out.println("Execution"+" "+i + " steps: "+list.size());
            for(int line : list){
                System.out.println(matchCfgHashCode(line).toString());
            }
            i++;
        }
        
    }
    
    public CfgNode matchCfgHashCode(int line){
        for(CfgNode node : cfg){
            if(node.getCfgHashCode() == line){
                return node;
            }
        }
        return null;
    }
    
    public void computeCoverage(){
        for(ArrayList<Integer> scenario : executionScenario){
            Iterator<Integer> iterator = pathsToBeCovered.keySet().iterator();
            while(iterator.hasNext()){
                JJPath jjpath = pathsToBeCovered.get(iterator.next());
                if(jjpath.isCovered()){
                      continue;
                }               
                int indexOfLastMatch = -1;
                boolean isContained = false;
                for(int i = indexOfLastMatch+1; i<scenario.size(); i++){
                    if(scenario.get(i) == jjpath.getJJPath().get(0)){
                        boolean found = true;
                        indexOfLastMatch = i;
                        for(int k = 0; k < jjpath.getJJPath().size(); k++){
                                if(i+k >= scenario.size() || (scenario.get(i+k) != jjpath.getJJPath().get(k))){
                                    found = false;
                                    break;
                                }
                        }
                        if(found){
                            isContained = true;
                           break; 
                        }
                    }
                }
                jjpath.setCoverage(isContained);
                if(isContained){
                    coveredJJPaths++;
                }
            }
            for(int cfgHashCode : scenario){
                matchCfgHashCode(cfgHashCode).setCovered(true);
            }
        }
        stmtCount = 0;
        coveredStmtCount = 0; 
        for(CfgNode node : cfg){
            if(node.isStatement() && !node.getSource().contains("System.currentTimeMillis();")){
                stmtCount ++;
                if(node.isCovered()){
                    coveredStmtCount ++;
                }
            } else if(node.isStatement() && node.getSource().contains("System.currentTimeMillis();") && cfg.size() == 2){
                stmtCount ++;
                if(node.isCovered()){
                    coveredStmtCount ++;
                }
            }
        }
        if(stmtCount != 0){
            statementCoverage = (coveredStmtCount * 100) / stmtCount;
        }
        if(!pathsToBeCovered.isEmpty()){
            coverage = (coveredJJPaths * 100) / pathsToBeCovered.size();
        }
    }
    
    public String createExecutionScenarioHTML(){
        String BR = System.getProperty("line.separator");
        FileWriter fout = null;
        try {
            String[] sourceTable = sourceCode.split("\n");
            fout = new FileWriter(new File(htmlFileName));
            String html = "<style type=\"text/css\">" + BR
                    + "<!--" + BR
                    + ".HLgreen" + BR
                    + "{" + BR
                    + "        background: #c9ffac;" + BR
                    + "        color: #000000;" + BR
                    + "}" + BR
                    + ".LineNum" + BR
                    + "{" + BR
                    + "        background:#C0C0C0;" + BR
                    + "        border-right: solid 1px #000; " + BR
                    + "        padding-left:5px;" + BR
                    + "        padding-right:10px;" + BR
                    + "}" + BR
                    + "-->" + BR
                    + "</style>" + BR
                    + "<html>" + BR
                    + "<body>" + BR
                    + "<table style=\"border-spacing: 20px 0;\">" + BR;
            int soutBefore = 0;
            for(int i = 0; i < sourceTable.length; i++){
                if(!sourceTable[i].contains("System.currentTimeMillis();")){ 
                    html += "<tr>" + BR;
                    html += "<td class=\"LineNum\">"+(i+1-soutBefore)+"</td>" + BR;
                    if(isLineCovered(i+beginLine)){
                        html += "<td class=\"HLgreen\"><pre><code>"+sourceTable[i]+"</code></pre></td>" + BR;
                    } else {
                        html += "<td><pre><code>"+sourceTable[i]+"</code></pre></td>" + BR;
                    }
                    html += "</tr>" + BR;
                } else {
                    soutBefore++;
                }
            }
            html += "</table>" + BR
                    + "</body>" + BR
                    + "</html>" + BR;
            fout.write(html);
            fout.flush();
            fout.close();
            return htmlFileName;
        } catch (IOException ex) {
            Logger.getLogger(CfgMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public String createHTML(){
        String BR = System.getProperty("line.separator");
        FileWriter fout = null;
        try {
            String[] sourceTable = sourceCode.split("\n");
            fout = new FileWriter(new File(htmlFileName));
            String html = "<style type=\"text/css\">" + BR
                    + "<!--" + BR
                    + ".LineNum" + BR
                    + "{" + BR
                    + "        background:#C0C0C0;" + BR
                    + "        border-right: solid 1px #000; " + BR
                    + "        padding-left:5px;" + BR
                    + "        padding-right:10px;" + BR
                    + "}" + BR
                    + "-->" + BR
                    + "</style>" + BR
                    + "<html>" + BR
                    + "<body>" + BR
                    + "<table style=\"border-spacing: 20px 0;\">" + BR;
            int soutBefore = 0;
            for(int i = 0; i < sourceTable.length; i++){
                if(!sourceTable[i].contains("System.currentTimeMillis();")){ 
                    html += "<tr>" + BR;
                    html += "<td class=\"LineNum\">"+(i+1-soutBefore)+"</td>" + BR;
                    html += "<td><pre><code>"+sourceTable[i]+"</code></pre></td>" + BR;
                    html += "</tr>" + BR;
                } else {
                    soutBefore++;
                }
            }
            html += "</table>" + BR
                    + "</body>" + BR
                    + "</html>" + BR;
            fout.write(html);
            fout.flush();
            fout.close();
            return htmlFileName;
        } catch (IOException ex) {
            Logger.getLogger(CfgMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public boolean isLineCovered(int line){
        for(ArrayList<Integer> execList : executionScenario){
            for(int hashCode : execList){
                if(getLineNumber(hashCode) == line){
                    return true;
                }
            }
        }
        return false;
    }
    
    private int getLineNumber(int hashCode){
        for(CfgNode node : cfg){
            if(node.getCfgHashCode() == hashCode){
                return node.getBeginLine();
            }
        }
        return 0;
    }

    public ArrayList<Integer> getJJPathsIndexes(boolean covered, boolean uncovered) {  
        ArrayList<Integer> jjPathsIndexes = new ArrayList<Integer>();
        Iterator<Integer> iterator = pathsToBeCovered.keySet().iterator();
        while(iterator.hasNext()){
            int pathIndex = iterator.next();
            JJPath path = pathsToBeCovered.get(pathIndex);
            if(!path.isCovered() && uncovered){
                jjPathsIndexes.add(pathIndex);
            } else if(path.isCovered() && covered){
                jjPathsIndexes.add(pathIndex);
            }
        }
        return jjPathsIndexes;
    }
    
    public String createHTMLforPath(int jjPathIndex){
        String BR = System.getProperty("line.separator");
        FileWriter fout = null;
        try {
            String[] sourceTable = sourceCode.split("\n");
            fout = new FileWriter(new File(htmlFileName));
            String html = "<style type=\"text/css\">" + BR
                    + "<!--" + BR
                    + ".HLcolor	" + BR
                    + "{" + BR;
                    if(!pathsToBeCovered.get(jjPathIndex).isCovered()){
                        html += "        background: #ff9b9b;" + BR; //red
                    } else {
                        html += "        background: #c9ffac;" + BR; //green
                    }     
                    html += "        color: #000000;" + BR
                    + "}" + BR
                    + ".LineNum" + BR
                    + "{" + BR
                    + "        background:#C0C0C0;" + BR
                    + "        border-right: solid 1px #000; " + BR
                    + "        padding-left:5px;" + BR
                    + "        padding-right:10px;" + BR
                    + "}" + BR
                    + "-->" + BR
                    + "</style>" + BR
                    + "<html>" + BR
                    + "<body>" + BR
                    + "<table style=\"border-spacing: 20px 0;\">" + BR;
            ArrayList<Integer> HLLines = new ArrayList<Integer>();
            for(int cfgNodeHashCode : pathsToBeCovered.get(jjPathIndex).getJJPath()){
                HLLines.add(matchCfgHashCode(cfgNodeHashCode).getBeginLine()-beginLine);
            }
            int soutBefore = 0;
            for(int i = 0; i < sourceTable.length; i++){
                if(!sourceTable[i].contains("System.currentTimeMillis();")){ 
                    html += "<tr>" + BR;
                    html += "<td class=\"LineNum\">"+(i+1-soutBefore)+"</td>" + BR;
                    if(HLLines.contains(i)){
                        html += "<td class=\"HLcolor\"><pre><code>"+sourceTable[i]+"</code></pre></td>" + BR;
                    } else {
                        html += "<td><pre><code>"+sourceTable[i]+"</code></pre></td>" + BR;
                    }
                    html += "</tr>" + BR;
                } else {
                    soutBefore++;
                }
            }
            html += "</table>" + BR
                    + "</body>" + BR
                    + "</html>" + BR;
            fout.write(html);
            fout.flush();
            fout.close();
            return htmlFileName;
        } catch (IOException ex) {
            Logger.getLogger(CfgMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public JGraph generateCfg(){
        ListenableGraph g = new ListenableDirectedGraph( DefaultEdge.class );
        JGraphModelAdapter m_jgAdapter = new JGraphModelAdapter( g );
        for(CfgNode node : cfg){
            g.addVertex( node.getCfgHashCode() + ". " + node.getSource());
        }
        for(CfgNode node : cfg){
            for(CfgNode conNode : node.getConnectedNodes()){
                g.addEdge(node.getCfgHashCode() + ". " + node.getSource(), conNode.getCfgHashCode() + ". " + conNode.getSource());
            }
            
        }        
        
        int i = 0;
        int y = 0;
        for(CfgNode node : cfg){
            positionVertexAt(m_jgAdapter, node.getCfgHashCode() + ". " + node.getSource(),i%2==0?0:100 ,y);
            i++;
            y += 50;
        }
        return new JGraph( m_jgAdapter );
    }
    
    private void positionVertexAt(JGraphModelAdapter m_jgAdapter, Object vertex, int x, int y) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
        Map attr = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attr);
        Rectangle bounds1 = bounds.getBounds();
        GraphConstants.setBounds(attr, new Rectangle(x, y, cell.toString().length()*7, bounds1.height));
        Map cellAttr = new HashMap();
        cellAttr.put(cell, attr);
        m_jgAdapter.edit(cellAttr, null, null, null);
    }
    
    public String exportCfgToPng(String exportPath){
        Utils.generateImage(generateCfg(), exportPath + "/" + methodsSignature + "_" + returnType, "png");
        return methodsSignature + "_" + returnType + ".png";
    }
}
