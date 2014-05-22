/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.core.Util;

import java.util.HashMap;

/**
 *
 * @author barius
 */
public class TraceMappings {
    
    private HashMap<String, String> statesHashMap;
    private HashMap<String, String> variablesHashMap;
    
    public TraceMappings() {
	this.statesHashMap = new HashMap<String, String>();
	this.variablesHashMap = new HashMap<String, String>();        
    }

    public HashMap<String, String> getStatesHashMap() {
        return statesHashMap;
    }

    public void setStatesHashMap(HashMap<String, String> statesHashMap) {
        this.statesHashMap = statesHashMap;
    }

    public HashMap<String, String> getVariablesHashMap() {
        return variablesHashMap;
    }

    public void setVariablesHashMap(HashMap<String, String> variablesHashMap) {
        this.variablesHashMap = variablesHashMap;
    }
}
