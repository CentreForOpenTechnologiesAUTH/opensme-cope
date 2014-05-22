package eu.opensme.cope.knowledgemanager.api.actions;

import java.util.ArrayList;
import java.util.Iterator;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import eu.opensme.cope.knowledgemanager.resources.RESOURCE;
import eu.opensme.cope.knowledgemanager.api.ReuseApi;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;

public class Language {

    private ReuseApi api;
    private boolean delay = false;

    public Language(ReuseApi api) {
        this.api = api;
    }

    private void delay(int time) {
        if (delay) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
 
    private String buildGetLanguages() {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdf:type ").append("opensme:Language; \n").
                append("opensme:languageName ?Name. } ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<KeyValue> getLanguages() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetLanguages();
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            KeyValue pair = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String compID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                pair.setKey(compID);
                pair.setValue(it1.next().getValue().stringValue());
                result.add(pair);
            }
        }
        evaluate.close();
        return result;
    }

    public KeyValue addLanguage(String name) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID("lang");

        connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "Language"));
        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "languageName"), f.createLiteral(name));

        connection.commit();
        return new KeyValue(id, name);
    }

    public void removeLanguage(String langID) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        connection.remove(f.createURI(RESOURCE.URI + langID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI + langID));
        connection.commit();
    }

    public void renameLanguage(String langID, String name) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        connection.remove(f.createURI(RESOURCE.URI + langID), f.createURI(RESOURCE.URI + "languageName"), null);
        connection.add(f.createURI(RESOURCE.URI + langID), f.createURI(RESOURCE.URI + "languageName"), f.createLiteral(name));
        
        connection.commit();
    }
    
}
