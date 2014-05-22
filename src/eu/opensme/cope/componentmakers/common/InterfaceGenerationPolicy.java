/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.common;

import eu.opensme.cope.componentmakers.IPolicy;

/**
 *
 * @author sskalist
 */
public class InterfaceGenerationPolicy implements IPolicy {
    private boolean generateSelectedClass;
    private boolean generateUnusedClasses;
    private boolean generateExternallyCalledClass;

    public InterfaceGenerationPolicy() {
        this.generateSelectedClass = false;
        this.generateUnusedClasses = false;
        this.generateExternallyCalledClass = false;
    }

    public InterfaceGenerationPolicy(boolean generateSelectedClass, boolean generateUnusedClasses, boolean generateExternallyCalledClass) {
        this.generateSelectedClass = generateSelectedClass;
        this.generateUnusedClasses = generateUnusedClasses;
        this.generateExternallyCalledClass = generateExternallyCalledClass;
    }

    public boolean isGenerateExternallyCalledClass() {
        return generateExternallyCalledClass;
    }

    public void setGenerateExternallyCalledClass(boolean generateExternallyCalledClass) {
        this.generateExternallyCalledClass = generateExternallyCalledClass;
    }

    public boolean isGenerateSelectedClass() {
        return generateSelectedClass;
    }

    public void setGenerateSelectedClass(boolean generateSelectedClass) {
        this.generateSelectedClass = generateSelectedClass;
    }

    public boolean isGenerateUnusedClasses() {
        return generateUnusedClasses;
    }

    public void setGenerateUnusedClasses(boolean generateUnusedClasses) {
        this.generateUnusedClasses = generateUnusedClasses;
    }
    
    
}
