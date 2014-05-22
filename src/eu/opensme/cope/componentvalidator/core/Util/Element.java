package eu.opensme.cope.componentvalidator.core.Util;

public class Element {

	private String descr;
	private boolean printable;
	
	public Element() {
		this.descr = "";
		this.printable = false;
	}
	
	public String getDescr() {
		return descr;
	}
	
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	public boolean getPrintable() {
		return printable;
	}
	
	public void setPrintable(boolean printable) {
		this.printable = printable;
	}
}
