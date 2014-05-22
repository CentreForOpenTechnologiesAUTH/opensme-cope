/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.recommenders.entities;

import java.util.Comparator;

/**
 *
 * @author akritiko
 */
public class ClassAnalysisNameComparator implements Comparator {
    
    public int compare(Object ca1, Object ca2){    
 
        String ca1Name = ((ClassAnalysis)ca1).getName();        
        String ca2Name = ((ClassAnalysis)ca2).getName();
       
        return ca1Name.compareTo(ca2Name);
    }
 
}
