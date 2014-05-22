package eu.opensme.cope.componentvalidator.core.ProMParser;

import java.util.ArrayList;
import java.util.List;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class AuditTrailEntry extends Element{
	
	List<WorkflowElement> workflowElements; 
	
	public AuditTrailEntry() {
		this.workflowElements = new ArrayList<WorkflowElement>();
	}

	public List<WorkflowElement> getWorkflowElements() {
		return workflowElements;
	}

	public void setWorkflowElements(List<WorkflowElement> workflowElements) {
		this.workflowElements = workflowElements;
	}
	
	public void addWorkflowElement(WorkflowElement we) {
		this.workflowElements.add(we);
	}

	public WorkflowElement getWorkflowElement(int index) {
		return this.workflowElements.get(index);
	}
	
	public WorkflowElement getWorkflowElementByDescr(String descr) {
		for (int i = 0; i < this.workflowElements.size(); i++) {
			if (this.getWorkflowElement(i).getDescr().equals(descr)){
				return this.getWorkflowElement(i);
			}
		}
		return null;
	}

	public WorkflowElement getFirstWorkflowElementByPointcut(int pointcut) {
		for (int i = 0; i < this.workflowElements.size(); i++) {
			if (this.getWorkflowElement(i).getPointcut() == pointcut){
				return this.getWorkflowElement(i);
			}
		}
		return null;
	}

	public WorkflowElement getLastWorkflowElementByPointcut(int pointcut) {
		WorkflowElement we = null;
		for (int i = 0; i < this.workflowElements.size(); i++) {
			if (this.getWorkflowElement(i).getPointcut() == pointcut){
				we = this.getWorkflowElement(i);
			}
		}
		return we;
	}
	
	public int getWorkflowElementsSize() {
		return this.workflowElements.size();
	}		
}
