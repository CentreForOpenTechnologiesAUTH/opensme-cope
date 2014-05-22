package eu.opensme.cope.componentvalidator.core.DaikonParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import eu.opensme.cope.componentvalidator.core.Util.FormatString;

import eu.opensme.cope.componentvalidator.core.GraphModel.Invariant;
import eu.opensme.cope.componentvalidator.core.GraphModel.Node;
import eu.opensme.cope.componentvalidator.core.GraphModel.Transition;

public class DaikonModel {
	
	private ArrayList<TransitionPpt> tPpts;
	private ArrayList<StatePpt> sPpts;
	private ArrayList<Node> nodes;
	private ArrayList<Transition> transitions;
	private HashMap<String,String> statesHashMap;
	private HashMap<String,String> variablesHashMap;
	
	public DaikonModel() {
		this.tPpts = new ArrayList<TransitionPpt>();
		this.sPpts = new ArrayList<StatePpt>();
		this.nodes = new ArrayList<Node>();
		this.transitions = new ArrayList<Transition>();
		this.statesHashMap = new HashMap<String, String>();
		this.variablesHashMap = new HashMap<String, String>();
	}
	
	public ArrayList<TransitionPpt> gettPpts() {
		return tPpts;
	}

	public void settPpts(ArrayList<TransitionPpt> tPpts) {
		this.tPpts = tPpts;
	}

	public void addTPpt(TransitionPpt tPpt) {
		this.tPpts.add(tPpt);
	}

	public int getTPptsSize() {
		return this.tPpts.size();
	}

	public TransitionPpt getTPpt(int index) {
		return this.tPpts.get(index);
	}	

	public ArrayList<StatePpt> getsPpts() {
		return sPpts;
	}

	public void setsPpts(ArrayList<StatePpt> sPpts) {
		this.sPpts = sPpts;
	}

	public void addSPpt(StatePpt sPpt) {
		this.sPpts.add(sPpt);
	}

	public int getSPptsSize() {
		return this.sPpts.size();
	}

	public StatePpt getSPpt(int index) {
		return this.sPpts.get(index);
	}	
	
	public HashMap<String, String> getStatesHashMap() {
		return statesHashMap;
	}

