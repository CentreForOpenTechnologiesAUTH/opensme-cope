/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui.matrix;

import java.awt.Component;
import java.awt.Insets;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;


class RowHeaderRenderer
    extends DefaultTableCellRenderer
    implements ListCellRenderer
{

	protected Border noFocusBorder;
	protected Border focusBorder;

    public RowHeaderRenderer()
    {
        setOpaque(true);
        setBorder(noFocusBorder);
    }

    public void updateUI()
    {
        super.updateUI();
        Border cell = UIManager.getBorder("TableHeader.cellBorder");
        Border focus = UIManager.getBorder("Table.focusCellHighlightBorder");

        focusBorder = new BorderUIResource.CompoundBorderUIResource(cell, focus);
 
        Insets i = focus.getBorderInsets(this);

        noFocusBorder = new BorderUIResource.CompoundBorderUIResource
             (cell, BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right));

        /* Alternatively, if focus shouldn't be supported:

        focusBorder = noFocusBorder = cell;
    
        */
    }

    public Component getListCellRendererComponent(JList list, Object value, 
        int index, boolean selected, boolean focused) 
    {
        if (list != null)
        {
            if (selected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setFont(list.getFont());

            setEnabled(list.isEnabled());
        }
        else
        {
            setBackground(UIManager.getColor("TableHeader.background"));
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setFont(UIManager.getFont("TableHeader.font"));
            setEnabled(true);
        }

        if (focused)
            setBorder(focusBorder);
        else
            setBorder(noFocusBorder);

        setValue(value);
 
        return this;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                       boolean selected, boolean focused, int row, int column)
    {
        if (table != null)
        {
            if (selected)
            {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            else
            {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            setFont(table.getFont());

            setEnabled(table.isEnabled());
        }
        else
        {
            setBackground(UIManager.getColor("TableHeader.background"));
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setFont(UIManager.getFont("TableHeader.font"));
            setEnabled(true);
        }
        
        if (focused)
            setBorder(focusBorder);
        else
            setBorder(noFocusBorder);

        setValue(value);
 
        return this;
    }
}