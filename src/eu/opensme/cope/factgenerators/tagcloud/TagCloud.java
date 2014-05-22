/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators.tagcloud;

import eu.opensme.cope.factgenerators.lsa.IterFactory;
import java.io.File;
import java.util.Iterator;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

/**
 *
 * @author econst
 */
public class TagCloud {
    private Cloud cloud = new Cloud();
    
    public TagCloud(){
        cloud.setMaxWeight(1);
        cloud.setMaxTagsToDisplay(Integer.MAX_VALUE);
    }
    
    //Add document
    public void processDocument(String text){
        Iterator<String> documentTokens = IterFactory.tokenize(text);
        while (documentTokens.hasNext()) {
            String word = documentTokens.next();
            
            // Skip added empty tokens for words that have been filtered out
            if (word.equals(IterFactory.EMPTY_TOKEN))
                continue;
            
                    Tag tag = new Tag(word);   // creates a tag
                    cloud.addTag(tag);       // adds it to the cloud
        }        
    }
    
    //Return cloud
    public Cloud getCloud(){
        return cloud;
    }
}
