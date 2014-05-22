package eu.opensme.cope.componentvalidator.core.ProMParser;

import java.util.ArrayList;
import java.util.List;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class ProcessInstance extends Element{

	private int id=0;
	private List<AuditTrailEntry> auditTrailEntries; 
	
	public ProcessInstance() {
		this.id++;
		this.auditTrailEntries = new ArrayList<AuditTrailEntry>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<AuditTrailEntry> getAuditTrailEntries() {
		return auditTrailEntries;
	}

	public void setAuditTrailEntries(List<AuditTrailEntry> auditTrailEntries) {
		this.auditTrailEntries = auditTrailEntries;
	}
	
	public void addAuditTrailEntry(AuditTrailEntry ate) {
		this.auditTrailEntries.add(ate);
	}

	public AuditTrailEntry getAuditTrailEntry(int index) {
		return this.auditTrailEntries.get(index);
	}
	
	public AuditTrailEntry getAuditTrailEntryByDescr(String descr) {
		for (int i = 0; i < this.auditTrailEntries.size(); i++) {
			if (this.getAuditTrailEntry(i).getDescr().equals(descr)){
				return this.getAuditTrailEntry(i);
			}
		}
		return null;
	}

	public int getAuditTrailEntriesSize() {
		return this.auditTrailEntries.size();
	}		
}
