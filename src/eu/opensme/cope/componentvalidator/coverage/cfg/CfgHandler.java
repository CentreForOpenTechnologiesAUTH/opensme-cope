/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author thanasis
 */
public class CfgHandler {
    private HashMap<String,ArrayList<CfgMethod>> classesMethodsMap;
    
    public CfgHandler(){
        classesMethodsMap = new HashMap<String, ArrayList<CfgMethod>>();
    }
    
    public HashMap<String,ArrayList<CfgMethod>> getCfgsMap(){
        return classesMethodsMap;
    }
    
    public void createCfgs(ArrayList<File> javaFilesList){
        CfgJavaParser javaParser = new CfgJavaParser();
        javaParser.findMethodsThrowingExceptions(javaFilesList);
        for(File javaFile : javaFilesList){
            ArrayList<CfgMethod> cfgMethodsList = javaParser.parse(javaFile);
            if(!cfgMethodsList.isEmpty()){
                classesMethodsMap.put(cfgMethodsList.get(0).getClassQualifiedName(),cfgMethodsList);
            }
        }
    }
    
    
    public void parseToFindTraceCodePositions(ArrayList<File> javaFilesList){
        CfgJavaParser javaParser = new CfgJavaParser();
        for(File javaFile : javaFilesList){
            ArrayList<CfgMethod> cfgMethodsList = javaParser.parse(javaFile);
            if(!cfgMethodsList.isEmpty()){
                classesMethodsMap.put(javaFile.getAbsolutePath(),javaParser.parseToFindTraceCodePositions(javaFile));
            }
        }
    }
    
    public HashMap<String,ArrayList<Integer>> findWhereToAddTraceLine(){
        HashMap<String,ArrayList<Integer>> tracePositionsMap = new HashMap<String, ArrayList<Integer>>();
        Iterator<String> iterator = classesMethodsMap.keySet().iterator();
        while(iterator.hasNext()){
            ArrayList<Integer> traceLinesList = new ArrayList<Integer>();
            String classPath = iterator.next();
            for(CfgMethod method : classesMethodsMap.get(classPath)){
                if(method.getCfg().size() == 1 && method.getCfg().first().isEndOfMethod()){ //for empty body constructors
                    traceLinesList.add(method.getCfg().first().getBeginLine() -1);
                    continue;
                } else if(method.getCfg().size() == 2 && method.getCfg().first().isReturnStmt()){
                    traceLinesList.add(method.getCfg().first().getBeginLine() -1);
                    continue;
                } else {
                    while(!method.getCfg().isEmpty()){
                        CfgNode lastExpression = null;
                        while(method.getCfg().first().isExpressionStmt()){
                            lastExpression = method.getCfg().pollFirst();
                        }
                        if(lastExpression != null){
                            traceLinesList.add(lastExpression.getEndLine());
                        }
                        method.getCfg().pollFirst();
                    }
                }
            }
            tracePositionsMap.put(classPath, traceLinesList);
        }       
        return tracePositionsMap;
    }
}
