/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.opensme.cope.recommenders;

import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.recommenders.entities.PackageAnalysis;

/**
 *
 * @author george
 */
public interface ReuseQualityModel {
    public abstract double getClassReusability(ClassAnalysis classsAnalysis);
    public abstract double getPackageReusability(PackageAnalysis packageAnalysis);
}
