/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.report;

import eu.opensme.cope.componentvalidator.coverage.cfg.CfgMethod;
import eu.opensme.cope.componentvalidator.util.Utils;
import eu.opensme.cope.domain.GeneratedComponent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 *
 * @author thanasis
 */
public class HtmlReport {
    private GeneratedComponent component;
    private List<CfgMethod> cfgMethodsList;
    private List<CfgMethod> selectedMethodsList;
    private Map<String, Set<String>> providedInterfacesMethodsMap;
    private Map<String, double[]> interafacesCoverage;
    private File testsDir;
    private File executionScenarioDir;
    private File testsDocDir;
    private File coverageReportDir;
    private File coverageReport;
    private File methodsCoveredListsDir;
    private File methodsUncoveredListsDir;
    private File coveredPathsDir;
    private File uncoveredPathsDir;
    private File methodsDir;
    private File methodsSrcDir;
    private File methodsExecSrcDir;
    private File cfgsDir;
    private File validationReportDir;
    private HashMap<String, String> stateMappingsMap;
    private HashMap<String, String> variableMappingsMap;
    private String projectName;
    private String executionScenarioName;
    private String executionScenarioDesciption;
    private double lcsajCoverage;
    private double statementCoverage;
    private boolean includeCoverageReport;
    private boolean includeValidationReport;
    private boolean executionScenarioCovered;
    private boolean coveredPaths;
    private boolean uncoveredPaths;
    private boolean cfgs;
    private boolean promXml;
    private boolean stateMappings;
    private boolean varMappings;
    private boolean fsm;
    private boolean modelJUnit;
    private JComponent fsmGraph;
    private List<String> dependsOnList;
    
    
    public HtmlReport(String executionScenarioName, String executionScenarioDescription, 
                                                    double lcsajCoverage, 
                                                    double statementCoverage, 
                                                    String projectName, 
                                                    GeneratedComponent component, 
                                                    List<CfgMethod> cfgMethodsList, 
                                                    List<CfgMethod> selectedMethodsList, 
                                                    String promXmlPath, 
                                                    HashMap<String, String> stateMappingsMap, 
                                                    HashMap<String, String> variableMappingsMap, 
                                                    String modelPath, 
                                                    JComponent fsmGraph, 
                                                    List<String> dependsOnList,
                                                    boolean includeCoverage, 
                                                    boolean includeValidation, 
                                                    boolean executionScenarioCovered, 
                                                    boolean coveredPaths, 
                                                    boolean uncoveredPaths, 
                                                    boolean cfgs, 
                                                    boolean promXml, 
                                                    boolean stateMappings, 
                                                    boolean varMappings, 
                                                    boolean fsm, 
                                                    boolean modelJUnit,
                                                    Map<String, Set<String>> providedInterfacesMethodsMap,
                                                    Map<String, double[]> interafacesCoverage) {
        this.component = component;
        this.cfgMethodsList = cfgMethodsList;
        this.selectedMethodsList = selectedMethodsList;
        this.projectName = projectName;
        File execFile = new File(executionScenarioName);
        if(execFile.exists()){
            executionScenarioName = execFile.getName();
        } else {
            executionScenarioName = "ExecutionScenario.java";
        }
        this.executionScenarioName = executionScenarioName.substring(0, executionScenarioName.indexOf("."));
        this.executionScenarioDesciption = executionScenarioDescription;
        this.lcsajCoverage = lcsajCoverage;
        this.statementCoverage = statementCoverage;
        
        this.includeCoverageReport = includeCoverage;
        this.includeValidationReport = includeValidation;
        this.executionScenarioCovered = executionScenarioCovered;
        this.coveredPaths = coveredPaths;
        this.uncoveredPaths = uncoveredPaths;
        this.cfgs = cfgs;
        this.promXml = promXml;
        this.stateMappings = stateMappings;
        this.varMappings = varMappings;
        this.fsm = fsm;
        this.modelJUnit = modelJUnit;
        this.dependsOnList = dependsOnList;
        this.fsmGraph = fsmGraph;
        
        this.providedInterfacesMethodsMap = providedInterfacesMethodsMap;
        this.interafacesCoverage = interafacesCoverage;
        
        this.testsDir = new File(component.getComponentDirectory()+"/tests/");
        this.executionScenarioDir = new File(testsDir + "/" + this.executionScenarioName);
        this.testsDocDir = new File(executionScenarioDir + "/doc");
        this.coverageReportDir = new File(testsDocDir + "/coverage");
        this.coverageReport = new File(coverageReportDir + "/coverageReports");
        this.methodsCoveredListsDir = new File(coverageReportDir + "/methodsCoveredLists");
        this.methodsUncoveredListsDir = new File(coverageReportDir + "/methodsUncoveredLists");
        this.coveredPathsDir = new File(coverageReportDir + "/covered_jjpaths");
        this.uncoveredPathsDir = new File(coverageReportDir + "/uncovered_jjpaths");
        this.methodsDir = new File(coverageReportDir + "/methods");
        this.methodsSrcDir = new File(coverageReportDir + "/methods_src");
        this.methodsExecSrcDir = new File(coverageReportDir + "/methods_exec");
        this.cfgsDir = new File(coverageReportDir + "/cfgs");
        this.validationReportDir = new File(testsDocDir + "/validation");
        this.stateMappingsMap = stateMappingsMap;
        this.variableMappingsMap = variableMappingsMap;
        makeDirectories(); 
        String executionScenarioPath = executionScenarioDir.toString()+"/"+executionScenarioName;
        Utils.copyDirectory(execFile, new File(executionScenarioPath));
    }
    
