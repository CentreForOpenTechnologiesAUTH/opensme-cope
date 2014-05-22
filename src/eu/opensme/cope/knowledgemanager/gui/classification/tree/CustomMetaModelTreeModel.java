package eu.opensme.cope.knowledgemanager.gui.classification.tree;

import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class CustomMetaModelTreeModel implements TreeModel {

    private TreeMetaModelNodeData root;
    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

    public CustomMetaModelTreeModel() {
    }

    public CustomMetaModelTreeModel(TreeMetaModelNodeData root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        TreeMetaModelNodeData p = (TreeMetaModelNodeData) parent;
        return p.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TreeMetaModelNodeData) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TreeMetaModelNodeData) node).getChildren() == null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeMetaModelNodeData p = (TreeMetaModelNodeData) parent;
        TreeMetaModelNodeData ch = (TreeMetaModelNodeData) child;
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

    public void fireTreeNodesInserted(TreePath nodePath, TreeMetaModelNodeData newNode) {
        Object[] children = {newNode};
        int index = this.getIndexOfChild(nodePath.getLastPathComponent(), newNode);
        int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath, indicies, children);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesInserted(e);
        }
    }

    public void fireTreeNodesDeleted(TreePath nodePath, TreeMetaModelNodeData deleted, int index) {
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
    public void fireTreeStructureChanged(TreePath nodePath, TreeMetaModelNodeData changed) {
        Object[] children = {changed};
        //int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath.getParentPath(), null, null);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

    public TreePath getComponentPathWithID(String clickedID) {
        TreePath path = new TreePath(root);

        ArrayList<TreeMetaModelNodeData> children = root.getChildren();
        for (TreeMetaModelNodeData tier : children) {
            ArrayList<TreeMetaModelNodeData> components = tier.getChildren();
            for (TreeMetaModelNodeData component : components) {
                if (component.getId().equals(clickedID)) {
                    path = path.pathByAddingChild(tier);
                    path = path.pathByAddingChild(component);
                    return path;
                }
            }
        }
        return null;
    }

    public boolean containsMetaModelName(String name) {

        ArrayList<TreeMetaModelNodeData> expandSet = new ArrayList<TreeMetaModelNodeData>();
        expandSet.add(root);
        while (!expandSet.isEmpty()) {
            TreeMetaModelNodeData current = expandSet.remove(0);
            if (current.getName().equalsIgnoreCase(name)) {
                return true;
            } else {
                expandSet.addAll(current.getChildren());
            }
        }
        return false;
    }
}
