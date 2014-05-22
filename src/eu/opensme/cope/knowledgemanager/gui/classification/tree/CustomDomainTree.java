package eu.opensme.cope.knowledgemanager.gui.classification.tree;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

public class CustomDomainTree extends JTree {

    public CustomDomainTree(TreeDomainNodeData node) {
        super(new CustomDomainTreeModel(node));
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void expandAll() {
        for (int i = 0; i < this.getRowCount(); i++) {
            this.expandRow(i);
        }
    }
    
    
}
