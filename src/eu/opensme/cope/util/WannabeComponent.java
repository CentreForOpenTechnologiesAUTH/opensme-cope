/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.util;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import java.util.List;

/**
 *
 * @author Apostolos Kritikos <akritiko@csd.auth.gr>
 */
public class WannabeComponent {
    
    String type;  //'abstract' or 'specific'
    List<ClassAnalysis> classes;
    ClassAnalysis centerClass;

    public WannabeComponent(String type, List<ClassAnalysis> classes, ClassAnalysis centerClass) {
        this.type = type;
        this.classes = classes;
        this.centerClass = centerClass;
    }

    public ClassAnalysis getCenterClass() {
        return centerClass;
    }

    public void setCenterClass(ClassAnalysis centerClass) {
        this.centerClass = centerClass;
    }

    public List<ClassAnalysis> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassAnalysis> classes) {
        this.classes = classes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
