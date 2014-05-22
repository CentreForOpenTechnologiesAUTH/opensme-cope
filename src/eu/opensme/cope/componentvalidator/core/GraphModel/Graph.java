package eu.opensme.cope.componentvalidator.core.GraphModel;

import java.util.ArrayList;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Graph extends Element{

	private int id;
	private ArrayList<Node> nodes;
	private ArrayList<Transition> transitions;
	
	public Graph() {
		this.id = 0;
		this.nodes = new ArrayList<Node>();
		this.transitions = new ArrayList<Transition>();
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<Transition> getTransitions() {
		return transitions;
	}

	public void setTransitions(ArrayList<Transition> transitions) {
		this.transitions = transitions;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void addNode(Node n) {
		this.nodes.add(n);
	}

	public Node getNode(int index) {
		return this.nodes.get(index);
	}
	
	public Node getNodeByDescr(String descr) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).getDescr().equals(descr)){
				return this.getNode(i);
			}
		}
		return null;
	}
	
	public Node getFirstNodeByPointcut(int pointcut) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).getPointcut() == pointcut){
				return getNode(i);
			}
		}
		return null;
	}

	public Node getLastNodeByPointcut(int pointcut) {
		Node n = null;
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).getPointcut() == pointcut){
				n= getNode(i);
			}
		}
		return n;
	}

	public int getLastIndexOfNode(Node n) {
		int index = -1;
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).equals(n)){
				index = i;
			}
		}
		return index;
	}
	
	public int getNodeSize() {
		return this.nodes.size();
	}	

	public void addTransition(Transition tr) {
		this.transitions.add(tr);
	}

	public Transition getTransition(int index) {
		return this.transitions.get(index);
	}
	
	public Transition getTransitionByDescr(String descr) {
		for (int i = 0; i < this.transitions.size(); i++) {
			if (this.getTransition(i).getDescr().equals(descr)){
				return this.getTransition(i);
			}
		}
		return null;
	}

	public int getTransitionsSize() {
		return this.transitions.size();
	}
}