    public void generateReport(){
        Velocity.addProperty(Velocity.RESOURCE_LOADER, "file");
        Velocity.setProperty("file.resource.loader.class", org.apache.velocity.runtime.resource.loader.FileResourceLoader.class.getName());
        Velocity.setProperty("file.resource.loader.path", Utils.getJarFolder());
        Velocity.init();
        generateHtml("/htmlReportTemplates/style.vm", testsDocDir.toString()+"/style.css", null);
        generateHtml("/htmlReportTemplates/index.vm", testsDocDir.toString()+"/index.html", null);
        generateHtml("/htmlReportTemplates/mainMenu.vm", testsDocDir.toString()+"/mainMenu.html", null);
        generateMain();
        generateCoverageMenu();
        generateValidationMenu();
    }
    
    private void generateMain(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("projectName", projectName);
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("executionScenarioDescription", executionScenarioDesciption);
        String dependsOn = "";
        if(!dependsOnList.isEmpty()){
            for(int i = 0; i < dependsOnList.size(); i++){
                dependsOn += dependsOnList.get(i);
                if(i < dependsOnList.size()-1){
                    dependsOn += ", ";
                }
                
            }
            parametersMap.put("depend", true);
            parametersMap.put("executionScenarioDependencies", dependsOn);
        } else {
            parametersMap.put("depend", false);
        }
        generateHtml("/htmlReportTemplates/main.vm", testsDocDir.toString()+"/main.html", parametersMap);
    }
    
    private void generateCoverageMenu(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        if(includeCoverageReport){
            parametersMap.put("coveragehtml", includeCoverageReport);
            ArrayList<HashMap<String, Object>> reportsList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> reportsMap = new HashMap<String, Object>();
            reportsMap.put("name", component.getComponentName());
            reportsMap.put("htmlpath", component.getComponentName() + ".html");
            reportsList.add(reportsMap);
            for(String providedInterface : providedInterfacesMethodsMap.keySet()){
                reportsMap = new HashMap<String, Object>();
                reportsMap.put("name", providedInterface);
                reportsMap.put("htmlpath", providedInterface + ".html");
                reportsList.add(reportsMap);
            }
            generateCoverage();
            parametersMap.put("reportslist", reportsList);
        } else {
            parametersMap.put("coveragehtml", includeCoverageReport);
        }
        generateHtml("/htmlReportTemplates/coverage/coverageMenu.vm", coverageReportDir.toString()+"/coverageMenu.html", parametersMap);
    }
    
