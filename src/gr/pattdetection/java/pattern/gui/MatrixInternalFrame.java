/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui;

import gr.pattdetection.java.pattern.TsantalisPatternInstance;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class MatrixInternalFrame extends JInternalFrame {
    //private DefaultTableModel model;
	private JTree patternInstanceTree;

    public MatrixInternalFrame(String title) {
		super(title,true,true,true,true);
		/*
        String[] columnNames = {"Pattern", "Instances", "Classes"};
        model = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 2;
            }
        };
        JTable table = new JTable(model) {
            public TableCellRenderer getCellRenderer(int row, int column) {
				TableColumn tableColumn = getColumnModel().getColumn(column);
				TableCellRenderer renderer = tableColumn.getCellRenderer();
				if (renderer == null) {
					Class c = getColumnClass(column);
					if( c.equals(Object.class) ) {
						Object o = getValueAt(row,column);
						if( o != null )
							c = getValueAt(row,column).getClass();
					}
					renderer = getDefaultRenderer(c);
				}
				return renderer;
			}

            public TableCellEditor getCellEditor(int row, int column) {
				TableColumn tableColumn = getColumnModel().getColumn(column);
				TableCellEditor editor = tableColumn.getCellEditor();
				if (editor == null) {
					Class c = getColumnClass(column);
					if( c.equals(Object.class) ) {
						Object o = getValueAt(row,column);
						if( o != null )
							c = getValueAt(row,column).getClass();
					}
					editor = getDefaultEditor(c);
				}
				return editor;
			}
        };
        table.setDefaultEditor(JComponent.class, new JComponentCellEditor());
        table.setDefaultRenderer(JComponent.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
			    return (JComponent)value;
		    }
	    });
        table.getColumnModel().getColumn(0).setMinWidth(160);
        table.getColumnModel().getColumn(0).setMaxWidth(160);
        table.getColumnModel().getColumn(1).setMinWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        JScrollPane scrollPane = new JScrollPane(table);
 		*/
		this.patternInstanceTree = new JTree(new DefaultMutableTreeNode());
		
		JScrollPane scrollPane = new JScrollPane(patternInstanceTree);
        this.setContentPane(scrollPane);
		this.setVisible(true);
		this.setLocation(0,0);
	}
    /*
    public void addRow(String patternName, JComboBox comboBox) {
        model.addRow(new Object[] {patternName, comboBox.getItemCount(), comboBox} );
    }
    */
    public void addPatternNode(String patternName, Vector<TsantalisPatternInstance> instances) {
    	DefaultMutableTreeNode parent = new DefaultMutableTreeNode(patternName);
    	int counter = 1;
    	for(TsantalisPatternInstance instance : instances) {
    		DefaultMutableTreeNode instanceChild = new DefaultMutableTreeNode("instance " + counter);
    		parent.add(instanceChild);
    		Iterator<TsantalisPatternInstance.Entry> entryIterator = instance.getRoleIterator();
    		while(entryIterator.hasNext()) {
    			TsantalisPatternInstance.Entry entry = entryIterator.next();
    			DefaultMutableTreeNode entryChild = new DefaultMutableTreeNode(entry);
    			instanceChild.add(entryChild);
    		}
    		counter++;
    	}
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode)patternInstanceTree.getModel().getRoot();
    	root.add(parent);
    	((DefaultTreeModel)patternInstanceTree.getModel()).reload();
    }
}