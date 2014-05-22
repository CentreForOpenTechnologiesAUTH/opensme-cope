/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.pathCoverageAlgorithms;

import java.util.ArrayList;

/**
 *
 * @author thanasis
 */
public class JJPath {
    private int start;
    private int end;
    private int jump;
    private boolean covered;
    
    public JJPath(int start, int end, int jump){
        this.start = start;
        this.end = end;
        this.jump = jump;
    }
    
    public ArrayList<Integer> getJJPath(){
        ArrayList<Integer> jjpath = new ArrayList<Integer>();
        for(int i = start; i<= end; i++){
            jjpath.add(i);
        }
        jjpath.add(jump);
        return jjpath;
    }
    
    public int getJump(){
        return this.jump;
    }
    
    public int getStart(){
        return this.start;
    }
    
    public int getEnd(){
        return this.end;
    }
    
    public void setCoverage(boolean covered){
        this.covered = covered;
    }

    public boolean isCovered() {
        return covered;
    }
}
