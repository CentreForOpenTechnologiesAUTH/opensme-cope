/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.recommenders.entities;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author krap
 */
public class ClassClusterPartcipant implements Comparable<ClassClusterPartcipant>, Serializable {

    private ClassAnalysis ca;
    private String role; 
    private double specificity;

    public ClassClusterPartcipant() {
    }

    public ClassClusterPartcipant(ClassAnalysis ca, double specificity, String role) {
        this.ca = ca;
        this.specificity = specificity;
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }
    
    public ClassAnalysis getClassAnalysis() {
        return ca;
    }

    public void setClassAnalysis(ClassAnalysis ca) {
        this.ca = ca;
    }

    public double getSpecificity() {
        return specificity;
    }

    public void setSpecificity(double specificity) {
        this.specificity = specificity;
    }

    public int compareTo(ClassClusterPartcipant o) {
        
        return this.getClassAnalysis().toFileName().compareTo(o.getClassAnalysis().toFileName());
    }
    public static Comparator SpecificityComparator = new Comparator() {

        public int compare(Object classPart, Object anotherClassPart) {
            double specificity1 = ((ClassClusterPartcipant) classPart).getSpecificity();
            double specificity2 = ((ClassClusterPartcipant) anotherClassPart).getSpecificity();

            return (int) Math.signum(specificity1 - specificity2);
        }
    };
}