    private void generateCoverage(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        
        ArrayList<HashMap<String, Object>> reportsList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> reportsMap = new HashMap<String, Object>();
        reportsMap.put("name", component.getComponentName());
        reportsMap.put("htmlpath", component.getComponentName() + ".html");
        reportsMap.put("type", "component");
        reportsList.add(reportsMap);
        generateCoverageReport(true, Collections.EMPTY_SET, component.getComponentName());
        for(String providedInterface : providedInterfacesMethodsMap.keySet()){
            reportsMap = new HashMap<String, Object>();
            reportsMap.put("name", providedInterface);
            reportsMap.put("htmlpath", providedInterface + ".html");
            reportsMap.put("type", "interface");
            reportsList.add(reportsMap);
            generateCoverageReport(false, providedInterfacesMethodsMap.get(providedInterface), providedInterface);
        }
        parametersMap.put("reportslist", reportsList);
        generateHtml("/htmlReportTemplates/coverage/coverage.vm", coverageReportDir.toString()+"/coverage.html", parametersMap);
    }
    
    private void generateCoverageReport(boolean componentCoverage, Set<String> cfgMethods, String coverageReportName){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        if(componentCoverage){
            parametersMap.put("componentLcsaj", lcsajCoverage);
            parametersMap.put("componentStatement", statementCoverage);
        } else {
            double[] coverage = interafacesCoverage.get(coverageReportName);
            parametersMap.put("componentLcsaj", coverage[0]);
            parametersMap.put("componentStatement", coverage[1]);
        }
        ArrayList<HashMap<String, Object>> methodsList = new ArrayList<HashMap<String, Object>>();
        for(CfgMethod method : cfgMethodsList){
            if(cfgMethods.contains(method.toString()) || componentCoverage){
                HashMap<String, Object> methodsMap = new HashMap<String, Object>();
                methodsMap.put("signature", method.toString());
                methodsMap.put("lcsaj", method.getCoverage());
                methodsMap.put("statement", method.getStmtCoverage());
                methodsMap.put("htmlname", method.getHtmlFileName());
                if( (executionScenarioCovered || coveredPaths || uncoveredPaths || cfgs) && selectedMethodsList.contains(method)){
                    methodsMap.put("methodDetails", true);
                    if(componentCoverage){
                        generateMethod(method);
                    }
                } else {
                    methodsMap.put("methodDetails", false); 
                }
                methodsList.add(methodsMap);
            }
        }
        parametersMap.put("methodsList", methodsList);
        generateHtml("/htmlReportTemplates/coverage/coverageReports/coverageReport.vm", coverageReport.toString() + "/" + coverageReportName + ".html", parametersMap);
    }
    
    private void generateMethod(CfgMethod method){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("methodSignature", method.toString());
        parametersMap.put("lcsajCoverage", method.getCoverage());
        parametersMap.put("statementCoverage", method.getStmtCoverage());
        parametersMap.put("sourceCodeHtmlName", method.getHtmlFileName()+"Src.html");
        parametersMap.put("methodsExecHtmlName", method.getHtmlFileName()+"Exec.html");
        parametersMap.put("execCoveredLines", executionScenarioCovered);
        parametersMap.put("coveredListHtmlName", method.getHtmlFileName()+"CoveredList.html");
        parametersMap.put("uncoveredListHtmlName", method.getHtmlFileName()+"UncoveredList.html");
        parametersMap.put("generatecfg", cfgs);
        if(cfgs){
            parametersMap.put("cfgImageFile", method.exportCfgToPng(cfgsDir.toString()));
        }
        generateMethodsSrc(method);
        generateMethodsCoveredUnCoveredList(method);
        if(executionScenarioCovered){
            generateExecScenarioCoveredLines(method);
        }
        generateHtml("/htmlReportTemplates/coverage/methods/coverageMethod.vm", methodsDir.toString()+"/"+method.getHtmlFileName(), parametersMap);
    }
    
