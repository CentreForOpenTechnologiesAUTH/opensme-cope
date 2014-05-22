package eu.opensme.cope.knowledgemanager.gui.management;

import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

public class MyGlassPane extends JComponent {

    public MyGlassPane() {
        CBListener listener = new CBListener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }
}

class CBListener extends MouseInputAdapter {

    public CBListener() {
    }

    public void mouseMoved(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseDragged(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e, true);
    }

    public void mouseReleased(MouseEvent e) {
        redispatchMouseEvent(e, true);
    }

    private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
    }
}
