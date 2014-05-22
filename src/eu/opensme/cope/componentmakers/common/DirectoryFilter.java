/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.common;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author sskalist
 */
public class DirectoryFilter implements FileFilter {

    public boolean accept(File file) {
        return file.isDirectory();
    }
}
