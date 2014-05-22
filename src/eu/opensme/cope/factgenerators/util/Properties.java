/**
 * 
 */
package eu.opensme.cope.factgenerators.util;

import eu.opensme.cope.domain.ReuseProject;
import java.io.File;

/**
 * @author krap
 *
 */
public class Properties {
	
	public final static String LUCENE_DIRECTORY = 
                ReuseProject.getReuseProjectsLocation()+File.separator+"luceneIndexDir";

}
