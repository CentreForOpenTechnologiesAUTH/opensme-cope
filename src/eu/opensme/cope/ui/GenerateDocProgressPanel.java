/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.DocumentationGenerator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

public class GenerateDocProgressPanel extends JPanel
        implements ActionListener,
        PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
    private ReuseProject project;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */

        @Override
        public Void doInBackground() {
            //Random random = new Random();
            DocumentationGenerator docGen = new DocumentationGenerator(project);

            //Initialize progress property.
            setProgress(0);

            docGen.expandJARFile();
            setProgress(50);
            //this.publish(50);

            docGen.generateDocumentation();
            setProgress(100);
            //this.publish(100);

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

    public GenerateDocProgressPanel(ReuseProject project) {
        super(new BorderLayout());
        this.project = project;
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
        taskOutput.append("Expanding JAR file. Please wait...\n");
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
                taskOutput.append("JAR expansion DONE.\n");
                taskOutput.append("Generating project documentation. Please wait...\n");
            } else {
                taskOutput.append("Documentation generationn DONE.");
            }
        }
    }
}
