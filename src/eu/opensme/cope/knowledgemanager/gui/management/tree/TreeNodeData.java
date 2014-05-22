package eu.opensme.cope.knowledgemanager.gui.management.tree;

import java.util.ArrayList;

public class TreeNodeData implements Comparable<TreeNodeData> {

    private String id;
    private String name;
    private boolean classified;
    private ArrayList<TreeNodeData> children;

    public TreeNodeData() {
    }

    public TreeNodeData(String id, String name) {
        this.id = id;
        this.name = name;
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

    public ArrayList<TreeNodeData> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TreeNodeData> children) {
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

    public int getIndexOfChild(TreeNodeData kid) {
        return children.indexOf(kid);
    }

    @Override
    public int compareTo(TreeNodeData o) {
        return name.compareTo(o.getName());
    }

    public boolean isClassified() {
        return classified;
    }

    public void setClassified(boolean classified) {
        this.classified = classified;
    }
    
}
