package eu.opensme.cope.componentvalidator.core.GraphModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import eu.opensme.cope.componentvalidator.core.DaikonParser.DaikonModel;
import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Graphs extends Element{

	private ArrayList<Graph> graphs;
	private int nodeCount;
	
	public Graphs() {
		this.graphs = new ArrayList<Graph>();
		this.nodeCount = 0;
	}

	public ArrayList<Graph> getGraphs() {
		return graphs;
	}

	public void setGraphs(ArrayList<Graph> graphs) {
		this.graphs = graphs;
	}
	
	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	public Graphs readFile(Graphs grs, String filename){
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try {
				grs = readTraces(grs,br);
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
		
		return grs;
	}
	
	private Graphs readTraces(Graphs grs,BufferedReader br) throws IOException {
		String line = "";
		int count = 0;
		
		while ((line = br.readLine()) != null) {
			count++;			
//			System.out.println(count + " " +line);
			
                        grs = parseTraces(grs,line);
		}
		grs = fillTransitionInvariants(grs);
		return grs; 
	}
	
	private Graphs parseTraces(Graphs grs, String l){
		String[] trace = l.split(" -> ");

		int grIndex = grs.getGraphsSize()-1;
		
		if (trace[0].startsWith("@")){
			Graph gr = new Graph();
			gr.setDescr(trace[1]);
			grs.addGraph(gr);
			grIndex = grs.getGraphsSize()-1;
		}
		
		if(grIndex==-1){
			Graph gr = new Graph();
			gr.setDescr("Instantiation()");
			
			grs.addGraph(gr);
			grIndex = grs.getGraphsSize()-1;
		}

		if(trace[0].startsWith("e")){
			if (!trace[1].equals("void")){
				Invariant inv = new Invariant();
				inv.setDescr("return?r");
				inv.setValue(trace[5].replace("<BR>","\n"));
				inv.setPrintable(true);
				inv.setPackageName("");
				inv.setType(trace[1]);
				inv.setKind("return");
				
				int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
				grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).addReturnInvariant(inv);		
			}
		}
		
		if(trace[0].startsWith("f")){
			Node n = new Node();
			n.setDescr(trace[1]);
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			n.setPointcut(pointcut);
			nodeCount++;
			n.setId(nodeCount); 

			try {
				Node nCousin = grs.getGraph(grIndex).getLastNodeByPointcut(pointcut);
				if (nCousin!=null){
					int nCousinIndex = grs.getGraph(grIndex).getLastIndexOfNode(nCousin);
					if (nCousinIndex!=-1){
						Transition tr = new Transition();
						tr.setFromNode(nCousinIndex);
						tr.setToNode(grs.getGraph(grIndex).getNodeSize());
						grs.getGraph(grIndex).addTransition(tr);
					}
				}
			} catch (Exception e) {
			}

			try {
				Node nParent = grs.getGraph(grIndex).getLastNodeByPointcut(pointcut-1);
				if (nParent!=null){
					int nParentIndex = grs.getGraph(grIndex).getLastIndexOfNode(nParent);
					if (nParentIndex!=-1){
						Transition tr = new Transition();
						tr.setFromNode(nParentIndex);
						tr.setToNode(grs.getGraph(grIndex).getNodeSize());
						grs.getGraph(grIndex).addTransition(tr);
					}
				}
			} catch (Exception e) {
			}
			
			grs.getGraph(grIndex).addNode(n);
		}

		if(trace[0].startsWith("o")){

			Invariant inv = new Invariant();
			inv.setDescr(trace[1]);
			inv.setValue(trace[2].replace("<BR>","\n"));
			inv.setPrintable(true);
			inv.setPackageName(trace[3]);
			inv.setType(trace[4]);
			inv.setKind("global");
			
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).addInvariant(inv);
		}

		if(trace[0].startsWith("s")){

			Invariant inv = new Invariant();
			inv.setDescr(trace[1]);
			inv.setValue(trace[2].replace("<BR>","\n"));
			inv.setPrintable(true);
			inv.setPackageName(trace[3]);
			inv.setType(trace[4]);
			inv.setKind("global");
			
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).addPostInvariant(inv);
		}
		
		if(trace[0].startsWith("p")){

			Invariant inv = new Invariant();
			inv.setDescr(trace[1] + "?"+ trace[3]);
			inv.setValue(trace[2].replace("<BR>","\n"));
			inv.setPrintable(true);
			inv.setPackageName("");
			inv.setType(trace[4]);
			inv.setKind("parameter");
			
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).addInvariant(inv);
		}
		
		if(trace[0].startsWith("g")){

			Invariant inv = new Invariant();
			inv.setDescr(trace[1]);
			int line = Integer.valueOf(trace[2]);			
			inv.setLine(line);
			inv.setType(trace[4]);			
			inv.setPrintable(false);
			inv.setValue(trace[5]);
			inv.setKind("global");
			
			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).addInvariant(inv);
		}
		
		if(trace[0].startsWith("c")){

			int pointcut = Integer.valueOf(trace[0].substring(1, trace[0].length()));
			int line = Integer.valueOf(trace[2]);
			
			Invariant inv = new Invariant();
			inv.setDescr(grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).getLastInvariantByLine(line).getDescr());
			inv.setKind(grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).getLastInvariantByLine(line).getKind());
			inv.setLine(grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).getLastInvariantByLine(line).getLine());
			inv.setPackageName(grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).getLastInvariantByLine(line).getPackageName());
			inv.setPrintable(true);
			inv.setType(grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).getLastInvariantByLine(line).getType());
			inv.setValue(trace[3]);
			
			grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).addPostInvariant(inv);
			
			grs.getGraph(grIndex).getLastNodeByPointcut(pointcut).getLastInvariantByLine(line).setPrintable(true);
		}
		
		return grs;		
	}

	private Graph getGraph(int index) {
		return this.graphs.get(index);
	}

	private void addGraph(Graph gr) {
		gr.setId(this.getGraphsSize() +1);
		this.graphs.add(gr);
		
	}

	private int getGraphsSize() {
		return this.graphs.size();
	}

	private Graphs fillTransitionInvariants(Graphs grs){
		for (int i = 0; i < grs.getGraphsSize(); i++) {
			for (int j = 0; j < grs.getGraph(i).getTransitionsSize(); j++) {
				int fromNodeIndx = grs.getGraph(i).getTransition(j).getFromNode();
				int toNodeIndx = grs.getGraph(i).getTransition(j).getToNode();
				
				ArrayList<Invariant> invs = grs.getGraph(i).getNode(fromNodeIndx).getPostInvariants();
				for (int k = 0; k < invs.size(); k++) {
					if (invs.get(k).getPrintable() && invs.get(k).getKind().equals("global")){
						grs.getGraph(i).getTransition(j).addInvariant(invs.get(k));
					}
				}

				invs = grs.getGraph(i).getNode(toNodeIndx).getInvariants();
				for (int k = 0; k < invs.size(); k++) {
					if (invs.get(k).getPrintable() && invs.get(k).getKind().equals("global")){
						grs.getGraph(i).getTransition(j).addInvariant(invs.get(k));
					}
				}
			} 
		}
		
		return grs;
	}
	
	public DaikonModel getDaikonModel(){
		DaikonModel dm = new DaikonModel();
		dm = fillPpts(dm);
		dm = fillData(dm);
		return dm;
	}
	
	private DaikonModel fillData(DaikonModel dm) {
		for (int i = 0; i < this.getGraphsSize(); i++) {
			for (int j = 0; j < this.getGraph(i).getNodeSize(); j++) {
				Node node = this.getGraph(i).getNode(j);
				dm = dm.addToNodes(dm, node);
			}
		}
		
		for (int i = 0; i < this.getGraphsSize(); i++) {
			for (int j = 0; j < this.getGraph(i).getTransitionsSize(); j++) {
				int fromNodeIndx = this.getGraph(i).getTransition(j).getFromNode();
				int toNodeIndx = this.getGraph(i).getTransition(j).getToNode();
				Node fromNode = this.getGraph(i).getNode(fromNodeIndx);
				Node toNode = this.getGraph(i).getNode(toNodeIndx);				
				dm = dm.addToTransitions(dm, fromNode, toNode, this.getGraph(i).getTransition(j));
			}
		}

		for (int i = 0; i < this.getGraphsSize(); i++) {
			for (int j = 0; j < this.getGraph(i).getNodeSize(); j++) {
				Node node = this.getGraph(i).getNode(j);
				dm = dm.addExitToNodes(dm, node);
			}
		}
		return dm;
	}

	private DaikonModel fillPpts(DaikonModel dm){
		for (int i = 0; i < this.getGraphsSize(); i++) {
			for (int j = 0; j < this.getGraph(i).getNodeSize(); j++) {
				Node n = this.getGraph(i).getNode(j);
				dm = dm.addToStatesHashMap(dm,n.getDescr());
				dm = dm.addVariablesToStatePpt(dm, n);
			}
		}
		
		for (int i = 0; i < this.getGraphsSize(); i++) {
			for (int j = 0; j < this.getGraph(i).getTransitionsSize(); j++) {
				int fromNodeIndx = this.getGraph(i).getTransition(j).getFromNode();
				int toNodeIndx = this.getGraph(i).getTransition(j).getToNode();
				Node fromNode = this.getGraph(i).getNode(fromNodeIndx);
				Node toNode = this.getGraph(i).getNode(toNodeIndx);
				dm = dm.addToStatesHashMap(dm,fromNode.getDescr());
				dm = dm.addToStatesHashMap(dm,toNode.getDescr());
				dm = dm.addVariablesToTransitionPpt(dm, fromNode, toNode, this.getGraph(i).getTransition(j));
			}
		}

		for (int i = 0; i < this.getGraphsSize(); i++) {
			for (int j = 0; j < this.getGraph(i).getNodeSize(); j++) {
				Node n = this.getGraph(i).getNode(j);
				dm = dm.addExitVariablesToStatePpt(dm, n);
			}
		}		
		return dm;
	}
	
}