    private void generateMethodsCoveredUnCoveredList(CfgMethod method){
        HashMap<String,Object> coveredParametersMap = new HashMap<String, Object>();
        HashMap<String,Object> uncoveredParametersMap = new HashMap<String, Object>();
        ArrayList<HashMap<String, Object>> coveredList = new ArrayList<HashMap<String, Object>>();
        ArrayList<HashMap<String, Object>> uncoveredList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> coveredUncoveredMap;
        
        Iterator<Integer> iterator = method.getPathsToBeCovered().keySet().iterator();
        while(iterator.hasNext()){
            int key = iterator.next();
            if(method.getPathsToBeCovered().get(key).isCovered()){
                coveredUncoveredMap = new HashMap<String, Object>();
                coveredUncoveredMap.put("pathName", "JJPath_" + key);
                coveredUncoveredMap.put("coveredPaths", coveredPaths);
                coveredUncoveredMap.put("htmlName", method.getHtmlFileName() + "CoveredPath" + key + ".html");
                coveredList.add(coveredUncoveredMap);
                if(coveredPaths){
                    generateCovered(method,key);
                }
            } else {
                coveredUncoveredMap = new HashMap<String, Object>();
                coveredUncoveredMap.put("htmlName", method.getHtmlFileName() + "UncoveredPath" + key + ".html");
                coveredUncoveredMap.put("pathName", "JJPath_" + key);
                coveredUncoveredMap.put("uncoveredPaths", uncoveredPaths);
                uncoveredList.add(coveredUncoveredMap);
                if(uncoveredPaths){
                    generateUncovered(method,key);
                }
            }
        }
        coveredParametersMap.put("coveredList", coveredList);
        uncoveredParametersMap.put("uncoveredList", uncoveredList);
        generateHtml("/htmlReportTemplates/coverage/methodsCoveredLists/coveredList.vm", methodsCoveredListsDir.toString()+"/"+method.getHtmlFileName()+"CoveredList.html", coveredParametersMap);
        generateHtml("/htmlReportTemplates/coverage/methodsUncoveredLists/uncoveredList.vm", methodsUncoveredListsDir.toString()+"/"+method.getHtmlFileName()+"UncoveredList.html", uncoveredParametersMap);
    }
    
    private void generateMethodsSrc(CfgMethod method){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        String[] methodSource = method.getSourceCode().replace("System.currentTimeMillis();\n", "").split("\n");
        parametersMap.put("methodsSourceList", methodSource); 
        generateHtml("/htmlReportTemplates/coverage/methods_src/sourceCode.vm", methodsSrcDir.toString()+"/"+method.getHtmlFileName()+"Src.html", parametersMap);
    }
    
    private void generateExecScenarioCoveredLines(CfgMethod method){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        String[] sourceTable = method.getSourceCode().split("\n");
        int soutBefore = 0;
        ArrayList<HashMap<String,Object>> methodsSourceList = new ArrayList<HashMap<String,Object>>();
        for(int i = 0; i < sourceTable.length; i++){
            HashMap<String,Object> execSrcMap = new HashMap<String, Object>();
            if(!sourceTable[i].contains("System.currentTimeMillis();")){ 
                if(method.isLineCovered(i+method.getBeginLine())){
                    execSrcMap.put("covered",true);
                    execSrcMap.put("source", sourceTable[i]);
                } else {
                    execSrcMap.put("covered",false);
                    execSrcMap.put("source", sourceTable[i]);
                }
                methodsSourceList.add(execSrcMap);
            } else {
                soutBefore++;
            }
        }
        parametersMap.put("methodsSourceList", methodsSourceList);
        generateHtml("/htmlReportTemplates/coverage/methods_exec/exec_covered.vm", methodsExecSrcDir.toString()+"/"+method.getHtmlFileName()+"Exec.html", parametersMap);
    }
    
    private void generateCovered(CfgMethod method, int key){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        String[] sourceTable = method.getSourceCode().split("\n");
        ArrayList<Integer> HLLines = new ArrayList<Integer>();
        for(int cfgNodeHashCode : method.getPathsToBeCovered().get(key).getJJPath()){
            HLLines.add(method.matchCfgHashCode(cfgNodeHashCode).getBeginLine() - method.getBeginLine());
        }
        int soutBefore = 0;
        ArrayList<HashMap<String,Object>> coveredSourceList = new ArrayList<HashMap<String,Object>>();
        for(int i = 0; i < sourceTable.length; i++){
            HashMap<String,Object> coveredSrcMap = new HashMap<String, Object>();
            if(!sourceTable[i].contains("System.currentTimeMillis();")){ 
                if(HLLines.contains(i)){
                    coveredSrcMap.put("covered",true);
                    coveredSrcMap.put("source", sourceTable[i]);
                } else {
                    coveredSrcMap.put("covered",false);
                    coveredSrcMap.put("source", sourceTable[i]);
                }
                coveredSourceList.add(coveredSrcMap);
            } else {
                soutBefore++;
            }
        }
        parametersMap.put("methodsSourceList", coveredSourceList);
        generateHtml("/htmlReportTemplates/coverage/covered_jjpaths/covered.vm", coveredPathsDir.toString()+"/"+method.getHtmlFileName() + "CoveredPath" + key + ".html", parametersMap);
    }
    
