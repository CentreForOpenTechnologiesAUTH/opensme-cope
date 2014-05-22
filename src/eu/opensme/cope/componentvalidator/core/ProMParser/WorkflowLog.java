package eu.opensme.cope.componentvalidator.core.ProMParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.opensme.cope.componentvalidator.core.Util.Element;
import eu.opensme.cope.componentvalidator.core.Util.EscapeChars;


public class WorkflowLog extends Element{

	private String sourceProgram="Log Data for OPEN-SME";
	private List<Process> processes; 
	
	public WorkflowLog() {
		this.processes = new ArrayList<Process>();
		setDescr("Log Data");
	}

	public String getSourceProgram() {
		return sourceProgram;
	}

	public void setSourceProgram(String sourceProgram) {
		this.sourceProgram = sourceProgram;
	}

	public List<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(List<Process> processes) {
		this.processes = processes;
	}
	
	public void addProcess(Process p) {
		p.setId(this.processes.size() +1);
		this.processes.add(p);
	}

	public Process getProcess(int index) {
		return this.processes.get(index);
	}
	
	public Process getProcessByDescr(String descr) {
		for (int i = 0; i < this.processes.size(); i++) {
			if (this.getProcess(i).getDescr().equals(descr)){
				return getProcess(i);
			}
		}
		return null;
	}
	
	public Process getProcessById(int id) {
		for (int i = 0; i < this.processes.size(); i++) {
			if (this.getProcess(i).getId() == id){
				return getProcess(i);
			}
		}
		return null;
	}
	
	public int getProcessesSize() {
		return this.processes.size();
	}	
	
	public void ProMExtract(String filename, HashMap<String, String> statesHashMap, HashMap<String, String> variablesHashMap){
		String BR = System.getProperty("line.separator");	

		BufferedWriter stream = setupStream(filename);
		
		try {
			stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +BR);
			stream.write("<WorkflowLog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"WorkflowLog.xsd\" description=\""+this.getDescr()+"\">" + BR);
			stream.write("<Source program=\""+this.sourceProgram +"\"/>" + BR);
		    stream.flush();
		} catch (IOException e) {
		}	    
		
