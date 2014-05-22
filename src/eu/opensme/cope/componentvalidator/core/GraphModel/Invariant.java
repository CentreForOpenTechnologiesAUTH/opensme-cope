package eu.opensme.cope.componentvalidator.core.GraphModel;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Invariant extends Element{

	private String value;
	private int line;
	private String type;
	private String packageName;
	private String kind;
	
	public Invariant() {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
}
