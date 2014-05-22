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
import eu.opensme.cope.knowledgemanager.api.dto.ArchitecturalPatternDetails;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;

public class Role {

    private ReuseApi api;
    private boolean delay = false;

    public Role(ReuseApi api) {
        this.api = api;
    }

    private void delay(int time) {
        if (delay) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String buildGetRoles() {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdf:type ").append("opensme:Role; \n").
                append("opensme:roleName ?Name. } ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<KeyValue> getRoles() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetRoles();
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

//    public KeyValue addRole(String name) throws RepositoryException {
//        delay(100);
//        RepositoryConnection connection = api.getDatabase().getConnection();
//        ValueFactory f = connection.getValueFactory();
//        String id = api.getUtils().generateUniqueID("role");
//
//        connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "Role"));
//        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "roleName"), f.createLiteral(name));
//
//        connection.commit();
//        return new KeyValue(id, name);
//    }
    public void renameRole(String roleID, String name) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        connection.remove(f.createURI(RESOURCE.URI + roleID), f.createURI(RESOURCE.URI + "roleName"), null);
        connection.add(f.createURI(RESOURCE.URI + roleID), f.createURI(RESOURCE.URI + "roleName"), f.createLiteral(name));

        connection.commit();
    }

    public void removeRole(String roleID) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        connection.remove(f.createURI(RESOURCE.URI + roleID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI + roleID));
        connection.commit();
    }

    private String biuldGetArchitecturalPattern(String roleID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID opensme:hasRole opensme:").append(roleID).append("; \n").
                append("opensme:architecturalPatternName ?Name. } \n");
        return query.toString();
    }

    public ArchitecturalPatternDetails getQualityAttributes(String roleID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = biuldGetArchitecturalPattern(roleID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        String patternID = null;
        String patternName = null;
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                patternID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                patternName = it1.next().getValue().stringValue();
            }
        }
        evaluate.close();

        if (patternID == null) {
            return null;
        }

        ArchitecturalPattern pattern = new ArchitecturalPattern(api);
        ArchitecturalPatternDetails patternDetails = pattern.getPatternDetails(patternID);
        patternDetails.setPatternName(patternName);
        return patternDetails;
    }
}
