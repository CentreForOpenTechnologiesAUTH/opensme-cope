/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.generic;

import eu.opensme.cope.componentmakers.ComponentMaker;
import eu.opensme.cope.componentmakers.CompositeComponentMaker;
import eu.opensme.cope.componentmakers.IPolicy;
import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.componentmakers.interfacemaker.InterfaceMaker;
import eu.opensme.cope.domain.GeneratedComponent;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class DependencyBasedMakerWithAntBuild extends CompositeComponentMaker {

    private final InterfaceMaker interfaceMaker;
    private final DependencyBasedMaker dependencyMaker;
    private final AntBuildMaker antMaker;
    private Set<String> generatedFiles;

    public DependencyBasedMakerWithAntBuild() {
        super();
        this.interfaceMaker = new InterfaceMaker();
        this.dependencyMaker = new DependencyBasedMaker();
        this.antMaker = new AntBuildMaker();
//        this.addMaker(this.dependencyMaker);
//        this.addMaker(this.interfaceMaker);
//        this.addMaker(this.antMaker);
    }

    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        super.makeComponent(componentName, componentFiles);
        clearComponentFolder();
        clearGeneratedFiles();
        this.dependencyMaker.makeComponent(componentName, componentFiles);
        this.interfaceMaker.makeComponent(componentName, generatedToInitialSourceFiles(dependencyMaker.getGeneratedFiles()));
        if (interfaceMaker.getPolicy().isGenerateSelectedClass()) {
            for (String classFilename : componentFiles) {
                this.interfaceMaker.generateProvidedInterface(classFilename);
            }
        }
        this.antMaker.makeComponent(componentName, dependencyMaker.getGeneratedFiles());
        generatedFiles = this.interfaceMaker.getGeneratedFiles();
        generatedFiles.addAll(antMaker.getGeneratedFiles());
        this.setGeneratedFilenames(generatedFiles);

        GeneratedComponent component = createComponent(componentName);
        copyLibraries(component);
        registerComponent(component);
    }

    @Override
    protected GeneratedComponent createComponent(String componentName) {
        GeneratedComponent component = super.createComponent(componentName);
        component.setProvidedInterfacesMap(interfaceMaker.getProvidedInterfaces());
        component.setRequiredInterfacesMap(interfaceMaker.getRequiredInterfaces());
        component.setMethodsOfInterface(interfaceMaker.getMethodsOfInterface());
        return component;
    }

    public Set<String> getProvidedInterfacesFiles() {
        return getInterfaceFiles(interfaceMaker.getProvidedInterfaces().keySet());
    }

    public Set<String> getRequiredInterfacesFiles() {
        return getInterfaceFiles(interfaceMaker.getRequiredInterfaces().keySet());
    }

    public Set<String> generatedToInitialSourceFiles(Set<String> filenames) {
        Set<String> newFilenames = new HashSet<String>();
        for (String filename : filenames) {
            newFilenames.add(filename.replace(this.getGeneratedSourcePath(), ComponentMaker.getSourcePath()));
        }
        return newFilenames;
    }

    @Override
    public Set<String> getGeneratedFiles() {
        return this.generatedFiles;
    }

    @Override
    public void setPolicy(IPolicy policy) {
        super.setPolicy(policy);
        this.dependencyMaker.setPolicy(policy);
        this.interfaceMaker.setPolicy(policy);
        this.antMaker.setPolicy(policy);
    }

    protected  void clearGeneratedFiles() {
        this.dependencyMaker.getGeneratedFiles().clear();
        this.interfaceMaker.getGeneratedFiles().clear();
        this.antMaker.getGeneratedFiles().clear();
    }
}
