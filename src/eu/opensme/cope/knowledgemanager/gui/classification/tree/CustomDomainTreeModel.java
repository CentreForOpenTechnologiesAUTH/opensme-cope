package eu.opensme.cope.knowledgemanager.gui.classification.tree;

import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class CustomDomainTreeModel implements TreeModel {

    private TreeDomainNodeData root;
    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

    public CustomDomainTreeModel() {
    }

    public CustomDomainTreeModel(TreeDomainNodeData root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        TreeDomainNodeData p = (TreeDomainNodeData) parent;
        return p.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TreeDomainNodeData) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TreeDomainNodeData) node).getChildren() == null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeDomainNodeData p = (TreeDomainNodeData) parent;
        TreeDomainNodeData ch = (TreeDomainNodeData) child;
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

    public void fireTreeNodesInserted(TreePath nodePath, TreeDomainNodeData newNode) {
        Object[] children = {newNode};
        int index = this.getIndexOfChild(nodePath.getLastPathComponent(), newNode);
        int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath, indicies, children);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesInserted(e);
        }
    }

    public void fireTreeNodesDeleted(TreePath nodePath, TreeDomainNodeData deleted, int index) {
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
    public void fireTreeStructureChanged(TreePath nodePath, TreeDomainNodeData changed) {
        Object[] children = {changed};
        //int[] indicies = {index};
        TreeModelEvent e = new TreeModelEvent(this, nodePath.getParentPath(), null, null);
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

    public TreePath getComponentPathWithID(String clickedID) {
        TreePath path = new TreePath(root);

        ArrayList<TreeDomainNodeData> children = root.getChildren();
        for (TreeDomainNodeData tier : children) {
            ArrayList<TreeDomainNodeData> components = tier.getChildren();
            for (TreeDomainNodeData component : components) {
                if (component.getId().equals(clickedID)) {
                    path = path.pathByAddingChild(tier);
                    path = path.pathByAddingChild(component);
                    return path;
                }
            }
        }
        return null;
    }

    public boolean containsDomainName(String name) {

        ArrayList<TreeDomainNodeData> expandSet = new ArrayList<TreeDomainNodeData>();
        expandSet.add(root);
        while (!expandSet.isEmpty()) {
            TreeDomainNodeData current = expandSet.remove(0);
            if (current.getName().equalsIgnoreCase(name)) {
                return true;
            } else {
                expandSet.addAll(current.getChildren());
            }
        }
        return false;
    }
}
