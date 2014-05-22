/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.analyzers.Analyzer;
import eu.opensme.cope.analyzers.DependencyAnalyzer;
import eu.opensme.cope.analyzers.DependencyTypeAnalyzer;
import eu.opensme.cope.analyzers.MetricsAnalyzer;
import eu.opensme.cope.domain.ReuseProject;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.Vector;

public class GenerateDependencyTypesProgressPanel extends JPanel
        implements ActionListener,
        PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
    private DependencyTypeAnalyzer dependencyAnalyzer;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {

            dependencyAnalyzer.analyze();         
            setProgress(50);
            dependencyAnalyzer.storeData();         
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

    public GenerateDependencyTypesProgressPanel(ReuseProject reuseProject) {
        super(new BorderLayout());
        dependencyAnalyzer = new DependencyTypeAnalyzer(reuseProject);
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
        if(!reuseProject.isStaticAnalysisPerformed()){
            taskOutput.append("Static Analysis is not perfomed yet...\n");
            taskOutput.append("Please perform the static analysis first.\n");
            taskOutput.validate();
            startButton.setEnabled(false);
        }
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
        taskOutput.append("Detecting the types of dependencies. Please wait...\n");
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
            if (progress == 50) {
                taskOutput.append("Dependency Types detection DONE.\n");
                taskOutput.append("Importing dependencies in database. Please wait...\n");
                
            } else if (progress == 100) {         
                taskOutput.append("Dependency Types stored DONE.\n");
                taskOutput.append("\nDependency Types analysis DONE.");   
                taskOutput.validate();
            }
        }
    }
}
