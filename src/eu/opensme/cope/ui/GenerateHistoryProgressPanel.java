/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.DependenciesGenerator;
import eu.opensme.cope.factgenerators.historyanalyzer.SVNDBImporter;
import eu.opensme.cope.util.CommandLineUtil;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.beans.*;
import java.io.File;

public class GenerateHistoryProgressPanel extends JPanel
        implements ActionListener,
        PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
    private ReuseProject reuseProject;
    private String svnXMLFile;
    private String rootPackage;
    
    
    private Long projectId;
    private String projectJAR;
    private String projectTitle;
    private String projectLocation;
    private ComponentAdaptationEnvironmentMain parentFrame;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            projectTitle = reuseProject.getProject().getProjecttitle();
            projectId = reuseProject.getProject().getProjectid();
            projectJAR = reuseProject.getProjectJARFilename();
            projectLocation = reuseProject.getProjectLocation();

            //import to db
            DependenciesGenerator depGen;
            SVNDBImporter svnDBImporter;
            try {
                //Initialize progress property.
                svnDBImporter = new SVNDBImporter(svnXMLFile,projectTitle,rootPackage);
                svnDBImporter.parseSVNLogXML();
            } catch (Exception ex) {
                Logger.getLogger(GenerateHistoryProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

            //this.publish(100);
           setProgress(100);
            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null); //turn off the wait cursor
        }
    }

    public GenerateHistoryProgressPanel(ReuseProject project, ComponentAdaptationEnvironmentMain c, String svnXMLFile, String rootPackage) {
        super(new BorderLayout());
        this.reuseProject = project;
        this.svnXMLFile = svnXMLFile;
        this.rootPackage = rootPackage;       
        
        
        //Create the UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.parentFrame = c;

    }

    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        taskOutput.append("Detecting SVN log entries. Please wait...\n");
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);

            System.out.println(progress);
            if (progress == 25) {
                taskOutput.append("History Analysis DONE.\n");
                taskOutput.append("Importing SVN log entries in database. Please wait...\n");
            } else if (progress == 50) {
                taskOutput.append("History Analysis, stored DONE.\n");
                //taskOutput.append("Detecting and storing CK metrics in datbase.\n");
            } else if (progress == 100) {
                taskOutput.append("History Analysis DONE.");
                parentFrame.changePanel();
            }
        }
    }
}
