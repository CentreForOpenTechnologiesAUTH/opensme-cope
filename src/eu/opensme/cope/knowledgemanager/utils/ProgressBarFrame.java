package eu.opensme.cope.knowledgemanager.utils;

import javax.swing.*;
import java.awt.*;

public class ProgressBarFrame extends JFrame {

    private JProgressBar jpb;

    // CONSTRUCTOR
    public ProgressBarFrame(String message) {

       
        jpb = new JProgressBar();
        jpb.setIndeterminate(false);
        jpb.setStringPainted(true);
        jpb.setString(message);
        this.setAlwaysOnTop(true);
        Container c = getContentPane();
        c.add(jpb);
        setUndecorated(true);
        jpb.setBackground(Color.white);


        
        setSize(300, 20);
        setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();

        // Put the frame into the center of the screen
        int x = (screenSize.width - frameSize.width) / 2;
        int y = (screenSize.height - frameSize.height) / 2;

        setLocation(x, y);
    }
    
    public void setIndeterminate(boolean flag){
        jpb.setIndeterminate(flag);
    }
}