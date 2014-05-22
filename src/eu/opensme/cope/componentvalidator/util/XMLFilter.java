/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.util;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 *
 * @author barius
 */
public class XMLFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(file);
        if (extension != null) {
            if (extension.equals(Utils.xml)) {
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    @Override
    public String getDescription() {
        return "Just XML Files";

    }
}
