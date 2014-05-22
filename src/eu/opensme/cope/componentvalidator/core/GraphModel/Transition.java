package eu.opensme.cope.componentvalidator.core.GraphModel;

import java.util.ArrayList;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Transition extends Element{

	private int fromNode;
	private int toNode;
	private ArrayList<Invariant> invariants;	
	
	public Transition() {
		this.fromNode = -1;
		this.toNode = -1;
		this.invariants = new ArrayList<Invariant>();
	}

	public int getFromNode() {
		return fromNode;
	}

	public void setFromNode(int fromNode) {
		this.fromNode = fromNode;
	}

	public int getToNode() {
		return toNode;
	}

	public void setToNode(int toNode) {
		this.toNode = toNode;
	}	
	
	public ArrayList<Invariant> getInvariants() {
		return invariants;
	}

	public void setInvariants(ArrayList<Invariant> invariants) {
		this.invariants = invariants;
	}
	
	public void addInvariant(Invariant inv) {
		this.invariants.add(inv);
	}

	public int getInvariantsSize() {
		return this.invariants.size();
	}

	public Invariant getInvariant(int index) {
		return this.invariants.get(index);
	}

	public Invariant getInvariantByDescr(String descr) {
		for (int i = 0; i < this.invariants.size(); i++) {
			if (this.getInvariant(i).getDescr().equals(descr)){
				return this.getInvariant(i);
			}
		}
		return null;
	}	
	
}
