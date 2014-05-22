package eu.opensme.cope.knowledgemanager.gui.management.table;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class ComponentRelationTableModel extends AbstractTableModel {

    private ArrayList<ComponentRelationDataModel> list = new ArrayList<ComponentRelationDataModel>();
    private String[] columnNames = new String[]{"Name", "Version", "Tier"};

    public ComponentRelationTableModel(ArrayList<ComponentRelationDataModel> list) {
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
        }
        return null;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void addRow(ComponentRelationDataModel model) {
        list.add(model);
        fireTableDataChanged();
    }

    public ArrayList<ComponentRelationDataModel> getDataModel() {
        return list;
    }

    public void removeObjectAt(int modelView) {
        list.remove(modelView);
        fireTableDataChanged();
    }

    public void clearAll() {
        list.clear();
        fireTableDataChanged();
    }

    public ComponentRelationDataModel getDataAtRow(int row) {
        return list.get(row);
    }
}
