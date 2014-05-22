/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.opensme.cope.recommenders;

import eu.opensme.cope.recommenders.entities.PackageAnalysis;

/**
 *
 * @author george
 */
public class PackageReusabilityUnit extends ReusabilityUnit {
    /**
     * A PackageReusabilityUnit is a container for a package that provides
     * the required functionality so that the package can be treated as a reusability
     * unit.
     */
    private PackageAnalysis packageAnalysis;
    private ReuseQualityModel reuseQualityModel;

    public PackageReusabilityUnit(ReuseQualityModel model) {
        this.reuseQualityModel = model;
    }

    @Override
    public double assessReusability() {
        return reuseQualityModel.getPackageReusability(packageAnalysis);
    }
}
