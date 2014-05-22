/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.interfacemaker;

import eu.opensme.cope.componentmakers.ComponentMaker;
import eu.opensme.cope.componentmakers.CompositeComponentMaker;
import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.componentmakers.generic.AntBuildMaker;
import eu.opensme.cope.domain.GeneratedComponent;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class InterfaceMakerWithAntBuild extends CompositeComponentMaker {

    private final InterfaceMaker interfaceMaker;

    public InterfaceMakerWithAntBuild() {
        super();
        this.interfaceMaker = new InterfaceMaker();
        this.addMaker(this.interfaceMaker);
        this.addMaker(new AntBuildMaker());
    }

    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        clearGeneratedFiles();
        super.makeComponent(componentName, componentFiles);
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
}