	public void setStatesHashMap(HashMap<String, String> statesHashMap) {
		this.statesHashMap = statesHashMap;
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	public HashMap<String, String> getVariablesHashMap() {
		return variablesHashMap;
	}

	public void setVariablesHashMap(HashMap<String, String> variablesHashMap) {
		this.variablesHashMap = variablesHashMap;
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

	public int getIndexOfNode(Node n) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).equals(n)){
				return i;
			}
		}
		return -1;
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

	public TransitionPpt getTPptByNodeDescriptions(String fromNodeDescr, String toNodeDescr) {
		for (int i = 0; i < this.getTPptsSize(); i++) {
			if (this.getTPpt(i).getFromNode().equals(fromNodeDescr) && 
					this.getTPpt(i).getToNode().equals(toNodeDescr)){
				return this.getTPpt(i);
			}
		}
		return null;
	}

	public StatePpt getSPptByNodeDescriptions(String nodeDescr) {
		for (int i = 0; i < this.getSPptsSize(); i++) {
			if (this.getSPpt(i).getNode().equals(nodeDescr)){
				return this.getSPpt(i);
			}
		}
		return null;
	}

	public int getIndexOfNodeDescription(String descr) {
		int index = -1;
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).getDescr().equals(descr)){
				index = i;
			}
		}
		return index;
	}	
	
	public int getIndexOfNodeId(int id) {
		int index = -1;
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.getNode(i).getId() == id){
				index = i;
			}
		}
		return index;
	}		
	
	private BufferedWriter setupStream(String filename) {
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(filename);
		} catch (IOException e) {
		}
	    return new BufferedWriter(fstream);
	}	
	
	public void DaikonExtract(String filename){
		String BR = System.getProperty("line.separator");	

		BufferedWriter stream = setupStream(filename);
		
		try {
			stream.write("decl-version 2.0"+ BR);
			stream.write("var-comparability implicit" + BR);
			stream.flush();
		} catch (Exception e) {
		}

		for (int j = 0; j < this.getSPptsSize(); j++) {
			try {
				stream.write(BR);
				
				String[] checkExit = this.getSPpt(j).getNode().split("_EXIT");
				
				String pptName = this.getSPpt(j).getNode();
					
				if (checkExit.length == 1){
					pptName = checkExit[0]; 
				}
				
				pptName = this.getStatesHashMap().get(pptName);
				
				if (checkExit.length == 1 && this.getSPpt(j).getNode().endsWith("_EXIT")){
					pptName = pptName + "_EXIT"; 
				}

				stream.write("ppt " + pptName + ":::ENTER" + BR);
				stream.write("ppt-type point"+ BR);
				stream.flush();
				
			} catch (Exception e) {
			}
			for (int j2 = 0; j2 < this.getSPpt(j).getVariablesSize(); j2++) {
				try {
					String variableName = this.getVariablesHashMap().get(this.getSPpt(j).getVariable(j2).getDescr());
					if (variableName == null)
						variableName = this.getSPpt(j).getVariable(j2).getDescr();
					if (this.getSPpt(j).getVariable(j2).getDecType().equals("java.util.List") 
							|| this.getSPpt(j).getVariable(j2).getDecType().equals("java.util.Collection")
							|| this.getSPpt(j).getVariable(j2).getDecType().equals("java.util.ArrayList")){
						stream.write("  variable " + variableName + "[]" + BR);
					}
					else{
						stream.write("  variable " + variableName + BR);
					}
					stream.write("    var-kind " + this.getSPpt(j).getVariable(j2).getVarKind() + BR);
					
					if (this.getSPpt(j).getVariable(j2).getDecType().equals("java.util.List")){
						stream.write("    dec-type java.lang.Class[]" + BR);
					}
					else{
						stream.write("    dec-type " + this.getSPpt(j).getVariable(j2).getDecType() + BR);
					}

					stream.write("    rep-type " + this.getSPpt(j).getVariable(j2).getRepType() + BR);
					stream.write("    comparability " + this.getSPpt(j).getVariable(j2).getComparability() + BR);
					stream.flush();
				} catch (Exception e) {
				}
			}
		}		
/*
		for (int j = 0; j < this.getTPptsSize(); j++) {
			try {
				stream.write(BR);
				stream.write("ppt " + this.getStatesHashMap().get(this.getTPpt(j).getFromNode()) + "," + this.getStatesHashMap().get(this.getTPpt(j).getToNode()) + ":::ENTER" + BR);
				stream.write("ppt-type point"+ BR);
				stream.flush();
				
			} catch (Exception e) {
			}
			
			for (int j2 = 0; j2 < this.getTPpt(j).getVariablesSize(); j2++) {
				try {
					if (this.getTPpt(j).getVariable(j2).getDecType().equals("java.util.List") 
							|| this.getTPpt(j).getVariable(j2).getDecType().equals("java.util.Collection")
							|| this.getTPpt(j).getVariable(j2).getDecType().equals("java.util.ArrayList")){
						stream.write("  variable " + this.getVariablesHashMap().get(this.getTPpt(j).getVariable(j2).getDescr()) + "[]" + BR);
					}
					else{
						stream.write("  variable " + this.getVariablesHashMap().get(this.getTPpt(j).getVariable(j2).getDescr()) + BR);
					}
					stream.write("    var-kind " + this.getTPpt(j).getVariable(j2).getVarKind() + BR);
					
					if (this.getTPpt(j).getVariable(j2).getDecType().equals("java.util.List")){
						stream.write("    dec-type java.lang.Class[]" + BR);
					}
					else{
						stream.write("    dec-type " + this.getTPpt(j).getVariable(j2).getDecType() + BR);
					}

					stream.write("    rep-type " + this.getTPpt(j).getVariable(j2).getRepType() + BR);
					stream.write("    comparability " + this.getTPpt(j).getVariable(j2).getComparability() + BR);
					stream.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
*/
		for (int j = 0; j < this.getNodeSize(); j++) {
			try {
				stream.write(BR);

				String[] checkExit = this.getNode(j).getDescr().split("_EXIT");
				
				String pptName = this.getNode(j).getDescr();
					
				if (checkExit.length == 1){
					pptName = checkExit[0]; 
				}
				
				pptName = this.getStatesHashMap().get(pptName);
				
				if (checkExit.length == 1 && this.getNode(j).getDescr().endsWith("_EXIT")){
					pptName = pptName + "_EXIT"; 
				}
				
				stream.write(pptName + ":::ENTER" + BR);
				stream.flush();
			} catch (Exception e) {
			}

			StatePpt ppt = getSPptByNodeDescriptions(this.getNode(j).getDescr());
			
			for (int j2 = 0; j2 < ppt.getVariablesSize(); j2++) {
				Invariant inv = this.getNode(j).getInvariantByDescr(ppt.getVariable(j2).getDescr());
				try {
					String variableName = this.getVariablesHashMap().get(inv.getDescr());
					if (variableName == null)
						variableName = inv.getDescr();
					
					if (ppt.getVariable(j2).getDecType().equals("java.util.List") 
							|| ppt.getVariable(j2).getDecType().equals("java.util.Collection")
							|| ppt.getVariable(j2).getDecType().equals("java.util.ArrayList")){
						stream.write(variableName + "[]"+ BR);
					}
					else{
						stream.write(variableName + BR);
					}
					stream.write(inv.getValue() + BR);
					stream.write("1" + BR);	
					stream.flush();
				} catch (Exception e) {
				}
			}
		}
/*
		for (int j = 0; j < this.getTransitionsSize(); j++) {
			try {
				stream.write(BR);
				stream.write(this.getStatesHashMap().get(this.getNode(this.getTransition(j).getFromNode()).getDescr()) + "," + this.getStatesHashMap().get(this.getNode(this.getTransition(j).getToNode()).getDescr()) + ":::ENTER" + BR);
				stream.flush();
			} catch (Exception e) {
			}

			TransitionPpt ppt = getTPptByNodeDescriptions(this.getNode(this.getTransition(j).getFromNode()).getDescr(), this.getNode(this.getTransition(j).getToNode()).getDescr());

			for (int j2 = 0; j2 < ppt.getVariablesSize(); j2++) {
				Invariant inv = this.getTransition(j).getInvariantByDescr(ppt.getVariable(j2).getDescr());
				try {
					if (ppt.getVariable(j2).getDecType().equals("java.util.List") 
							|| ppt.getVariable(j2).getDecType().equals("java.util.Collection")
							|| ppt.getVariable(j2).getDecType().equals("java.util.ArrayList")){
						stream.write(this.getVariablesHashMap().get(inv.getDescr()) + "[]"+ BR);
					}
					else{
						stream.write(this.getVariablesHashMap().get(inv.getDescr()) + BR);
					}
					stream.write(inv.getValue() + BR);
					stream.write("1" + BR);	
					stream.flush();
				} catch (Exception e) {
				}
			}
		}
*/		
	}

	public DaikonModel addToStatesHashMap(DaikonModel dm, String descr) {
		if(!(dm.getStatesHashMap().containsKey(descr))){
			String preFix = descr;
			try {
				preFix = descr.substring(0, descr.indexOf('('));
			} catch (Exception e) {
				System.out.println(descr);
			}
			preFix = preFix.substring(preFix.lastIndexOf('.')+1, preFix.length());
			String posFix = "";
			int con = 0;
			while (dm.getStatesHashMap().containsValue(preFix+posFix)){
				con++;
				posFix = Integer.toString(con);
			}
			dm.getStatesHashMap().put(descr, preFix+posFix);
		}
		return dm;
	}

	public DaikonModel addToVariablesHashMap(DaikonModel dm, String descr) {
		if(!(dm.getVariablesHashMap().containsKey(descr))){
			String preFix = descr.substring(descr.lastIndexOf('.')+1, descr.length());
			String posFix = "?g";
			int con = 0;
			while (dm.getVariablesHashMap().containsValue(preFix+posFix)){
				con++;
				posFix = "?"+Integer.toString(con);
			}
			dm.getVariablesHashMap().put(descr, preFix+posFix);
		}
		return dm;
	}

	public DaikonModel addVariablesToStatePpt(DaikonModel dm, Node n) {
		if (dm.getSPptByNodeDescriptions(n.getDescr()) == null){
			StatePpt ppt = new StatePpt();
			ppt.setNode(n.getDescr());
			
			for (int k = 0; k < n.getInvariantsSize(); k++) {
				if (n.getInvariant(k).getPrintable()){
					if (n.getInvariant(k).getKind().equals("global")){
						dm = dm.addToVariablesHashMap(dm,n.getInvariant(k).getDescr());
					}
					if (ppt.getVariableByDescr(n.getInvariant(k).getDescr())==null){
						Variable dv = new Variable();
						dv.setVarKind("variable");
						dv.setComparability("22");
						dv.setDecType(n.getInvariant(k).getType());
						dv.setDescr(n.getInvariant(k).getDescr());
						dv.setRepType(n.getInvariant(k).getValue());
						ppt.addVariable(dv);
					}
				}
			}
			
			dm.addSPpt(ppt);
		}
		else{
			for (int k = 0; k < n.getInvariantsSize(); k++) {
				if (n.getInvariant(k).getPrintable()){
					if (n.getInvariant(k).getKind().equals("global")){
						dm = dm.addToVariablesHashMap(dm,n.getInvariant(k).getDescr());
					}
					if (dm.getSPptByNodeDescriptions(n.getDescr()).getVariableByDescr(n.getInvariant(k).getDescr())==null){
						Variable dv = new Variable();
						dv.setVarKind("variable");
						dv.setComparability("22");
						dv.setDecType(n.getInvariant(k).getType());
						dv.setDescr(n.getInvariant(k).getDescr());
						dv.setRepType(n.getInvariant(k).getValue());
						dm.getSPptByNodeDescriptions(n.getDescr()).addVariable(dv);
					}
				}
			}
		}

		return dm;
	}
	
	public DaikonModel addVariablesToTransitionPpt(DaikonModel dm, Node fromNode, Node toNode, Transition trans) {
		if (dm.getTPptByNodeDescriptions(fromNode.getDescr(), toNode.getDescr()) == null){
			TransitionPpt ppt = new TransitionPpt();
			ppt.setFromNode(fromNode.getDescr());
			ppt.setToNode(toNode.getDescr());
			
			for (int k = 0; k < trans.getInvariantsSize(); k++) {
				if (trans.getInvariant(k).getPrintable()){
					if (trans.getInvariant(k).getKind().equals("global")){
						dm = dm.addToVariablesHashMap(dm,trans.getInvariant(k).getDescr());
					}
					if (ppt.getVariableByDescr(trans.getInvariant(k).getDescr())==null){
						Variable dv = new Variable();
						dv.setVarKind("variable");
						dv.setComparability("22");
						dv.setDecType(trans.getInvariant(k).getType());
						dv.setDescr(trans.getInvariant(k).getDescr());
						dv.setRepType(trans.getInvariant(k).getValue());
						ppt.addVariable(dv);
					}
				}
			}
			
			dm.addTPpt(ppt);
		}
		else{
			for (int k = 0; k < trans.getInvariantsSize(); k++) {
				if (trans.getInvariant(k).getPrintable()){
					if (trans.getInvariant(k).getKind().equals("global")){
						dm = dm.addToVariablesHashMap(dm,trans.getInvariant(k).getDescr());
					}
					if (dm.getTPptByNodeDescriptions(fromNode.getDescr(), toNode.getDescr()).getVariableByDescr(trans.getInvariant(k).getDescr())==null){
						Variable dv = new Variable();
						dv.setVarKind("variable");
						dv.setComparability("22");
						dv.setDecType(trans.getInvariant(k).getType());
						dv.setDescr(trans.getInvariant(k).getDescr());
						dv.setRepType(trans.getInvariant(k).getValue());
						dm.getTPptByNodeDescriptions(fromNode.getDescr(), toNode.getDescr()).addVariable(dv);
					}
				}
			}
		}
		return dm;
	}

	public DaikonModel addToNodes(DaikonModel dm, Node n) {
		Node node = new Node();
		node.setDescr(n.getDescr());
		node.setId(n.getId());
		for (int k = 0; k < n.getInvariantsSize(); k++) {
			if (n.getInvariant(k).getPrintable()){
				if (node.getInvariantByDescr(n.getInvariant(k).getDescr()) ==null){
					Invariant inv = new Invariant();
					inv.setDescr(n.getInvariant(k).getDescr());
					inv.setPackageName(n.getInvariant(k).getPackageName());
					inv.setType(n.getInvariant(k).getType());
					inv.setKind(n.getInvariant(k).getKind());
					
					StatePpt tempPpt = dm.getSPptByNodeDescriptions(n.getDescr());
					Variable temp_dv = tempPpt.getVariableByDescr(n.getInvariant(k).getDescr());
					String value = FormatString.formatValueToType(n.getInvariant(k).getValue(), temp_dv.getRepType());
					inv.setValue(value);

					node.addInvariant(inv);
				}
				else{
					StatePpt tempPpt = dm.getSPptByNodeDescriptions(n.getDescr());
					Variable temp_dv = tempPpt.getVariableByDescr(n.getInvariant(k).getDescr());
					String value = FormatString.formatValueToType(n.getInvariant(k).getValue(), temp_dv.getRepType());

					node.getInvariantByDescr(n.getInvariant(k).getDescr()).setValue(value);
				}
			}
		}
		
		StatePpt tempPpt = dm.getSPptByNodeDescriptions(n.getDescr());
		if (tempPpt.getVariablesSize()!= node.getInvariantsSize()){
			for (int k = 0; k < tempPpt.getVariablesSize(); k++) {
				if (node.getInvariantByDescr(tempPpt.getVariable(k).getDescr()) == null){
					Invariant inv = new Invariant();
					inv.setDescr(tempPpt.getVariable(k).getDescr());
					inv.setPackageName(tempPpt.getVariable(k).getDescr().substring(0,tempPpt.getVariable(k).getDescr().lastIndexOf('.')));
					inv.setType(tempPpt.getVariable(k).getDecType());
					inv.setKind("global");

					String repType = tempPpt.getVariable(k).getRepType();
					if (repType.equals("java.lang.String")){
						inv.setValue("\"\"");
					}
					else if (repType.equals("hashcode")){
						inv.setValue("0");
					}
					else if (repType.equals("java.lang.String[]")){
						inv.setValue("[null]");
					}
					else{
						inv.setValue("");
					}
					node.addInvariant(inv);							
				}
			}
		}
		
		dm.addNode(node);
		return dm;
	}

	public DaikonModel addToTransitions(DaikonModel dm, Node fromNode,
			Node toNode, Transition t) {

		Transition trans = new Transition();
		trans.setDescr(t.getDescr());
		trans.setFromNode(dm.getIndexOfNodeId(fromNode.getId()));
		trans.setToNode(dm.getIndexOfNodeId(toNode.getId()));
		for (int k = 0; k < t.getInvariantsSize(); k++) {
			if (t.getInvariant(k).getPrintable()){
				if (trans.getInvariantByDescr(t.getInvariant(k).getDescr()) ==null){
					Invariant inv = new Invariant();
					inv.setDescr(t.getInvariant(k).getDescr());
					inv.setPackageName(t.getInvariant(k).getPackageName());
					inv.setType(t.getInvariant(k).getType());
					inv.setKind(t.getInvariant(k).getKind());
					
					TransitionPpt tempPpt = dm.getTPptByNodeDescriptions(fromNode.getDescr(),toNode.getDescr());
					Variable temp_dv = tempPpt.getVariableByDescr(t.getInvariant(k).getDescr());
					String value = FormatString.formatValueToType(t.getInvariant(k).getValue(), temp_dv.getRepType());
					inv.setValue(value);

					trans.addInvariant(inv);
				}
				else{
					TransitionPpt tempPpt = dm.getTPptByNodeDescriptions(fromNode.getDescr(), toNode.getDescr());
					Variable temp_dv = tempPpt.getVariableByDescr(t.getInvariant(k).getDescr());
					String value = FormatString.formatValueToType(t.getInvariant(k).getValue(), temp_dv.getRepType());

					trans.getInvariantByDescr(t.getInvariant(k).getDescr()).setValue(value);
				}
			}
		}
		
		TransitionPpt tempPpt = dm.getTPptByNodeDescriptions(fromNode.getDescr(), toNode.getDescr());
		if (tempPpt.getVariablesSize()!= trans.getInvariantsSize()){
			for (int k = 0; k < tempPpt.getVariablesSize(); k++) {
				if (trans.getInvariantByDescr(tempPpt.getVariable(k).getDescr()) == null){
					Invariant inv = new Invariant();
					inv.setDescr(tempPpt.getVariable(k).getDescr());
					inv.setPackageName(tempPpt.getVariable(k).getDescr().substring(0,tempPpt.getVariable(k).getDescr().lastIndexOf('.')));
					inv.setType(tempPpt.getVariable(k).getDecType());
					inv.setKind("global");

					String repType = tempPpt.getVariable(k).getRepType();
					if (repType.equals("java.lang.String")){
						inv.setValue("\"\"");
					}
					else if (repType.equals("hashcode")){
						inv.setValue("0");
					}
					else if (repType.equals("java.lang.String[]")){
						inv.setValue("[null]");
					}
					else{
						inv.setValue("");
					}
					trans.addInvariant(inv);							
				}
			}
		}
		
		dm.addTransition(trans);

		return dm;
	}

	public DaikonModel addExitToNodes(DaikonModel dm, Node n) {
		Node node = new Node();
		node.setDescr(n.getDescr() + "_EXIT");
		node.setId(-1 - n.getId());
		for (int k = 0; k < n.getReturnInvariantsSize(); k++) {
			if (n.getReturnInvariant(k).getPrintable()){
				if (node.getInvariantByDescr(n.getReturnInvariant(k).getDescr()) ==null){
					Invariant inv = new Invariant();
					inv.setDescr(n.getReturnInvariant(k).getDescr());
					inv.setPackageName(n.getReturnInvariant(k).getPackageName());
					inv.setType(n.getReturnInvariant(k).getType());
					inv.setKind(n.getReturnInvariant(k).getKind());
					
					StatePpt tempPpt = dm.getSPptByNodeDescriptions(n.getDescr() + "_EXIT");
					Variable temp_dv = tempPpt.getVariableByDescr(n.getReturnInvariant(k).getDescr());
					String value = FormatString.formatValueToType(n.getReturnInvariant(k).getValue(), temp_dv.getRepType());
					inv.setValue(value);

					node.addInvariant(inv);
				}
				else{
					StatePpt tempPpt = dm.getSPptByNodeDescriptions(n.getDescr() + "_EXIT");
					Variable temp_dv = tempPpt.getVariableByDescr(n.getReturnInvariant(k).getDescr());
					String value = FormatString.formatValueToType(n.getReturnInvariant(k).getValue(), temp_dv.getRepType());

					node.getInvariantByDescr(n.getReturnInvariant(k).getDescr()).setValue(value);
				}
			}
		}
		
		StatePpt tempPpt = dm.getSPptByNodeDescriptions(n.getDescr() + "_EXIT");
		if (tempPpt.getVariablesSize()!= node.getInvariantsSize()){
			for (int k = 0; k < tempPpt.getVariablesSize(); k++) {
				if (node.getInvariantByDescr(tempPpt.getVariable(k).getDescr()) == null){
					Invariant inv = new Invariant();
					inv.setDescr(tempPpt.getVariable(k).getDescr());

					inv.setPackageName(tempPpt.getVariable(k).getDescr());					
					try {
						inv.setPackageName(tempPpt.getVariable(k).getDescr().substring(0,tempPpt.getVariable(k).getDescr().lastIndexOf('.')));
					} catch (Exception e) {
					}
					
					inv.setType(tempPpt.getVariable(k).getDecType());
					inv.setKind("return");

					String repType = tempPpt.getVariable(k).getRepType();
					if (repType.equals("java.lang.String")){
						inv.setValue("\"\"");
					}
					else if (repType.equals("hashcode")){
						inv.setValue("0");
					}
					else if (repType.equals("java.lang.String[]")){
						inv.setValue("[null]");
					}
					else if (repType.equals("boolean")){
						inv.setValue("false");
					}
					else{
						inv.setValue("");
					}
					node.addInvariant(inv);							
				}
			}
		}
		
		dm.addNode(node);
		return dm;

	}

	public DaikonModel addExitVariablesToStatePpt(DaikonModel dm, Node n) {
		if (dm.getSPptByNodeDescriptions(n.getDescr() + "_EXIT") == null){
			StatePpt ppt = new StatePpt();
			ppt.setNode(n.getDescr() + "_EXIT");
			
			for (int k = 0; k < n.getReturnInvariantsSize(); k++) {
				if (n.getReturnInvariant(k).getPrintable()){
					if (ppt.getVariableByDescr(n.getReturnInvariant(k).getDescr())==null){
						Variable dv = new Variable();
						dv.setVarKind("variable");
						dv.setComparability("22");
						dv.setDecType(n.getReturnInvariant(k).getType());
						dv.setDescr(n.getReturnInvariant(k).getDescr());
						dv.setRepType(n.getReturnInvariant(k).getValue());
						ppt.addVariable(dv);
					}
				}
			}
			
			dm.addSPpt(ppt);
		}
		else{
			for (int k = 0; k < n.getReturnInvariantsSize(); k++) {
				if (n.getReturnInvariant(k).getPrintable()){
					if (dm.getSPptByNodeDescriptions(n.getDescr() + "_EXIT").getVariableByDescr(n.getReturnInvariant(k).getDescr())==null){
						Variable dv = new Variable();
						dv.setVarKind("variable");
						dv.setComparability("22");
						dv.setDecType(n.getReturnInvariant(k).getType());
						dv.setDescr(n.getReturnInvariant(k).getDescr());
						dv.setRepType(n.getReturnInvariant(k).getValue());
						dm.getSPptByNodeDescriptions(n.getDescr() + "_EXIT").addVariable(dv);
					}
				}
			}
		}

		return dm;

	}
	
}
