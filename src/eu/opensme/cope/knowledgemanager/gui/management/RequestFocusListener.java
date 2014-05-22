package eu.opensme.cope.knowledgemanager.gui.management;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener {

    @Override
    public void ancestorAdded(AncestorEvent e) {
        JComponent component = (JComponent) e.getComponent();
        component.requestFocusInWindow();
        ((JTextField) component).setSelectionStart(0);
        ((JTextField) component).setSelectionEnd(((JTextField) component).getText().length());
        //component.removeAncestorListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent e) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent e) {
    }
}