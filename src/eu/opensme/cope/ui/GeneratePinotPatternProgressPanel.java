package eu.opensme.cope.ui;

import eu.opensme.cope.analyzers.Analyzer;
import eu.opensme.cope.analyzers.PatternAnalyzer;
import eu.opensme.cope.analyzers.PinotPatternAnalyzer;
import eu.opensme.cope.domain.ReuseProject;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

public class GeneratePinotPatternProgressPanel extends JPanel implements ActionListener, PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
    private Analyzer patternAnalyzer;
    private ComponentAdaptationEnvironmentMain parentFrame;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */

        @Override
        public Void doInBackground() {
            
            boolean success = patternAnalyzer.analyze();
            
            setProgress(50);
            if (success == true) 
                patternAnalyzer.storeData();
//            } else {
//                setProgress(90);
//            }
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

    public GeneratePinotPatternProgressPanel(ReuseProject reuseProject, ComponentAdaptationEnvironmentMain c) {
        super(new BorderLayout());
        patternAnalyzer = new PinotPatternAnalyzer(reuseProject);
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
        taskOutput.append("Detecting patterns. Please wait...\n");
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
                taskOutput.append("Detecting pattern DONE.\n");
            } else if (progress == 55) {
                taskOutput.append("Detecting all pattern participants. Please wait... \n");
            } else if (progress == 90) {
                taskOutput.append("Importing patterns in database. Please wait...\n");
            } else if (progress == 100) {
                taskOutput.append("Patterns stored DONE.\n");
                taskOutput.append("Pattern analysis DONE.");
                parentFrame.changePanel();
            } else if (progress == 90) {
                taskOutput.append("Pattern analysis already done before!");
                parentFrame.changePanel();
            }
        }
    }
}
