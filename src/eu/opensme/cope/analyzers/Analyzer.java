/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers;

import eu.opensme.cope.domain.ReuseProject;

/**
 *
 * @author auth
 */
public abstract class Analyzer {
    protected ReuseProject reuseProject;

    public Analyzer(ReuseProject reuseProject) {
        this.reuseProject = reuseProject;
    }
    
    public abstract boolean analyze();
    
    public abstract void storeData(); 
    
}
