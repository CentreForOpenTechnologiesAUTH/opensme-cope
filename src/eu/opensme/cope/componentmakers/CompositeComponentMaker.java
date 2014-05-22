package eu.opensme.cope.componentmakers;

import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import eu.opensme.cope.domain.GeneratedComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class CompositeComponentMaker extends ComponentMaker {

    private List<IComponentMaker> makers;
    protected HashSet<GeneratedComponent> generatedComponents;

    public CompositeComponentMaker() {
        makers = new ArrayList<IComponentMaker>();
        this.generatedComponents = new HashSet<GeneratedComponent>();
    }

    public CompositeComponentMaker(List<IComponentMaker> makers) {
        this.makers.addAll(makers);
    }

    public void addMaker(IComponentMaker maker) {
        if (maker != null) {
            makers.add(maker);
        }
    }

    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        this.componentName = componentName;
        clearComponentFolder();
        for (IComponentMaker maker : makers) {
            maker.makeComponent(componentName, componentFiles);
        }
    }

    @Override
    public Set<String> getGeneratedFiles() {
        HashSet<String> files = new HashSet<String>();
        for (IComponentMaker maker : makers) {
            files.addAll(maker.getGeneratedFiles());
        }
        return files;
    }

    protected GeneratedComponent createComponent(String componentName) {
        String componentFolder = ComponentMaker.getProjectLocation() + ComponentMaker.generatedFolderName + this.componentName + File.separator;
        String componentSourceFolder = componentFolder + ComponentMaker.generatedSourceFolderSuffix;
        GeneratedComponent component = new GeneratedComponent(componentName, componentFolder, componentSourceFolder);
        this.generatedComponents.add(component);
        return component;
    }

    protected Set<String> getInterfaceFiles(Set<String> interfaceNames) {
        Set<String> filenames = new HashSet<String>();
        for (String interfaceName : interfaceNames) {
            filenames.add(ComponentMaker.qualifiedNameToSourceFilename.get(interfaceName));
        }
        return filenames;
    }
    
    protected  void clearGeneratedFiles() {
        for(IComponentMaker maker: makers)
            maker.getGeneratedFiles().clear();
    }

    public Set<GeneratedComponent> getGenaratedComponents() {
        return this.generatedComponents;
    }

    public void setPolicy(IPolicy policy) {
        for(IComponentMaker maker:makers)
        {
            maker.setPolicy(policy);
        }
    }
}
