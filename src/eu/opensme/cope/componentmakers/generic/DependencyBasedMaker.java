/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.generic;

import eu.opensme.cope.componentmakers.ComponentMaker;
import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class DependencyBasedMaker extends ComponentMaker {
 
    ClassAnalysis classAnalysis;
    private Set<String> generatedFiles;

    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        super.makeComponent(componentName, componentFiles);
        if(componentFiles.size() != 1)
            return;
        String relativeFilepath = componentFiles.iterator().next();
        relativeFilepath = relativeFilepath.replaceFirst(ComponentMaker.getSourcePath(), "");
        makeComponent(componentName ,ClassAnalysis.srcPathToClassAnalysis(relativeFilepath,ComponentMaker.project.getProject() ));
    }
    
    
    
    public void makeComponent(String componentName, ClassAnalysis classAnalysis)
    {
        this.classAnalysis = classAnalysis;
        String componentDestinationPath = ComponentMaker.getProjectLocation() + ComponentMaker.generatedFolderName + componentName + File.separator;
        this.generatedFiles = classAnalysis.extractComponent(ComponentMaker.project.getSrcDir(), componentDestinationPath);
//        GeneratedComponent generatedComponent = new GeneratedComponent(componentName, componentDestinationPath, componentDestinationPath + ComponentMaker.generatedSourceFolderSuffix);
//        copyLibraries(generatedComponent);
//        ComponentMaker.project.addGeneratedComponent(generatedComponent);
    }

    @Override
    public Set<String> getGeneratedFiles() {
        if(this.classAnalysis == null)
        {
            return new HashSet<String>();
        }else
        {
            return this.generatedFiles;
        }
    }   
}
