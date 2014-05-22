/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.recommenders.DependenciesRecommender;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

public class DependenciesRecommenderProgressPanel extends JPanel
        implements ActionListener,
        PropertyChangeListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;
    private ReuseProject reuseProject;
    private DependenciesRecommender dr;

    class Task extends SwingWorker<Void, Void> {

        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            //Initialize progress property.
            setProgress(0);
            try {
                dr = new DependenciesRecommender(reuseProject);
                dr.createClusters();
                setProgress(100);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            setProgress(100);
            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            reuseProject.addClusters(dr.getClusters());
            Toolkit.getDefaultToolkit().beep();
            setCursor(null); //turn off the wait cursor
        }
    }

    public DependenciesRecommenderProgressPanel(ReuseProject project) {
        super(new BorderLayout());
        this.reuseProject = project;
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
        progressBar.setIndeterminate(true);
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        taskOutput.append("Creating clusters. Please wait...\n");
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
            if (progress == 0) {
                progressBar.setIndeterminate(true);
            } else if (progress == 100) {
                progressBar.setIndeterminate(false);
                taskOutput.append("Cluster creation completed.\n");
                taskOutput.append("No. of clusters: " + dr.getClusters().size() + "\n");

                taskOutput.append("Cluster details\n");

                for (int i = 0; i < dr.getClusters().size(); i++) {
                    taskOutput.append("Cluster details for cluster " + dr.getClusters().get(i).getName() + "\n");
                    taskOutput.append("\tCluster size: " + dr.getClusters().get(i).getClusterParticipants().size() + "\n");
                    taskOutput.append("\tCluster classes\n");
                    for (int j = 0; j < dr.getClusters().get(i).getClusterParticipants().size(); j++) {
                        taskOutput.append("\t\tClass Name: " + (dr.getClusters().get(i).getClusterParticipants().get(j) != null ? dr.getClusters().get(i).getClusterParticipants().get(j).getClassAnalysis().getName() : "null") + "\n");
                    }

                }
                
                if (reuseProject != null) {
                    JDialog dialog = new JDialog();
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    JComponent newContentPane = new ClusterVisualizeDialog(reuseProject);
                    newContentPane.setOpaque(true);
                    dialog.setTitle("Cluster Visualization Dialog");
                    dialog.setContentPane(newContentPane);
                    dialog.pack();
                    dialog.setVisible(true);
                    this.getParent().getParent().getParent().setVisible(false);
                    this.repaint();
                }
            }
        }
    }
}