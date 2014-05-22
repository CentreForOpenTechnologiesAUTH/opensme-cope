/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.analyzers.Analyzer;
import eu.opensme.cope.analyzers.DependencyAnalyzer;
import eu.opensme.cope.analyzers.MetricsAnalyzer;
import eu.opensme.cope.domain.ReuseProject;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.Vector;

public class GenerateDependenciesProgressPanel extends JPanel
        implements ActionListener,
        PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
    private DependencyAnalyzer dependencyAnalyzer;
    private MetricsAnalyzer metricsAnalyzer;
    private ComponentAdaptationEnvironmentMain parentFrame;
    private ReuseProject reuseProject;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {

            dependencyAnalyzer.analyze();         
            setProgress(25);
            dependencyAnalyzer.storeData();         
            setProgress(50);
            metricsAnalyzer.analyze();
            setProgress(75);
            metricsAnalyzer.storeData();
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

    public GenerateDependenciesProgressPanel(ReuseProject reuseProject, ComponentAdaptationEnvironmentMain c) {
        super(new BorderLayout());
        this.reuseProject = reuseProject;
        dependencyAnalyzer = new DependencyAnalyzer(reuseProject);
        metricsAnalyzer = new MetricsAnalyzer(reuseProject);
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
        taskOutput.append("Detecting dependencies. Please wait...\n");
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
                taskOutput.append("Dependencies detection DONE.\n");
                taskOutput.append("Importing dependencies in database. Please wait...\n");
            } else if (progress == 50) {
                taskOutput.append("Dependencies stored DONE.\n");
                taskOutput.append("Detecting and storing CK metrics in datbase.\n");
            } else if (progress == 100) {         
                if (((MetricsAnalyzer)metricsAnalyzer).getMissingDependencies().size()>0){
                    taskOutput.append("\nError: Incorrect metric values.\n");
                    taskOutput.append("Add the following dependencies and run static analysis again:\n");
                    Vector<String> deps = ((MetricsAnalyzer) metricsAnalyzer).getMissingDependencies();
                    for (int i=0;i<deps.size();i++){
                        taskOutput.append(deps.elementAt(i) +"\n");
                        reuseProject.setStaticAnalysisPerformed(false);                        
                    }
                }else{
                        reuseProject.setStaticAnalysisPerformed(true);
                }
                taskOutput.append("\nStatic analysis DONE.");   
                taskOutput.validate();
                parentFrame.changePanel();
            }
        }
    }
}
