/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.opensme.cope.recommenders;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;

/**
 *
 * @author george
 */
public class ClassReusabilityUnit extends ReusabilityUnit {
    /**
     * A ClassReusabilityUnit is a container for a class that provides
     * the required functionality so that the class can be treated as a reusability
     * unit.
     */
    private ClassAnalysis classAnalysis;
    private ReuseQualityModel reuseQualityModel;

    public ClassReusabilityUnit(ReuseQualityModel model) {
        this.reuseQualityModel = model;
    }

    @Override
    public double assessReusability() {
        return reuseQualityModel.getClassReusability(classAnalysis);
    }


}