    private void generateUncovered(CfgMethod method, int key){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        String[] sourceTable = method.getSourceCode().split("\n");
        ArrayList<Integer> HLLines = new ArrayList<Integer>();
        for(int cfgNodeHashCode : method.getPathsToBeCovered().get(key).getJJPath()){
            HLLines.add(method.matchCfgHashCode(cfgNodeHashCode).getBeginLine() - method.getBeginLine());
        }
        int soutBefore = 0;
        ArrayList<HashMap<String,Object>> uncoveredSourceList = new ArrayList<HashMap<String,Object>>();
        for(int i = 0; i < sourceTable.length; i++){
            HashMap<String,Object> uncoveredSrcMap = new HashMap<String, Object>();
            if(!sourceTable[i].contains("System.currentTimeMillis();")){ 
                if(HLLines.contains(i)){
                    uncoveredSrcMap.put("uncovered",true);
                    uncoveredSrcMap.put("source", sourceTable[i]);
                } else {
                    uncoveredSrcMap.put("uncovered",false);
                    uncoveredSrcMap.put("source", sourceTable[i]);
                }
                uncoveredSourceList.add(uncoveredSrcMap);
            } else {
                soutBefore++;
            }
        }
        parametersMap.put("methodsSourceList", uncoveredSourceList);
        generateHtml("/htmlReportTemplates/coverage/uncovered_jjpaths/uncovered.vm", uncoveredPathsDir.toString()+"/"+method.getHtmlFileName() + "UncoveredPath" + key + ".html", parametersMap);
    }
    
