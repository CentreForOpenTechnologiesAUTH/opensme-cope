package eu.opensme.cope.knowledgemanager.gui.management.table;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

public class InterfaceTableModel extends AbstractTableModel {

    private ArrayList<InterfaceDataModel> list = new ArrayList<InterfaceDataModel>();
    private String[] columnNames = new String[]{"Name", "Version"};

    public InterfaceTableModel(ArrayList<InterfaceDataModel> list) {
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
            return list.get(rowIndex).getInterfaceName();
        } else if (columnIndex == 1) //version
        {
            return list.get(rowIndex).getVersion();
        }
        return null;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void addRow(InterfaceDataModel model) {
        list.add(model);
        fireTableDataChanged();
    }

    public ArrayList<InterfaceDataModel> getDataModel() {
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

    public InterfaceDataModel getDataAtRow(int row) {
        return list.get(row);
    }

    public int getObjectRowWithID(String id) {
        for (int i = 0; i < list.size(); i++) {
            InterfaceDataModel data = list.get(i);
            if (data.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
