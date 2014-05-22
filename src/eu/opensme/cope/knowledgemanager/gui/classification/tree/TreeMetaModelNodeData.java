package eu.opensme.cope.knowledgemanager.gui.classification.tree;

import java.util.ArrayList;

public class TreeMetaModelNodeData implements Comparable<TreeMetaModelNodeData> {

    private String id;
    private String name;
    private String type;
    private ArrayList<TreeMetaModelNodeData> children;

    public TreeMetaModelNodeData() {
        children = new ArrayList<TreeMetaModelNodeData>();
    }

    public TreeMetaModelNodeData(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        children = new ArrayList<TreeMetaModelNodeData>();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<TreeMetaModelNodeData> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TreeMetaModelNodeData> children) {
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

    public int getIndexOfChild(TreeMetaModelNodeData kid) {
        return children.indexOf(kid);
    }

    @Override
    public int compareTo(TreeMetaModelNodeData o) {
        return name.compareTo(o.getName());
    }
}
