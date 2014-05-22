/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.common;

/**
 *
 * @author sskalist
 */
public class SourceFileNotFoundException extends Exception {

    private String className;
    private String sourcePath;
    @Override
    public String getMessage() {
        return "Source file for " + className + " was not found. (location should be " + sourcePath + ")";
    }

    
    public SourceFileNotFoundException(String className, String sourcePath) {
       this.className = className;
       this.sourcePath = sourcePath;
    }

    public String getClassName() {
        return className;
    }

    public String getSourcePath() {
        return sourcePath;
    }
    
}
