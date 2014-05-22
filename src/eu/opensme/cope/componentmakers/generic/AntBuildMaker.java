/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.generic;

import eu.opensme.cope.componentmakers.ComponentMaker;
import eu.opensme.cope.componentmakers.common.BinaryFileNotFoundException;
import eu.opensme.cope.componentmakers.common.ReuseProjectNotSetException;
import eu.opensme.cope.componentmakers.common.SourceFileNotFoundException;
import java.io.File;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class AntBuildMaker extends ComponentMaker {

    private static final String antSuffix = "build.xml";
    private static final String newLine = "\n";
    private boolean hasLibs = !ComponentMaker.project.getDependenciesJARs().isEmpty();
    private String antFile;

    @Override
    public void makeComponent(String componentName, Set<String> componentFiles) throws ReuseProjectNotSetException, BinaryFileNotFoundException, SourceFileNotFoundException {
        super.makeComponent(componentName, componentFiles);
        this.antFile = ComponentMaker.getProjectLocation() + generatedFolderName + componentName + File.separator + antSuffix;
        generateAntFile();
    }

    private void generateAntFile() {
        super.setCurrentFile(antFile);
        out(generateXMLContents());
        super.endCurrentFile();
    }

    private String generateXMLContents() {
        StringBuilder xml = new StringBuilder();
        xml.append("<project name=\"").append(super.componentName).append("\" default=\"jar\" basedir=\".\">").append(newLine);
        xml.append(generateSubElements());
        xml.append("</project>");
        return xml.toString();
    }

    private String generateSubElements() {
        StringBuilder subElements = new StringBuilder();
        subElements.append(generateProperties()).append(newLine);
        subElements.append(generateTargets()).append(newLine);
        return subElements.toString();
    }

    private String generateProperties() {
        StringBuilder properties = new StringBuilder();
        properties.append(indent(1)).append("<property name=\"dir.src\" value=\"").append(ComponentMaker.generatedSourceFolderSuffix).append("\"/>").append(newLine);
        properties.append(indent(1)).append("<property name=\"dir.build\" value=\"").append(ComponentMaker.generatedBinaryFolderSuffix).append("\"/>").append(newLine);
        if (hasLibs) {
            properties.append(indent(1)).append("<property name=\"dir.lib\" value=\"").append(ComponentMaker.generatedLibraryFolderSuffix).append("\"/>").append(newLine);
        }
        properties.append(indent(1)).append("<property name=\"dir.jar\" value=\".\"/>").append(newLine);
        properties.append(indent(1)).append("<property name=\"java.dir\" value=\"").append(System.getProperty("java.home")).append(File.separator).append("lib").append(File.separator).append("\"/>").append(newLine);

        return properties.toString();
    }

    private String generateTargets() {
        StringBuilder targets = new StringBuilder();
        targets.append(generateCleanTarget());
        targets.append(generatePrepareTarget());
        targets.append(generateCompileTarget());
        targets.append(generateJarTarget());
        return targets.toString();
    }

    private String generateCleanTarget() {
        StringBuilder target = new StringBuilder();
        target.append(indent(1)).append("<target name=\"clean\" description=\"Removing the all generated files.\">").append(newLine);
        target.append(indent(2)).append("<delete dir=\"${dir.build}\"/>").append(newLine);
        target.append(indent(2)).append("<delete dir=\"${dir.jar}\"/>").append(newLine);
        target.append(indent(1)).append("</target>").append(newLine);
        return target.toString();
    }

    private String generatePrepareTarget() {
        StringBuilder target = new StringBuilder();
        target.append(indent(1)).append("<target name=\"prepare\" depends=\"clean\">").append(newLine);
        target.append(indent(2)).append("<mkdir dir=\"${dir.build}\"/>").append(newLine);
        target.append(indent(1)).append("</target>").append(newLine);
        return target.toString();
    }

    private String generateCompileTarget() {
        StringBuilder target = new StringBuilder();
        target.append(indent(1)).append("<target name=\"compile\" depends=\"prepare\" description=\"Compilation of all source code.\">").append(newLine);
        target.append(indent(2)).append("<javac srcdir=\"${dir.src}\" destdir=\"${dir.build}\">").append(newLine);
        target.append(indent(3)).append("<classpath>").append(newLine);
        if (hasLibs) {
            target.append(indent(4)).append("<fileset dir=\"${dir.lib}\">").append(newLine);
            target.append(indent(5)).append("<include name=\"**/*.jar\"/>").append(newLine);
            target.append(indent(4)).append("</fileset>").append(newLine);
        }
        target.append(indent(4)).append("<fileset dir=\"${java.dir}\">").append(newLine);
        target.append(indent(5)).append("<include name=\"**/*.jar\"/>").append(newLine);
        target.append(indent(4)).append("</fileset>").append(newLine);
        target.append(indent(3)).append("</classpath>").append(newLine);
        target.append(indent(2)).append("</javac>").append(newLine);
        target.append(indent(1)).append("</target>").append(newLine);
        return target.toString();
    }

    private String generateJarTarget() {
        StringBuilder target = new StringBuilder();
        target.append(indent(1)).append("<target name=\"jar\" depends=\"compile\" description=\"Generates the .jar file in to base directory.\">").append(newLine);
        target.append(indent(2)).append("<jar jarfile=\"${dir.jar}/").append(this.componentName).append(".jar\" basedir=\"${dir.build}\"/>").append(newLine);
        target.append(indent(1)).append("</target>").append(newLine);
        return target.toString();
    }
}
