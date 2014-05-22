package eu.opensme.cope.knowledgemanager.gui.management.tree;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

public class CustomTree extends JTree {

    public CustomTree(TreeNodeData node) {
        super(new CustomTreeModel(node));
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }
}
