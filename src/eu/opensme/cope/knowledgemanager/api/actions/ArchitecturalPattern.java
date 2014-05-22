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

public class ArchitecturalPattern {

    private ReuseApi api;
    private boolean delay = false;

    public ArchitecturalPattern(ReuseApi api) {
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

    private String buildGetArchitecturalPatterns() {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdf:type ").append("opensme:ArchitecturalPattern; \n").
                append("opensme:architecturalPatternName ?Name. } ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<KeyValue> getArchitecturalPatterns() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetArchitecturalPatterns();
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

    public ArchitecturalPatternDetails getPatternDetails(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArchitecturalPatternDetails result = new ArchitecturalPatternDetails();
        result.setHighQuality(getPatternHighQuality(patternID));
        result.setMediumQuality(getPatternMediumQuality(patternID));
        result.setLowQuality(getPatternLowQuality(patternID));
        result.setRoles(getPatternRoles(patternID));

        return result;

    }

    private String buildGetPatternHighQuality(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?QualityID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:ArchitecturalPattern; \n").
                append("opensme:highQuality ?QualityID. \n").
                append("?QualityID opensme:qualityAttributeName ?Name. \n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }

    private ArrayList<KeyValue> getPatternHighQuality(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetPatternHighQuality(patternID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            KeyValue u = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setValue(it1.next().getValue().stringValue());
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetPatternMediumQuality(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?QualityID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:ArchitecturalPattern; \n").
                append("opensme:mediumQuality ?QualityID. \n").
                append("?QualityID opensme:qualityAttributeName ?Name. \n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<KeyValue> getPatternMediumQuality(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetPatternMediumQuality(patternID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            KeyValue u = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setValue(it1.next().getValue().stringValue());
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetPatternLowQuality(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?QualityID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:ArchitecturalPattern; \n").
                append("opensme:lowQuality ?QualityID. \n").
                append("?QualityID opensme:qualityAttributeName ?Name. \n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<KeyValue> getPatternLowQuality(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetPatternLowQuality(patternID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            KeyValue u = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setValue(it1.next().getValue().stringValue());
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetPatternRoles(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?RoleID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:ArchitecturalPattern; \n").
                append("opensme:hasRole ?RoleID. \n").
                append("?RoleID opensme:roleName ?Name. \n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<KeyValue> getPatternRoles(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetPatternRoles(patternID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            KeyValue u = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setValue(it1.next().getValue().stringValue());
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    public KeyValue addArchitecturalPattern(String name) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID("patt");

        connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "ArchitecturalPattern"));
        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "architecturalPatternName"), f.createLiteral(name));
        connection.commit();
        return new KeyValue(id, name);
    }

    public void removePattern(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        //the roles should be also removed...
        ArrayList<KeyValue> patternRoles = getPatternRoles(patternID);
        for (KeyValue keyValue : patternRoles) {
            connection.remove(f.createURI(RESOURCE.URI + keyValue.getKey()), null, null);
            connection.remove((URI) null, null, f.createURI(RESOURCE.URI + keyValue.getKey()));
        }
        
        connection.remove(f.createURI(RESOURCE.URI + patternID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI + patternID));
        
        connection.commit();
    }

    public void renamePattern(String patternID, String name) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        connection.remove(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "architecturalPatternName"), null);
        connection.add(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "architecturalPatternName"), f.createLiteral(name));
        
        connection.commit();
    }

    public void addHighQualityAttributes(String patternID, Object[] highQualities) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : highQualities) {
            KeyValue v1 = (KeyValue) v;
            connection.add(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "highQuality"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }

    public void removeHighQualityAttributes(String patternID, Object[] selectedValues) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : selectedValues) {
            KeyValue v1 = (KeyValue) v;
            connection.remove(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "highQuality"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }

    public void addMediumQualityAttributes(String patternID, Object[] mediumQualities) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : mediumQualities) {
            KeyValue v1 = (KeyValue) v;
            connection.add(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "mediumQuality"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }

    public void removeMediumQualityAttributes(String patternID, Object[] selectedValues) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : selectedValues) {
            KeyValue v1 = (KeyValue) v;
            connection.remove(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "mediumQuality"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }

    public void addLowQualityAttributes(String patternID, Object[] lowQualities) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : lowQualities) {
            KeyValue v1 = (KeyValue) v;
            connection.add(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "lowQuality"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }
    
    public void removeLowQualityAttributes(String patternID, Object[] selectedValues) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : selectedValues) {
            KeyValue v1 = (KeyValue) v;
            connection.remove(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "lowQuality"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }

    public void addRoles(String patternID, String roleName) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        String roleID = api.getUtils().generateUniqueID("role");
        connection.add(f.createURI(RESOURCE.URI + roleID), RDF.TYPE, f.createURI(RESOURCE.URI + "Role"));
        connection.add(f.createURI(RESOURCE.URI + roleID), f.createURI(RESOURCE.URI + "roleName"), f.createLiteral(roleName));
        
        connection.add(f.createURI(RESOURCE.URI + patternID), f.createURI(RESOURCE.URI + "hasRole"), f.createURI(RESOURCE.URI + roleID));
        
        connection.commit();
    }
    
    public void removeRoles(String patternID, Object[] selectedValues) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : selectedValues) {
            KeyValue v1 = (KeyValue) v;
            connection.remove(f.createURI(RESOURCE.URI + v1.getKey()), null, null);
            connection.remove((URI) null, null, f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }
}
