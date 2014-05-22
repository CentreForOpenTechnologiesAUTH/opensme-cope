package eu.opensme.cope.componentvalidator.core.ProMParser;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Data extends Element{

	private String value;
	private int line;
	
	public Data() {
		this.value = "";
		this.line = 0;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
}
