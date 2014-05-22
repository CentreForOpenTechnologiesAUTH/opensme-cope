package eu.opensme.cope.componentvalidator.core.DaikonParser;

import java.util.ArrayList;

public class Ppt {

	private ArrayList<Variable> variables;

	public Ppt() {
		this.variables = new ArrayList<Variable>();		
	}
	
	public ArrayList<Variable> getVariables() {
		return variables;
	}

	public void setVariables(ArrayList<Variable> variables) {
		this.variables = variables;
	}

	public void addVariable(Variable variable) {
		this.variables.add(variable);
	}

	public int getVariablesSize() {
		return this.variables.size();
	}

	public Variable getVariable(int index) {
		return this.variables.get(index);
	}
	
	public Variable getVariableByDescr(String descr){
		for (int i = 0; i < getVariablesSize(); i++) {
			if(getVariable(i).getDescr().equals(descr)){
				return getVariable(i);
			}
		}
		return null;
	}
}
