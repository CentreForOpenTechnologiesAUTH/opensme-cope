package eu.opensme.cope.knowledgemanager.gui.classification.tree;

import java.util.ArrayList;

public class TreeDomainNodeData implements Comparable<TreeDomainNodeData> {

    private String id;
    private String name;
    private ArrayList<TreeDomainNodeData> children;

    public TreeDomainNodeData() {
        children = new ArrayList<TreeDomainNodeData>();
    }

    public TreeDomainNodeData(String id, String name) {
        this.id = id;
        this.name = name;
        children = new ArrayList<TreeDomainNodeData>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<TreeDomainNodeData> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TreeDomainNodeData> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return name;
    }

    Object getChildAt(int index) {
        return children.get(index);
    }

    public int getChildCount() {
        return children.size();
    }

    public int getIndexOfChild(TreeDomainNodeData kid) {
        return children.indexOf(kid);
    }

    @Override
    public int compareTo(TreeDomainNodeData o) {
        return name.compareTo(o.getName());
    }
}
