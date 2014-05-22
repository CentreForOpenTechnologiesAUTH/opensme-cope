package eu.opensme.cope.knowledgemanager.gui.management.table;

import java.util.ArrayList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class ComponentListTableModel extends AbstractTableModel {

    private ArrayList<ComponentListDataModel> list = new ArrayList<ComponentListDataModel>();
    private String[] columnNames = new String[]{"Name", "Version", "Tier", "Language", "Technology"};

    public ComponentListTableModel(ArrayList<ComponentListDataModel> list) {
        this.list = list;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (list.isEmpty()) {
            return "";
        }
        if (columnIndex == 0) //name
        {
            return list.get(rowIndex).getComponentName();
        } else if (columnIndex == 1) //version
        {
            return list.get(rowIndex).getVersion();
        } else if (columnIndex == 2) //tier
        {
            return list.get(rowIndex).getTier();
        } else if (columnIndex == 3) //language
        {
            return list.get(rowIndex).getLanguage();
        } else if (columnIndex == 4) //technology
        {
            return list.get(rowIndex).getTechnology();
        }
        return null;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void addRow(ComponentListDataModel model) {
        list.add(model);
        fireTableDataChanged();
    }

    public void clearAll() {
        list.clear();
        fireTableDataChanged();
    }

    public void addAll(ArrayList<ComponentListDataModel> result) {
        list.addAll(result);
        fireTableDataChanged();
    }

    public ComponentListDataModel getDataAtRow(int row) {
        return list.get(row);
    }

    
}
