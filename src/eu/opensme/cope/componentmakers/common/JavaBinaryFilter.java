/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.common;

import java.io.File;

/**
 *
 * @author sskalist
 */
public class JavaBinaryFilter {
    public boolean accept(File file) {
        return file.getName().trim().endsWith(".class");
    }
}
