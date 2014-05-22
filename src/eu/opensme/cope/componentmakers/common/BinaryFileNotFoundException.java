/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.common;

import eu.opensme.cope.componentmakers.ComponentMaker;

/**
 *
 * @author sskalist
 */
public class BinaryFileNotFoundException extends Exception {

    private String className;
    private String sourcePath;

    @Override
    public String getMessage() {
        return "Binary file for " + className + " was not found. (location should be " + ComponentMaker.getJarPath() +":" + sourcePath + ")";
    }

    public BinaryFileNotFoundException(String className, String sourcePath) {
        this.className = className;
        this.sourcePath = sourcePath;
    }
}
