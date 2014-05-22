/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators.lucene;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.util.Properties;
import eu.opensme.cope.recommenders.DependenciesRecommender;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import eu.opensme.cope.util.HibernateUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author econst
 */
public class Searcher {

    private String FIELD_CLASS_FULL_NAME = "class_full_name";
    private String FIELD_CLASS_NAME = "name";
    /** The field of the index for Full Text based queries */
    private String FIELD_FULL_TEXT = "full_text";
    /** The field of the index for Class Javadoc based queries */
    private String FIELD_ATTRIBUTE_NAME = "attribute";
    /** The field of the index for Method Javadoc based queries */
    private String FIELD_METHOD_NAME = "method";
    /** The field of the index for the rest Comments */
    private String FIELD_COMMENT = "comment";
    private IndexSearcher is;
    private String indexDirectory;
    private Directory toIndexDirectory;
    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    private final int HITS_PER_PAGE = 1000;
    private Vector<Result> results;
    ScoreDoc[] hits;
    private boolean indexExists = false;
    
    public Searcher(String toIndexDir) {
        try {
            indexDirectory = toIndexDir+"/index";
            toIndexDirectory = FSDirectory.open(new File(indexDirectory));
            is = new IndexSearcher(toIndexDirectory);
            indexExists = true;
        } catch (IOException ex) {
            //Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean search(String queryString, String field,boolean fuzzy,boolean andSelectedMultTerm) {
        if (!indexExists)
            return false;
        
        results = new Vector<Result>(0);
        try {
            QueryParser queryParser = null;
            if (field.equals(FIELD_CLASS_NAME)) {
                queryParser = new QueryParser(Version.LUCENE_30,
                        FIELD_CLASS_NAME, analyzer);
            } else if (field.equals(FIELD_FULL_TEXT)) {
                queryParser = new QueryParser(Version.LUCENE_30,
                        FIELD_FULL_TEXT, analyzer);
            } else if (field.equals(FIELD_ATTRIBUTE_NAME)) {
                queryParser = new QueryParser(Version.LUCENE_30,
                        FIELD_ATTRIBUTE_NAME, analyzer);
            } else if (field.equals(FIELD_METHOD_NAME)) {
                queryParser = new QueryParser(Version.LUCENE_30,
                        FIELD_METHOD_NAME, analyzer);
            } else if (field.equals(FIELD_COMMENT)) {
                queryParser = new QueryParser(Version.LUCENE_30,
                        FIELD_COMMENT, analyzer);
            }
            //Query query = queryParser.parse(queryString);
            Query query = null;
            
            if (fuzzy){
                //not supported
                if (queryString.contains(" ")) {
	            return false;
                }
            }else{
                if (queryString.contains(" ")) {
                                        if (andSelectedMultTerm) {
                        queryString = queryString.replaceAll(" ", " AND ");
                    } else {
                        queryString = queryString.replaceAll(" ", " OR ");
                    }   
                }            
            }

            if (fuzzy){
                query = new FuzzyQuery(new Term(field,queryString));
            }else{ 
                try {
                    query = queryParser.parse(queryString);
                } catch (ParseException ex) {
                    Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (query==null)
                return false;
            TopScoreDocCollector collector = TopScoreDocCollector.create(
                    HITS_PER_PAGE, true);
            is.search(query, collector);
            hits = collector.topDocs().scoreDocs;
            for (int i = 0; i < hits.length; i++) {
                results.addElement(new Result(is.doc(hits[i].doc).getField(FIELD_CLASS_FULL_NAME).stringValue(), hits[i].score));
            }
        } catch (IOException ex) {
            Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
        } //catch (ParseException ex) {
     //       Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
      //  }
        return true;
    }

    public Vector<ClassAnalysis> getClassesResultsSortedDescByScore(long projectid) {
        Collections.sort(results, new Comparator() {

            public int compare(Object a, Object b) {
                return (new Float(((Result) a).getScore())).compareTo(new Float(((Result) b).getScore()))*-1;
            }
        });
        Vector<ClassAnalysis> temp = new Vector<ClassAnalysis>(0);
        for (int i=0;i<results.size();i++)
            temp.addElement(classNameToClassAnalysis(results.elementAt(i).getFullClassName(),projectid));
        return temp;
    }
    
    public static ClassAnalysis classNameToClassAnalysis(String className,long projectid) {
        //now look for the id of this class in the database
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        org.hibernate.Query query = session.createQuery("from ClassAnalysis where name=:name and projectid="+projectid);
        query.setParameter("name", className);
        ClassAnalysis ca = new ClassAnalysis();
        ca = (ClassAnalysis) query.uniqueResult();
        session.close();
        return ca;                
    }  
}
