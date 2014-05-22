/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators.lucene;

/**
 *
 * @author econst
 */
public class Result {
    private String fullClassName;
    private float score;
    
    public Result(){
        
    }
    
    public Result(String full, float sc){
        fullClassName = full;
        score = sc;
    }
    
    public String getFullClassName() {
        return fullClassName;
    }

    public float getScore() {
        return score;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public void setScore(float score) {
        this.score = score;
    }    
}