    private void generateHtml(String templateFile, String generatePath, HashMap<String, Object> parametersMap){
        FileWriter fw = null;
        try {
            VelocityContext context = new VelocityContext();
            if(parametersMap != null){
                Iterator<String> iterator = parametersMap.keySet().iterator();
                while(iterator.hasNext()){
                    String key = iterator.next();
                    context.put(key,parametersMap.get(key));
                }
            }
            Template template =  null;
            try 
            {
                template = Velocity.getTemplate(templateFile);
            }
            catch( ResourceNotFoundException rnfe )
            {
                System.out.println("Example : error : cannot find template " + templateFile );
            }
            catch( ParseErrorException pee )
            {
                System.out.println("Example : Syntax error in template " + templateFile + ":" + pee );
            }
            StringWriter sw = new StringWriter();
            template.merge( context, sw );
            fw = new FileWriter(new File(generatePath));
            fw.write(sw.toString());
        } catch (IOException ex) {
            Logger.getLogger(HtmlReport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(HtmlReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void generateValidationMenu(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        if(includeValidationReport){
            parametersMap.put("includeValidation", includeValidationReport);
            parametersMap.put("promxml", promXml);
            parametersMap.put("statemappings", stateMappings);
            parametersMap.put("varmappings", varMappings);
            parametersMap.put("spaghetti", fsm);
            parametersMap.put("modelJunit", modelJUnit);
            generateValidation();
        } else {
            parametersMap.put("includeValidation", includeValidationReport);
        }
        generateHtml("/htmlReportTemplates/validation/validationMenu.vm", validationReportDir.toString()+"/validationMenu.html", parametersMap);
    }
    
    private void generateValidation(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        if(promXml){
            parametersMap.put("promxml", promXml);
            generatePromXml();
        }
        if(stateMappings){
            parametersMap.put("statemappings", stateMappings);
            generateStateMappings();
        }
        if(varMappings){
            parametersMap.put("varmappings", varMappings);
            generateVarMappings();
        }
        if(fsm){
            parametersMap.put("spaghetti", fsm);
            generateFsm();
        }
        if(modelJUnit){
            parametersMap.put("modelJunit", modelJUnit);
            generateModelJunit();
        }
        generateHtml("/htmlReportTemplates/validation/validation.vm", validationReportDir.toString()+"/validation.html", parametersMap);
    }
    
    private void generatePromXml(){
        String promTraceXml = "";
        try {
            FileInputStream fstream = new FileInputStream(executionScenarioDir.toString() + "/PromTrace.xml");
            
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                promTraceXml = promTraceXml.concat(strLine + "\n");
            }
            promTraceXml = promTraceXml.replaceAll("<", "&lt;");
            promTraceXml = promTraceXml.replaceAll(">", "&gt;");
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("xmlFilePath", executionScenarioName + "/PromTrace.xml");
        parametersMap.put("xmltrace", promTraceXml);
        generateHtml("/htmlReportTemplates/validation/viewXML.vm", validationReportDir.toString()+"/viewXML.html", parametersMap);
    }
    
    private void generateStateMappings(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("xmlFilePath", executionScenarioName + "/PromTrace.xml");
        ArrayList<HashMap<String, String>> statesList = new ArrayList<HashMap<String, String>>();
        
        Iterator<String> iterator = stateMappingsMap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            HashMap<String, String> statesMap = new HashMap<String, String>();
            statesMap.put("state", stateMappingsMap.get(key));
            statesMap.put("function", key);
            statesList.add(statesMap);
        }
        parametersMap.put("statesList", statesList);
        generateHtml("/htmlReportTemplates/validation/stateMappings.vm", validationReportDir.toString()+"/stateMappings.html", parametersMap);
    }
    
    private void generateVarMappings(){
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("xmlFilePath", executionScenarioName + "/PromTrace.xml");
        ArrayList<HashMap<String, String>> varList = new ArrayList<HashMap<String, String>>();
        
        Iterator<String> iterator = variableMappingsMap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            HashMap<String, String> varMap = new HashMap<String, String>();
            varMap.put("var", variableMappingsMap.get(key));
            varMap.put("name", key);
            varList.add(varMap);
        }
        parametersMap.put("varList", varList);
        generateHtml("/htmlReportTemplates/validation/varMappings.vm", validationReportDir.toString()+"/varMappings.html", parametersMap);
    }
    
    private void generateFsm(){           
        Utils.generateImage(fsmGraph, validationReportDir.toString() + "/fsm", "png");
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("xmlFilePath", executionScenarioName + "/PromTrace.xml");
        generateHtml("/htmlReportTemplates/validation/spaghetti.vm", validationReportDir.toString()+"/spaghetti.html", parametersMap);
    
    }
    
    private void  generateModelJunit(){
        String javaModel = "";
        try {
            FileInputStream fstream = new FileInputStream(executionScenarioDir.toString() + "/test.java");
            
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                javaModel = javaModel.concat(strLine + "\n");
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        HashMap<String,Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("componentName", component.getComponentName());
        parametersMap.put("executionScenarioName", executionScenarioName);
        parametersMap.put("modelFilePath", executionScenarioName + "/test.java");
        parametersMap.put("javamodel", javaModel);
        generateHtml("/htmlReportTemplates/validation/model.vm", validationReportDir.toString()+"/model.html", parametersMap);
    }
    
    private void makeDirectories(){
        if(!testsDir.exists()){
            testsDir.mkdir();
        }
        if(!executionScenarioDir.exists()){
            executionScenarioDir.mkdir();
        }
        if(!testsDocDir.exists()){
            testsDocDir.mkdir();
        }
        if(!coverageReportDir.exists()){
            coverageReportDir.mkdir();
        }
        if(!coverageReport.exists()){
            coverageReport.mkdir();
        }
        if(!methodsCoveredListsDir.exists()){
            methodsCoveredListsDir.mkdir();
        }
        if(!methodsUncoveredListsDir.exists()){
            methodsUncoveredListsDir.mkdir();
        }
        if(!coveredPathsDir.exists()){
            coveredPathsDir.mkdir();
        }
        if(!uncoveredPathsDir.exists()){
            uncoveredPathsDir.mkdir();
        }
        if(!methodsDir.exists()){
            methodsDir.mkdir();
        }
        if(!methodsSrcDir.exists()){
            methodsSrcDir.mkdir();
        }
        if(!methodsExecSrcDir.exists()){
            methodsExecSrcDir.mkdir();
        }
        if(!cfgsDir.exists()){
            cfgsDir.mkdir();
        }
        if(!validationReportDir.exists()){
            validationReportDir.mkdir();
        }
    }
}
