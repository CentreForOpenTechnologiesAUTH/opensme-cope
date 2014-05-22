/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DetailsJDialog.java
 *
 * Created on Nov 1, 2011, 1:46:56 PM
 */
package eu.opensme.cope.componentvalidator.ui;

import java.io.File;
import javax.swing.JFrame;

/**
 *
 * @author thanasis
 */
public class DetailsJDialog extends javax.swing.JDialog {

    private File htmlFile;
    

    DetailsJDialog(JFrame jFrame, String string, String htmlFilePath) {
        super(jFrame, string);
        htmlFile = new File(htmlFilePath);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 553, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    @Override
    public void dispose() {
       // do your work here
       super.dispose();
       htmlFile.delete();
    }   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
