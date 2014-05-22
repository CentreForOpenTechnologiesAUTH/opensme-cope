package eu.opensme.cope.knowledgemanager.gui.management.tree;

import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class CustomTreeModel implements TreeModel {

    private TreeNodeData root;
    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

    public CustomTreeModel() {
    }

    public CustomTreeModel(TreeNodeData root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        TreeNodeData p = (TreeNodeData) parent;
        return p.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TreeNodeData) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TreeNodeData) node).getChildren() == null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeNodeData p = (TreeNodeData) parent;
        TreeNodeData ch = (TreeNodeData) child;
        return p.getIndexOfChild(ch);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    public void fireTreeNodesInserted(TreePath nodePath, TreeNodeData newNode) {
        Object[] children = {newNode};
        int index = this.getIndexOfChild(nodePath.getLastPathComponent(), newNode);
        int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath, indicies, children);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesInserted(e);
        }
    }

    public void fireTreeNodesDeleted(TreePath nodePath, TreeNodeData deleted, int index) {
        Object[] children = {deleted};
        int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath, indicies, children);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesRemoved(e);
        }
    }

//    public void fireTreeNodesChanged(TreePath nodePath, TreeNodeData changed, int index) {
//        Object[] children = {changed};
//        int[] indicies = {index};
//        TreeModelEvent e = new TreeModelEvent(this, nodePath, indicies, children);
//        for (TreeModelListener tml : treeModelListeners) {
//            tml.treeNodesChanged(e);
//        }
//    }
    public void fireTreeStructureChanged(TreePath nodePath, TreeNodeData changed) {
        Object[] children = {changed};
        //int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath.getParentPath(), null, null);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

    public TreePath getComponentPathWithID(String clickedID) {
        TreePath path = new TreePath(root);

        ArrayList<TreeNodeData> children = root.getChildren();
        for (TreeNodeData tier : children) {
            ArrayList<TreeNodeData> components = tier.getChildren();
            for (TreeNodeData component : components) {
                if (component.getId().equals(clickedID)) {
                    path = path.pathByAddingChild(tier);
                    path = path.pathByAddingChild(component);
                    return path;
                }
            }
        }
        return null;
    }

    public boolean containsComponentName(String name) {
        ArrayList<TreeNodeData> children = root.getChildren();
        for (TreeNodeData tier : children) {
            ArrayList<TreeNodeData> components = tier.getChildren();
            for (TreeNodeData component : components) {
                if (component.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
