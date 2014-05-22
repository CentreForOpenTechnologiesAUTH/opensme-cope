/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers;

import eu.opensme.cope.analyzers.dependencyTypeAnalyzer.DependencyTypeGenerator;
import eu.opensme.cope.domain.ReuseProject;

/**
 *
 * @author auth
 */
public class DependencyTypeAnalyzer extends Analyzer {

    private DependencyTypeGenerator typeGen;

    public DependencyTypeAnalyzer(ReuseProject reuseProject) {
        super(reuseProject);
        typeGen = new DependencyTypeGenerator(reuseProject);
    }

    @Override
    public boolean analyze() {

        typeGen.findDependencies();
        return true;
    }

    @Override
    public void storeData() {

        typeGen.storeData();


    }
}
