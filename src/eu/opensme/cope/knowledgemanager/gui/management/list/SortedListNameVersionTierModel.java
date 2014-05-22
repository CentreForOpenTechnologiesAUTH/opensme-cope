package eu.opensme.cope.knowledgemanager.gui.management.list;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListNameVersionTierDataModel;

public class SortedListNameVersionTierModel extends AbstractListModel {

    // Define a SortedSet
    SortedSet model;
    Object currentValue;
    private static Comparator USEFUL_COMPARATOR = new Comparator() {

        @Override
        public int compare(Object o1, Object o2) {
            String str1 = ((SortedListNameVersionTierDataModel) o1).getId();
            String str2 = ((SortedListNameVersionTierDataModel) o2).getId();
            Collator collator = Collator.getInstance();
            int result = collator.compare(str1, str2);
            return result;
        }
    };

    public SortedListNameVersionTierModel() {
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

    public List<SortedListNameVersionTierDataModel> getModelAsList() {
        SortedListNameVersionTierDataModel[] result = new SortedListNameVersionTierDataModel[model.size()];
        model.toArray(result);
        return Arrays.asList(result);
    }
}
