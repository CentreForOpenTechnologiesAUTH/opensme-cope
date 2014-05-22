package eu.opensme.cope.knowledgemanager.gui.configtool;

import java.io.File;

public class SimpleFilter extends javax.swing.filechooser.FileFilter {
    private String m_description = null;
    private String m_extension = null;
    
    public SimpleFilter(String extension, String description) {
        m_description = description;
	m_extension = "."+extension.toLowerCase();
    }

    public String getDescription() {
	return m_description;
    }
    
    public String getExtension()
    {
        return m_extension;
    }

    public boolean accept(File f) {
        if (f == null)
            return false;
	if (f.isDirectory())
            return true;
	return f.getName().toLowerCase().endsWith(m_extension);
    }
}
