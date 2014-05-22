/**
 * 
 */
package eu.opensme.cope.factgenerators.util;

import java.util.Vector;

/**
 * @author krap
 * 
 */
public class SourceFileInfo {

	private String projectID;
        private String fullName;
	private Vector<String> className = new Vector<String>(0);
	private Vector<String> methodNames = new Vector<String>(0);
	private Vector<String> attributeNames = new Vector<String>(0);
	private Vector<String> comments = new Vector<String>(0);

	public SourceFileInfo() {

	}

	/**
	 * @param className
	 * @param methodNames
	 * @param attributeNames
	 * @param comments
	 */
	public SourceFileInfo(Vector<String> className, Vector<String> methodNames,
			Vector<String> attributeNames, Vector<String> comments, String projectID) {
		super();
		this.className = className;
		this.methodNames = methodNames;
		this.attributeNames = attributeNames;
		this.comments = comments;
		this.projectID = projectID;
	}

	/**
	 * @return the projectID
	 */
	public String getProjectID() {
		return projectID;
	}

	/**
	 * @param projectID
	 *            the projectID to set
	 */
	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}

        public void setFullClassName(String full){
            this.fullName = full;
        }
        
        public String getFullName(){
            return fullName;
        }
        
	/**
	 * @return the className
	 */
	public Vector<String> getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(Vector<String> className) {
		this.className = className;
	}

        public void addToClassName(String t){
            className.addElement(t);
        }
        
	/**
	 * @return the methodNames
	 */
	public Vector<String> getMethodNames() {
		return methodNames;
	}

	/**
	 * @param methodNames
	 *            the methodNames to set
	 */
	public void setMethodNames(Vector<String> methodNames) {
		this.methodNames = methodNames;
	}

	/**
	 * @return the comments
	 */
	public Vector<String> getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(Vector<String> comments) {
		this.comments = comments;
	}

	public Vector<String> getAttributeNames() {
		return attributeNames;
	}

	public void setAttributeNames(Vector<String> attributeNames) {
		this.attributeNames = attributeNames;
	}

	public String getFullText() {
		return this.attributeNames.toString() + " " + this.methodNames.toString() + " "
				+ this.comments;
	}
        
        public void addMethod(String methodName){
            this.methodNames.addElement(methodName);
        }

        public void addField(String fieldName){
            this.attributeNames.addElement(fieldName);
        }        
        
        public void addComment(String com){
            this.comments.addElement(com);
        }
        
	public String toString() {
		return "ProjectID: " + this.projectID + " / Attributes: "
				+ this.attributeNames + " / Methods: " + this.methodNames
				+ " / ClassName: " + this.className + " / Comments: "
				+ this.comments;
	}
}
