package eu.opensme.cope.componentvalidator.core.GraphModel;

import java.util.ArrayList;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Node extends Element{
	
	private int id;
	private int pointcut;
	private ArrayList<Invariant> invariants;
	private ArrayList<Invariant> postInvariants;
	private ArrayList<Invariant> returnInvariants;
	
	public Node() {
		this.id = 0;
		this.pointcut = 0;
		this.invariants = new ArrayList<Invariant>();
		this.postInvariants = new ArrayList<Invariant>();
		this.returnInvariants = new ArrayList<Invariant>();		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPointcut() {
		return pointcut;
	}

	public void setPointcut(int pointcut) {
		this.pointcut = pointcut;
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
	
	public Invariant getLastInvariantByLine(int line) {
		Invariant inv = null;
		for (int i = 0; i < this.invariants.size(); i++) {
			if (this.getInvariant(i).getLine() == line){
				inv= this.getInvariant(i);
			}
		}
		return inv;
	}

	public Invariant getInvariant(int index) {
		return this.invariants.get(index);
	}

	public int getInvariantsSize() {
		return this.invariants.size();
	}

	public ArrayList<Invariant> getPostInvariants() {
		return postInvariants;
	}

	public void setPostInvariants(ArrayList<Invariant> postInvariants) {
		this.postInvariants = postInvariants;
	}
	
	public void addPostInvariant(Invariant inv) {
		this.postInvariants.add(inv);
	}
	
	public Invariant getLastPostInvariantByLine(int line) {
		Invariant inv = null;
		for (int i = 0; i < this.postInvariants.size(); i++) {
			if (this.getPostInvariant(i).getLine() == line){
				inv= this.getPostInvariant(i);
			}
		}
		return inv;
	}

	public Invariant getPostInvariant(int index) {
		return this.postInvariants.get(index);
	}	

	public int getPostInvariantsSize() {
		return this.postInvariants.size();
	}	
	
	public ArrayList<Invariant> getReturnInvariants() {
		return returnInvariants;
	}

	public void setReturnInvariants(ArrayList<Invariant> returnInvariants) {
		this.returnInvariants = returnInvariants;
	}
	
	public void addReturnInvariant(Invariant inv) {
		this.returnInvariants.add(inv);
	}
	
	public Invariant getLastReturnInvariantByLine(int line) {
		Invariant inv = null;
		for (int i = 0; i < this.returnInvariants.size(); i++) {
			if (this.getReturnInvariant(i).getLine() == line){
				inv= this.getReturnInvariant(i);
			}
		}
		return inv;
	}

	public Invariant getReturnInvariant(int index) {
		return this.returnInvariants.get(index);
	}	

	public int getReturnInvariantsSize() {
		return this.returnInvariants.size();
	}		

	public Node copyNode(Node toNode){
		toNode.setDescr(this.getDescr());
		toNode.setId(this.getId());
		for (int k = 0; k < this.getInvariantsSize(); k++) {
			Invariant inv = new Invariant();
			inv.setDescr(this.getInvariant(k).getDescr());
			inv.setPackageName(this.getInvariant(k).getPackageName());
			inv.setType(this.getInvariant(k).getType());
			inv.setValue(this.getInvariant(k).getValue());
			inv.setKind(this.getInvariant(k).getKind());
			toNode.addInvariant(inv);
		}
		for (int k = 0; k < this.getPostInvariantsSize(); k++) {
			Invariant inv = new Invariant();
			inv.setDescr(this.getPostInvariant(k).getDescr());
			inv.setPackageName(this.getPostInvariant(k).getPackageName());
			inv.setType(this.getPostInvariant(k).getType());
			inv.setValue(this.getPostInvariant(k).getValue());
			inv.setKind(this.getPostInvariant(k).getKind());
			toNode.addPostInvariant(inv);
		}
		for (int k = 0; k < this.getReturnInvariantsSize(); k++) {
			Invariant inv = new Invariant();
			inv.setDescr(this.getReturnInvariant(k).getDescr());
			inv.setPackageName(this.getReturnInvariant(k).getPackageName());
			inv.setType(this.getReturnInvariant(k).getType());
			inv.setValue(this.getReturnInvariant(k).getValue());
			inv.setKind(this.getReturnInvariant(k).getKind());
			toNode.addReturnInvariant(inv);
		}
		return toNode;
	}

	public Invariant getInvariantByDescr(String descr) {
		for (int i = 0; i < this.invariants.size(); i++) {
			if (this.getInvariant(i).getDescr().equals(descr)){
				return this.getInvariant(i);
			}
		}
		return null;
	}

/*	
	public Invariant getReturnInvariantByDescr(String descr) {
		for (int i = 0; i < this.returnInvariants.size(); i++) {
			if (this.getReturnInvariant(i).getDescr().equals(descr)){
				return this.getReturnInvariant(i);
			}
		}
		return null;
	}
*/	
}
