package eu.opensme.cope.componentvalidator.core.DaikonParser;

public class TransitionPpt extends Ppt{

	private String fromNode;
	private String toNode;
	
	public TransitionPpt() {
		this.fromNode = null;
		this.toNode = null;
	}

	public String getFromNode() {
		return fromNode;
	}

	public void setFromNode(String fromNode) {
		this.fromNode = fromNode;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}
}
