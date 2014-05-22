package eu.opensme.cope.knowledgemanager.gui.management.table;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class MethodTableModel extends AbstractTableModel {

    private ArrayList<MethodDataModel> list = new ArrayList<MethodDataModel>();
    private String[] columnNames = new String[]{"Name", "Parameters", "Returns", "Throws"};

    public MethodTableModel(ArrayList<MethodDataModel> list) {
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
            return list.get(rowIndex).getName();
        } else if (columnIndex == 1) //version
        {
            return list.get(rowIndex).getParameters();
        }else if (columnIndex == 2) //version
        {
            return list.get(rowIndex).getReturns();
        }else if (columnIndex == 3) //version
        {
            return list.get(rowIndex).getThrows();
        }
        return null;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void addRow(MethodDataModel model) {
        list.add(model);
        fireTableDataChanged();
    }
    
    public void addAll(ArrayList<MethodDataModel> model) {
        list.addAll(model);
        fireTableDataChanged();
    }

    public ArrayList<MethodDataModel> getDataModel() {
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

    public MethodDataModel getDataAtRow(int row) {
        return list.get(row);
    }

    public int getObjectRowWithID(String id) {
        for (int i = 0; i < list.size(); i++) {
            MethodDataModel data = list.get(i);
            if (data.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
