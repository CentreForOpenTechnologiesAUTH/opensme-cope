
package eu.opensme.cope.componentmakers.common;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author sskalist
 */
public class JavaSourceFilter implements FileFilter {

    public boolean accept(File file) {
        return file.getName().trim().endsWith(".java");
    }
}