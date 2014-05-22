/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FirstGraph.java
 *
 * Created on Nov 23, 2011, 12:30:22 PM
 */
package eu.opensme.cope.componentvalidator.ui;

import eu.opensme.cope.componentvalidator.coverage.cfg.CfgMethod;
import eu.opensme.cope.componentvalidator.coverage.cfg.CfgNode;
import eu.opensme.cope.componentvalidator.util.Utils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;

/**
 *
 * @author thanasis
 */
public class CfgViewerFrame extends javax.swing.JFrame {

     private JScrollPane scrollPane;
     private JGraphModelAdapter m_jgAdapter;
     private CfgMethod method;

    
     CfgViewerFrame(CfgMethod selectedMethod)
     {
        //initComponents();
        this.method  = selectedMethod;
        JGraph jgraph = method.generateCfg();
        scrollPane = new JScrollPane(jgraph);
        
        JPanel cfgPanel = new JPanel();
        cfgPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Control Flow Graph", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        cfgPanel.setMinimumSize(new Dimension(300, 500));
        scrollPane.setMinimumSize(new Dimension(300, 500));
        cfgPanel.setLayout(new BorderLayout());
        cfgPanel.add(scrollPane, BorderLayout.CENTER);
        setTitle("Control Flow Graph Viewer");
        getContentPane().add(cfgPanel, BorderLayout.CENTER);
        pack();
        setVisible(true);
     }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(300, 500));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 410, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 495, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