		for (int i = 0; i < this.getProcessesSize(); i++) {
			if(this.processes.get(i).getPrintable()){
				try {
					stream.write("  <Process id=\"" + this.processes.get(i).getId() + "\" description=\""+this.processes.get(i).getDescr()+"\">" +BR);
				    stream.flush();
				} catch (IOException e) {
				}	    
				for (int j = 0; j < this.processes.get(i).getProcessInstancesSize(); j++) {
					if(this.processes.get(i).getProcessInstance(j).getPrintable()){
						try {
							stream.write("    <ProcessInstance id=\"" + this.processes.get(i).getProcessInstance(j).getId() + "\" description=\"" + this.processes.get(i).getProcessInstance(j).getDescr()+ "\">" +BR);
						    stream.flush();
						} catch (IOException e) {
						}	    
						for (int j2 = 0; j2 < this.processes.get(i).getProcessInstance(j).getAuditTrailEntriesSize(); j2++) {
							if(this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getPrintable()){
								try {
									stream.write("      <AuditTrailEntry>"+BR);
								    stream.flush();
								} catch (IOException e) {
								}	    
								for (int k = 0; k < this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElementsSize(); k++) {
									if (this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getPrintable()){
										try {
											stream.write("        <WorkflowModelElement>" + statesHashMap.get(this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getDescr())+ "</WorkflowModelElement>"+ BR);
											stream.write("          <EventType>complete</EventType>" +BR);
											stream.write("          <Data>"+BR);
										    stream.flush();
										} catch (IOException e) {
										}	    
										for (int k2 = 0; k2 < this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getDataSize(); k2++) {
											if (this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getData(k2).getPrintable()){
												try {
													stream.write("            <Attribute name=\"" + variablesHashMap.get(this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getData(k2).getDescr()) + "\">" + EscapeChars.forXML(this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getData(k2).getValue())+ "</Attribute>"+ BR);
												    stream.flush();
												} catch (IOException e) {
												}	    
											}
										}
										try {
											stream.write("          </Data>"+BR);
											stream.write("          <Timestamp>" +this.processes.get(i).getProcessInstance(j).getAuditTrailEntry(j2).getWorkflowElement(k).getTime()+ "</Timestamp>" +BR);											
											stream.flush();
										} catch (IOException e) {
										}
									}
								}
								try {
									stream.write("      </AuditTrailEntry>" +BR);
									stream.flush();
								} catch (IOException e) {
								}
							}
						}
						try {
							stream.write("    </ProcessInstance>" +BR);
							stream.flush();
						} catch (IOException e) {
						}
					}
				}
				try {
					stream.write("  </Process>" + BR);
					stream.flush();
				} catch (IOException e) {
				}	    
			}
		}
		try {
			stream.write("</WorkflowLog>" +BR);
		    stream.flush();
		} catch (IOException e) {
		}	    
		
	}
	
	private BufferedWriter setupStream(String filename) {
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(filename);
		} catch (IOException e) {
		}
	    return new BufferedWriter(fstream);
	}

	public WorkflowLog readFile(WorkflowLog wfl, String filename){
		Process p = new Process();
		p.setDescr(filename);
		wfl.addProcess(p);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try {
				wfl = readTraces(wfl,br);
			}
			finally {
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return wfl;
	}
	
	private WorkflowLog readTraces(WorkflowLog wfl,BufferedReader br) throws IOException {
		String line = "";
		int count = 0;
		
		while ((line = br.readLine()) != null) {
			count++;			
//			System.out.println(count + " " +line);
			
			wfl = parseTraces(wfl,line);
		}
		
		return wfl; 
	}
	
	private WorkflowLog parseTraces(WorkflowLog wfl, String l){
		String[] trace = l.split(" -> ");

		int pIndex = wfl.getProcessesSize()-1;
		int piIndex = -1;
		int ateIndex = -1;
		
		try {
			piIndex = wfl.getProcess(pIndex).getProcessInstancesSize()-1;
			try {
				ateIndex = wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntriesSize()-1;
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}

		if (trace[0].startsWith("@")){
			ProcessInstance pi = new ProcessInstance();
			pi.setDescr(trace[1]);
			wfl.getProcess(pIndex).addProcessInstance(pi);
			piIndex = wfl.getProcess(pIndex).getProcessInstancesSize()-1;
		}
		
		if(piIndex==-1){
			ProcessInstance pi = new ProcessInstance();
			pi.setDescr("Instantiation()");
			
			wfl.getProcess(pIndex).addProcessInstance(pi);
			piIndex = wfl.getProcess(pIndex).getProcessInstancesSize()-1;
		}

		if((trace[0].startsWith("f")) && (trace[0].substring(1, trace[0].length()).equals("1"))){
			AuditTrailEntry ate = new AuditTrailEntry();
			ate.setDescr(trace[1]);

			WorkflowElement we = new WorkflowElement();
			we.setDescr(trace[1]);
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			we.setPointcut(pointcut);
			we.setDeclaringTypeName(trace[2]);
			we.setTime(trace[3]);
			
			ate.addWorkflowElement(we);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).addAuditTrailEntry(ate);
			
		}

		if((trace[0].startsWith("f")) && !(trace[0].substring(1, trace[0].length()).equals("1"))){
			
			WorkflowElement we = new WorkflowElement();
			we.setDescr(trace[1]);
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			we.setPointcut(pointcut);
			we.setDeclaringTypeName(trace[2]);
			we.setTime(trace[3]);
			
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).addWorkflowElement(we);
		}

		if(trace[0].startsWith("s")){

			Data d = new Data();

			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			String declaringTypeName = wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).getDeclaringTypeName();

			if (trace[3].equals(declaringTypeName)){
				d.setDescr(trace[1]);
//				d.setDescr(trace[1].replaceFirst(trace[3], "this"));
			}
			else{
				d.setDescr(trace[1]);
			}
			
			d.setValue(trace[2].replace("<BR>","\n"));
			d.setPrintable(true);
			
			wfl.getProcess(pIndex).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).addData(d);			
		}
		
		if(trace[0].startsWith("g")){

			Data d = new Data();

			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			String declaringTypeName = wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).getDeclaringTypeName();

			if (trace[3].equals(declaringTypeName)){
				d.setDescr(trace[1]);
//				d.setDescr(trace[1].replaceFirst(trace[3], "this"));
			}
			else{
				d.setDescr(trace[1]);
			}
			d.setLine(Integer.valueOf(trace[2]));

			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).addData(d);
		}
		
		if(trace[0].startsWith("c")){

			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			int line = Integer.valueOf(trace[2]);

			wfl.getProcess(pIndex).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).setPrintable(true);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).getLastDataByLine(line).setValue(trace[3]);
			wfl.getProcess(pIndex).getProcessInstance(piIndex).getAuditTrailEntry(ateIndex).getLastWorkflowElementByPointcut(pointcut).getLastDataByLine(line).setPrintable(true);
		}
		
		return wfl;		
	}

}
