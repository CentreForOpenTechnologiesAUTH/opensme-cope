/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.core;

import eu.opensme.cope.componentvalidator.coverage.cfg.CfgHandler;
import eu.opensme.cope.componentvalidator.coverage.cfg.CfgMethod;
import eu.opensme.cope.componentvalidator.coverage.dynamicAnalysis.DynamicAnalisisLogger;
import eu.opensme.cope.componentvalidator.coverage.pathCoverageAlgorithms.Lcsaj;
import eu.opensme.cope.componentvalidator.util.JavaFileReader;
import eu.opensme.cope.componentvalidator.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author thanasis
 */
public class Coverage {
    private String componentSrcPath;
    private String copiedComponentSrc;
    private ArrayList<File> javaFilesList;
    private CfgHandler cfgHandler;
    private String librariesPath;
    private String executionScenario;
    
    public Coverage(String componentSrcPath, String copiedComponentSrc, String logPath, String librariesPath, String executionScenario){
        this.componentSrcPath = componentSrcPath;
        this.librariesPath = librariesPath;
        this.copiedComponentSrc = copiedComponentSrc;
        javaFilesList = new ArrayList<File>();
        this.executionScenario = executionScenario;
    }
    
    public void createGraphComputeJJPaths(){
        copyComponentSrc();
        Utils.formatSource(librariesPath, copiedComponentSrc);
        Utils.formatSource(librariesPath, executionScenario);
        storeAllJavaFiles();
        
        //add trace lines
        cfgHandler = new CfgHandler();
        cfgHandler.parseToFindTraceCodePositions(javaFilesList);
        TraceCodeAdder traceAdder = new TraceCodeAdder(cfgHandler.findWhereToAddTraceLine());
        traceAdder.addTraceCode();
        
        storeAllJavaFiles();
        
        //create cfgs
        cfgHandler = new CfgHandler();
        cfgHandler.createCfgs(javaFilesList); 
        
        computeJJPaths();
    }
    
        private void computeJJPaths(){
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            String classPath = iterator.next();
            for(CfgMethod method : getClassesMethodsMap().get(classPath)){
                if(!method.getCfg().isEmpty()){
                    Lcsaj lcsaj = new Lcsaj();
                    lcsaj.computePaths(method);
                }
            }
        }
    }
    
    private void storeAllJavaFiles(){
        JavaFileReader javaFileReader = new JavaFileReader();
        File srcPath = new File(copiedComponentSrc);
        javaFileReader.Process(srcPath);
        javaFilesList = javaFileReader.getJavaFileList();
        String executionScenarioName = executionScenario;
        if(executionScenario.contains(System.getProperty("file.separator"))){
            executionScenarioName = executionScenario.substring(executionScenario.lastIndexOf(System.getProperty("file.separator"))+1);
        }
        for(File javaFile : javaFilesList){
            if(javaFile.toString().contains(executionScenarioName)){
                if(Utils.getThePackage(javaFile.toString(), true).equals(Utils.getThePackage(executionScenario, true))){
                    javaFilesList.remove(javaFile);
                    break;
                }
            }
        }
    }
    
    private void copyComponentSrc(){
        File srcPath = new File(componentSrcPath);
        File trgPath = new File(copiedComponentSrc);
        if(trgPath.exists()){
            Utils.deleteDirectory(trgPath);
        }
        Utils.copyDirectory(srcPath, trgPath);
    }
    
    
    public void computeCoverageForEveryMethod(){
        Iterator<String> iterator = cfgHandler.getCfgsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : cfgHandler.getCfgsMap().get(iterator.next())){
                method.computeCoverage();
            }
        }
    } 
    
    
    public HashMap<String,ArrayList<CfgMethod>> getClassesMethodsMap(){
        return cfgHandler.getCfgsMap();
    }
    
    public void parseScenarioLogFile(String logPath){
        DynamicAnalisisLogger log = new DynamicAnalisisLogger(logPath);
        log.parseLog();
        log.findTracesPerMethod();
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
                log.setMethodExecutionScenario(method);
            }
        }
    }
    
    public void deleteTmpComponentSourceFolder() {
        File trgPath = new File(copiedComponentSrc);
        if(trgPath.exists()){
            Utils.deleteDirectory(trgPath);
        }
    }
    

    public List<Object[]> getCoverageTable() {
        List<Object[]> coverageList = new ArrayList<Object[]>();
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
               coverageList.add(new Object[]{method.toString(),method.getCoverage(),method.getStmtCoverage()});
            }
        }
        return coverageList;
    }
    
    public List<Object[]> getInterfaceCoverageTable(Set<String> methodsList) {
        List<Object[]> coverageList = new ArrayList<Object[]>();
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
                if(methodsList.contains(method.toString())){
                    coverageList.add(new Object[]{method.toString(),method.getCoverage(),method.getStmtCoverage()});
                }
            }
        }
        return coverageList;
    }
    
    public List<CfgMethod> getMethodsList(){
        List<CfgMethod> methodsList = new ArrayList<CfgMethod>();
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
               methodsList.add(method);
            }
        }
        return methodsList;
    }

    public CfgMethod getSelectedMethod(String selectedValue) {
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
               if(method.toString().equals(selectedValue)){
                   return method;
               }
            }
        }
        return null;
    }
    
    public double[] getComponentCoverage(){
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        int totalStmtCount = 0;
        int totalCoveredStmtCount = 0;
        int totalLcsaJCount = 0;
        int totalCoveredLcsaJCount = 0;
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
                totalStmtCount += method.getStmtCount();
                totalCoveredStmtCount += method.getCoveredStmtCount();
                totalLcsaJCount += method.getTotalJJPaths();
                totalCoveredLcsaJCount += method.getCoveredJJPaths();
            }
        }
        if(totalStmtCount != 0){
            double stmtCov = ( totalCoveredStmtCount * 100 ) / totalStmtCount;
            double lcsajCov = ( totalCoveredLcsaJCount * 100 ) / totalLcsaJCount;
            double[] coveredMetrics = {lcsajCov, stmtCov};
            return coveredMetrics;
        } else {
            return new double[0];
        }
    }
    
     public double[] getInterfaceCoverage(Set<String> methodsList){
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        int totalStmtCount = 0;
        int totalCoveredStmtCount = 0;
        int totalLcsaJCount = 0;
        int totalCoveredLcsaJCount = 0;
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
                if(methodsList.contains(method.toString())){
                    totalStmtCount += method.getStmtCount();
                    totalCoveredStmtCount += method.getCoveredStmtCount();
                    totalLcsaJCount += method.getTotalJJPaths();
                    totalCoveredLcsaJCount += method.getCoveredJJPaths();
                }
            }
        }
        if(totalStmtCount != 0){
            double stmtCov = ( totalCoveredStmtCount * 100 ) / totalStmtCount;
            double lcsajCov = ( totalCoveredLcsaJCount * 100 ) / totalLcsaJCount;
            double[] coveredMetrics = {lcsajCov, stmtCov};
            return coveredMetrics;
        } else {
            return new double[0];
        }
    }

    public int getMethodsEndHashCode(String selectedValue) {
        Iterator<String> iterator = getClassesMethodsMap().keySet().iterator();
        while(iterator.hasNext()){
            for(CfgMethod method : getClassesMethodsMap().get(iterator.next())){
               if(method.toString().equals(selectedValue)){
                   return method.getCfg().last().getCfgHashCode();
               }
            }
        }
        return 0;
    }
}
