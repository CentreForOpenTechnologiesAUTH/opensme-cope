package eu.opensme.cope.componentvalidator.core.ProMParser;

import java.util.ArrayList;
import java.util.List;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Process extends Element{

	private int id;
	private List<ProcessInstance> processInstances; 
	
	public Process() {
		this.id = 0;
		this.processInstances = new ArrayList<ProcessInstance>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<ProcessInstance> getProcessInstances() {
		return processInstances;
	}

	public void setProcessInstances(List<ProcessInstance> processInstances) {
		this.processInstances = processInstances;
	}
	
	public void addProcessInstance(ProcessInstance pi) {
		pi.setId(this.processInstances.size() +1);
		this.processInstances.add(pi);
	}

	public ProcessInstance getProcessInstance(int index) {
		return this.processInstances.get(index);
	}
	
	public ProcessInstance getProcessInstanceByDescr(String descr) {
		for (int i = 0; i < this.processInstances.size(); i++) {
			if (this.getProcessInstance(i).getDescr().equals(descr)){
				return this.getProcessInstance(i);
			}
		}
		return null;
	}
	
	public ProcessInstance getProcessInstanceById(int id) {
		for (int i = 0; i < this.processInstances.size(); i++) {
			if (this.getProcessInstance(i).getId() == id){
				return getProcessInstance(i);
			}
		}
		return null;
	}

	public int getProcessInstancesSize() {
		return this.processInstances.size();
	}	
	
}
