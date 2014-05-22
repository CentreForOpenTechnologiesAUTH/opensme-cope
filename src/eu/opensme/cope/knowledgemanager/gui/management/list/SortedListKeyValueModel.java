package eu.opensme.cope.knowledgemanager.gui.management.list;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;

public class SortedListKeyValueModel extends AbstractListModel implements ComboBoxModel {

    // Define a SortedSet
    SortedSet model;
    Object currentValue;
    private static Comparator USEFUL_COMPARATOR = new Comparator() {

        @Override
        public int compare(Object o1, Object o2) {
            String str1 = ((KeyValue) o1).getValue();
            String str2 = ((KeyValue) o2).getValue();
            Collator collator = Collator.getInstance();
            int result = collator.compare(str1, str2);
            return result;
        }
    };

    public SortedListKeyValueModel() {
        // Create a TreeSet
        // Store it in SortedSet variable
        model = new TreeSet(USEFUL_COMPARATOR);
    }

    // ListModel methods
    @Override
    public int getSize() {
        // Return the model size
        return model.size();
    }

    @Override
    public Object getElementAt(int index) {
        // Return the appropriate element
        return model.toArray()[index];
    }

    // Other methods
    public void add(Object element) {
        if (model.add(element)) {
            fireIntervalAdded(this, 0, getSize());
        }
    }

    public void addAll(Object elements[]) {
        Collection c = Arrays.asList(elements);
        model.addAll(c);
        fireIntervalAdded(this, 0, getSize());
    }

    public void clear() {
        model.clear();
        fireIntervalRemoved(this, 0, getSize());
    }

    public boolean contains(Object element) {
        return model.contains(element);
    }

    public boolean containsValue(String value) {
        for (Iterator it = model.iterator(); it.hasNext();) {
            KeyValue object = (KeyValue) it.next();
            if (object.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsKey(String key) {
        for (Iterator it = model.iterator(); it.hasNext();) {
            KeyValue object = (KeyValue) it.next();
            if (object.getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public Object firstElement() {
        // Return the appropriate element
        return model.first();
    }

    public Iterator iterator() {
        return model.iterator();
    }

    public Object lastElement() {
        // Return the appropriate element
        return model.last();
    }

    public boolean removeElement(Object element) {
        boolean removed = model.remove(element);
        if (removed) {
            fireIntervalRemoved(this, 0, getSize());
        }
        return removed;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        currentValue = anItem;
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getSelectedItem() {
        return currentValue;
    }
}
