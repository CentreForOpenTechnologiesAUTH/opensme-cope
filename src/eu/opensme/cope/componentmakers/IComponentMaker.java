/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers;

import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public interface IComponentMaker {

    /**
     * This method creates the component according to each component specifications.
     * @param directoryOrFile An absolute path to a Directory or a File.
     */
    //public void makeComponent(File directoryOrFile);
    
    public void makeComponent(String componentName,Set<String> componentFiles) throws ReuseProjectNotSetException,BinaryFileNotFoundException,SourceFileNotFoundException;
    
    public Set<String> getGeneratedFiles();
    
    public void setPolicy(IPolicy policy);
}
