package eu.opensme.cope.componentvalidator.core.ProMParser;

import java.util.ArrayList;
import java.util.List;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class WorkflowElement extends Element{

	private int pointcut;
	private String declaringTypeName;
	private String time;	
	private List<Data> data;
	
	public WorkflowElement() {
		this.pointcut = 0;
		this.declaringTypeName = "";
		this.data = new ArrayList<Data>();
	}

	public int getPointcut() {
		return pointcut;
	}

	public void setPointcut(int pointcut) {
		this.pointcut = pointcut;
	}

	public String getDeclaringTypeName() {
		return declaringTypeName;
	}

	public void setDeclaringTypeName(String declaringTypeName) {
		this.declaringTypeName = declaringTypeName;
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}
	
	public void addData(Data d) {
		this.data.add(d);
	}

	public Data getData(int index) {
		return this.data.get(index);
	}
	
	public Data getDataByDescr(String descr) {
		for (int i = 0; i < this.data.size(); i++) {
			if (this.getData(i).getDescr().equals(descr)){
				return this.getData(i);
			}
		}
		return null;
	}

	public Data getFirstDataByLine(int line) {
		for (int i = 0; i < this.data.size(); i++) {
			if (this.getData(i).getLine() == line){
				return this.getData(i);
			}
		}
		return null;
	}
	
	public Data getLastDataByLine(int line) {
		Data d = null;
		for (int i = 0; i < this.data.size(); i++) {
			if (this.getData(i).getLine() == line){
				d = this.getData(i);
			}
		}
		return d;
	}

	public int getDataSize() {
		return this.data.size();
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}	
}
