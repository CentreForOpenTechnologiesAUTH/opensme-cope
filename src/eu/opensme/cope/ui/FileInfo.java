/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.opensme.cope.ui;

import java.io.File;

/**
 *
 * @author george
 */
public class FileInfo {
    private File file;

    public FileInfo(File f) {
        file=f;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
