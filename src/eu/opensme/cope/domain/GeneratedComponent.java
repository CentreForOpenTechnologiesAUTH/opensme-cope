/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.domain;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thanasis
 */
public class GeneratedComponent implements Serializable {

    private String componentName;
    private String componentDirectory;
    private String componentSourceFolder;
    private String componentCentreClass;
    private List<String> componentLibraries;
    private List<String> packagesList;
    private Map<String,String> providedInterfacesMap;
    private Map<String,String> requiredInterfacesMap;
    private Map<String,Set<MethodSignature>> methodsOfInterface;
    
    public GeneratedComponent(String componentName, String componentDirectory, String componentSourceFolder, List<String> componentLibraries, List<String> packagesList, Map<String,String>  providedInterfacesMap, Map<String,String>  requiredInterfacesMap) {
        this.componentName = componentName;
        this.componentDirectory = componentDirectory;
        this.componentSourceFolder = componentSourceFolder;
        this.componentCentreClass = null;
        this.componentLibraries = componentLibraries;
        this.packagesList = packagesList;
        this.providedInterfacesMap = providedInterfacesMap;
        this.requiredInterfacesMap = requiredInterfacesMap;
        this.methodsOfInterface = new HashMap<String, Set<MethodSignature>>();
    }
    
    public GeneratedComponent(String componentName, String componentDirectory, String componentSourceFolder, List<String> componentLibraries, List<String> packagesList) {
        this.componentName = componentName;
        this.componentDirectory = componentDirectory;
        this.componentSourceFolder = componentSourceFolder;
        this.componentCentreClass = null;
        this.componentLibraries = componentLibraries;
        this.packagesList = packagesList;
        this.providedInterfacesMap = new HashMap<String,String>();
        this.requiredInterfacesMap = new HashMap<String,String>();
        this.methodsOfInterface = new HashMap<String, Set<MethodSignature>>();
    }

    public GeneratedComponent(String componentName, String componentDirectory, String componentSourceFolder, List<String> componentLibraries) {
        this.componentName = componentName;
        this.componentDirectory = componentDirectory;
        this.componentSourceFolder = componentSourceFolder;
        this.componentCentreClass = null;
        this.componentLibraries = componentLibraries;
        this.packagesList = new ArrayList<String>();
        this.providedInterfacesMap = new HashMap<String,String>();
        this.requiredInterfacesMap = new HashMap<String,String>();
        this.methodsOfInterface = new HashMap<String, Set<MethodSignature>>();
    }
    
    public GeneratedComponent(String componentName, String componentDirectory, String componentSourceFolder) {
        this.componentName = componentName;
        this.componentDirectory = componentDirectory;
        this.componentSourceFolder = componentSourceFolder;
        this.componentCentreClass = null;
        this.componentLibraries = new ArrayList<String>();
        this.packagesList = new ArrayList<String>();
        this.providedInterfacesMap = new HashMap<String,String>();
        this.requiredInterfacesMap = new HashMap<String,String>();
        this.methodsOfInterface = new HashMap<String, Set<MethodSignature>>();
    }

    public String getComponentName() {
        return this.componentName;
    }

    public String getComponentDirectory() {
        return this.componentDirectory;
    }

    public String getComponentSourceFolder() {
        return this.componentSourceFolder;
    }
    
    public String getComponentCentreClass() {
        return componentCentreClass;
    }
    
    public List<String> getComponentLibraries() {
        return this.componentLibraries;
    }
    
    public List<String> getPackagesList() {
        return this.packagesList;
    }
    
    public Map<String,String> getProvidedInterfacesMap() {
        return this.providedInterfacesMap;
    }
    
    public Map<String,String> getRequiredInterfacesMap() {
        return this.requiredInterfacesMap;
    }
    
    public Set<String> getInterfaces(){
        return this.methodsOfInterface.keySet();
    }
    
    public Set<MethodSignature> getMethodsOfInterface(String interfaceName){
        if(!methodsOfInterface.containsKey(interfaceName))
            return null;
        return methodsOfInterface.get(interfaceName);
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    public void setComponentDirectory(String componentDirectory) {
        this.componentDirectory = componentDirectory;
    }
    
    public void setComponentSourceFolder(String componentSourceFolder) {
        this.componentSourceFolder = componentSourceFolder;
    }
    
    public void setComponentCentreClass(String componentCentreClass) {
        this.componentCentreClass = componentCentreClass;
    }

    public void setComponentLibraries(List<String> componentLibraries) {
        this.componentLibraries = componentLibraries;
    }
    
    public void setPackagesList(List<String> packagesList) {
        this.packagesList = packagesList;
    }
    
    public void setProvidedInterfacesMap(Map<String,String> providedInterfacesMap) {
        this.providedInterfacesMap = new HashMap<String, String>(providedInterfacesMap);
    }
    
    public void setRequiredInterfacesMap(Map<String,String> requiredInterfacesMap) {
        this.requiredInterfacesMap = new HashMap<String, String>(requiredInterfacesMap);
    }

    public void setMethodsOfInterface(Map<String, Set<MethodSignature>> methodsOfInterface) {
        this.methodsOfInterface.clear();
        this.methodsOfInterface.putAll(methodsOfInterface);
    }
    
    public boolean isClassCentric(){
        return this.componentCentreClass == null;
    }
    public void addMethodsToInterface(String interfaceName, Set<MethodSignature> methods){
        if(!methodsOfInterface.containsKey(interfaceName))
            methodsOfInterface.put(interfaceName, new HashSet<MethodSignature>());
        methodsOfInterface.get(interfaceName).addAll(methods);
    }
    public void addLibrary(String libraryPath){
        componentLibraries.add(libraryPath);
    }
    
    public void addPackage(String newPackage){
        if(!packagesList.contains(newPackage)){
            packagesList.add(newPackage);
        }
    }
    
    public void addProvidedInterface(String newInterface,String implementingClass){
        if(!providedInterfacesMap.containsKey(newInterface)){
            providedInterfacesMap.put(newInterface,implementingClass);
        }
    }
    
    public void addRequiredInterface(String newInterface,String originalClass){
        if(!requiredInterfacesMap.containsKey(newInterface)){
            requiredInterfacesMap.put(newInterface, originalClass);
        }
    }

    public void removePackages() {
        packagesList.removeAll(packagesList);
    }
}
