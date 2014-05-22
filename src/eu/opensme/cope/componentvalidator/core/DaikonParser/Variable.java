package eu.opensme.cope.componentvalidator.core.DaikonParser;

import eu.opensme.cope.componentvalidator.core.Util.Element;

public class Variable extends Element{

	private String varKind;
	private String decType;
	private String repType;
	private String comparability;
	
	public Variable() {

	}

	public String getVarKind() {
		return varKind;
	}

	public void setVarKind(String varKind) {
		this.varKind = varKind;
	}

	public String getDecType() {
		return decType;
	}

	public void setDecType(String decType) {
		if (decType.equals("boolean"))
			this.decType = "boolean";
		else if (decType.equals("int"))
			this.decType = "int";
		else if (decType.equals("java.lang.String"))
			this.decType = "java.lang.String";
		else if (decType.equals("java.util.List"))
			this.decType = "java.util.List";
		else if (decType.equals("java.util.Collection"))
			this.decType = "java.util.Collection";
		else if (decType.equals("java.util.ArrayList"))
			this.decType = "java.util.ArrayList";
		else if (decType.equals("java.util.Vector"))
			this.decType = "java.util.Vector";
		else if (decType.equals("java.util.Calendar"))
			this.decType = "java.util.Calendar";                
//		else if (decType.equals("java.util.Map"))
//			this.decType = "java.util.Map";
		else if (decType.equals("java.util.Date"))
			this.decType = "java.util.Date";		
		else 
			this.decType = "java.lang.Object";
	}

	public String getRepType() {
		return repType;
	}

	public void setRepType(String value) {
		value = value.toUpperCase();
		
		if (value.equals("TRUE") || value.equals("FALSE"))
			this.repType = "boolean";
		else if (value.startsWith("[") && value.endsWith("]"))
			this.repType = "java.lang.String[]";
//		else if (value.startsWith("{") && value.endsWith("}"))
//			this.repType = "java.util.Map[]";
//		else if(this.decType.equals("java.lang.Object"))
//			this.repType = "java.lang.Object";
		else 
			this.repType = "java.lang.String";
		
		try {
			String val[] = value.split("@");
			Integer.valueOf(val[1], 16);
			this.repType = "hashcode";
		} catch (Exception e) {
		}

		try {
			if (this.decType.equals("java.util.List"))
				this.repType = "java.lang.String[]";
			else if (this.decType.equals("java.util.Collection"))
				this.repType = "java.lang.String[]";
			else if (this.decType.equals("java.util.Vector"))
				this.repType = "hashcode";
			else if (this.decType.equals("java.util.Calendar"))
				this.repType = "hashcode";
		} catch (Exception e) {
		}
		
	}

	public String getComparability() {
		return comparability;
	}

	public void setComparability(String comparability) {
		this.comparability = comparability;
	}
}
