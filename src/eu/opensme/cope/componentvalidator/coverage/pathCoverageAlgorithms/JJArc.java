/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.pathCoverageAlgorithms;

/**
 *
 * @author thanasis
 */
public class JJArc {
    private int start;
    private int end;
    
    public JJArc(int start, int end){
        this.start = start;
        this.end = end;
    }
    
    public int getStart(){
        return start;
    }
    
    public int getEnd(){
        return end;
    }
}
