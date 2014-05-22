/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.ui;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author sskalist
 */
public class ClassReusabilityTableModel extends AbstractTableModel {

    private String[] columnNames = {"Class Name", "Class Type","Reusability Assessment"};
    private List<ClassAnalysis> data;

    public ClassReusabilityTableModel(Set<ClassAnalysis> classes) {
        data = new ArrayList<ClassAnalysis>(classes);
    }

    public ClassAnalysis getRowObject(int row) {
        return data.get(row);
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        ClassAnalysis c = data.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return c.getName();
            case 1:
                return c.getType();
            case 2:
                return c.getReusabilityAssessment();
            default:
                return "Undef. case";
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getIndexOf(ClassAnalysis target) {
        int i = 0;
        for (ClassAnalysis classAnalysis : this.data) {
            if (classAnalysis.equals(target)) {
                return i;
            }
            i++;
        }
        return -1;
    }
}
