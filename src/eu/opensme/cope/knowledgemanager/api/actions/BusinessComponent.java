package eu.opensme.cope.knowledgemanager.api.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import eu.opensme.cope.knowledgemanager.api.dto.BusinessComponentDetails;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListKeyValuePatternDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListKeyValuePatternModel;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListNameVersionTierDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentRelationDataModel;

public class BusinessComponent {

    private ReuseApi api;
    private boolean delay = false;

    public BusinessComponent(ReuseApi api) {
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

    private String buildGetBusinessComponents() {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdf:type ").append("opensme:BComponent; \n").
                append("opensme:bcomponentName ?Name. } ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<KeyValue> getBusinessComponents() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetBusinessComponents();
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

    public KeyValue addBusinessComponent(String name) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID("bcomp");

        connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "BComponent"));
        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "bcomponentName"), f.createLiteral(name));
        connection.commit();
        return new KeyValue(id, name);
    }

    public void removeBusinessComponent(String bcomponentID) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        connection.remove(f.createURI(RESOURCE.URI + bcomponentID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI + bcomponentID));
        connection.commit();
    }

    public void renameBusinessComponent(String bcomponentID, String name) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        connection.remove(f.createURI(RESOURCE.URI + bcomponentID), f.createURI(RESOURCE.URI + "bcomponentName"), null);
        connection.add(f.createURI(RESOURCE.URI + bcomponentID), f.createURI(RESOURCE.URI + "bcomponentName"), f.createLiteral(name));
        
        connection.commit();
    }

    public void setEnterpriseComponents(String id, List<SortedListNameVersionTierDataModel> enterpriseTier) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : enterpriseTier) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "hasEnterpriseTier");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }
    
    public void setResourceComponents(String id, List<SortedListNameVersionTierDataModel> resourceTier) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : resourceTier) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "hasResourceTier");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }
    
    public void setUserComponents(String id, List<SortedListNameVersionTierDataModel> userTier) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : userTier) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "hasUserTier");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }
    
    public void setWorkspaceComponents(String id, List<SortedListNameVersionTierDataModel> workspaceTier) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : workspaceTier) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "hasWorkspaceTier");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }
    
    private String buildGetBusinessComponentRoles(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?RoleID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:BComponent; \n").
                append("opensme:playsRole ?RoleID. \n").
                append("?RoleID opensme:roleName ?Name. \n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<KeyValue> getBusinessComponentRoles(String patternID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetBusinessComponentRoles(patternID);
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

    public BusinessComponentDetails getBusinessComponentDetails(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        BusinessComponentDetails result = new BusinessComponentDetails();
        result.setEnterpriseTier(getComponentEnterpriseTier(id));
        result.setResourceTier(getComponentResourceTier(id));
        result.setUserTier(getComponentUserTier(id));
        result.setWorkspaceTier(getComponentWorkspaceTier(id));
        result.setRoles(getBusinessComponentRoles(id));
        
        return result;
    }

    private String buildGetComponentEnterpriseTier(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:BComponent; \n").
                append("opensme:hasEnterpriseTier ?ID. \n").
                append("?ID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?ID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<ComponentRelationDataModel> getComponentEnterpriseTier(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentEnterpriseTier(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            ComponentRelationDataModel u = new ComponentRelationDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setComponentName(it1.next().getValue().stringValue());
                if (next.hasBinding("Version")) {
                    u.setVersion(it1.next().getValue().stringValue());
                } else {
                    u.setVersion("");
                }
                u.setTier(it1.next().getValue().stringValue());

                ValueFactory f = connection.getValueFactory();
                URI subURI = f.createURI(RESOURCE.URI + id);
                URI preURI = f.createURI(RESOURCE.URI + "hasEnterpriseTier");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentResourceTier(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:BComponent; \n").
                append("opensme:hasResourceTier ?ID. \n").
                append("?ID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?ID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<ComponentRelationDataModel> getComponentResourceTier(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentResourceTier(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            ComponentRelationDataModel u = new ComponentRelationDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setComponentName(it1.next().getValue().stringValue());
                if (next.hasBinding("Version")) {
                    u.setVersion(it1.next().getValue().stringValue());
                } else {
                    u.setVersion("");
                }
                u.setTier(it1.next().getValue().stringValue());

                ValueFactory f = connection.getValueFactory();
                URI subURI = f.createURI(RESOURCE.URI + id);
                URI preURI = f.createURI(RESOURCE.URI + "hasResourceTier");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentUserTier(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:BComponent; \n").
                append("opensme:hasUserTier ?ID. \n").
                append("?ID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?ID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<ComponentRelationDataModel> getComponentUserTier(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentUserTier(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            ComponentRelationDataModel u = new ComponentRelationDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setComponentName(it1.next().getValue().stringValue());
                if (next.hasBinding("Version")) {
                    u.setVersion(it1.next().getValue().stringValue());
                } else {
                    u.setVersion("");
                }
                u.setTier(it1.next().getValue().stringValue());

                ValueFactory f = connection.getValueFactory();
                URI subURI = f.createURI(RESOURCE.URI + id);
                URI preURI = f.createURI(RESOURCE.URI + "hasUserTier");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentWorkspaceTier(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:BComponent; \n").
                append("opensme:hasWorkspaceTier ?ID. \n").
                append("?ID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?ID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }
    private ArrayList<ComponentRelationDataModel> getComponentWorkspaceTier(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentWorkspaceTier(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            ComponentRelationDataModel u = new ComponentRelationDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setComponentName(it1.next().getValue().stringValue());
                if (next.hasBinding("Version")) {
                    u.setVersion(it1.next().getValue().stringValue());
                } else {
                    u.setVersion("");
                }
                u.setTier(it1.next().getValue().stringValue());

                ValueFactory f = connection.getValueFactory();
                URI subURI = f.createURI(RESOURCE.URI + id);
                URI preURI = f.createURI(RESOURCE.URI + "hasWorkspaceTier");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    public void removeEnterprise(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "hasEnterpriseTier");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }
    
    public void removeResource(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "hasResourceTier");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }
    
    public void removeUser(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "hasUserTier");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }
    public void removeWorkspace(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "hasWorkspaceTier");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }

//    public void addPlayRoles(String bcomponentID, Object[] roles) throws RepositoryException {
//        delay(100);
//
//        RepositoryConnection connection = api.getDatabase().getConnection();
//        ValueFactory f = connection.getValueFactory();
//        
//        for (Object v : roles) {
//            KeyValue v1 = (KeyValue) v;
//            connection.add(f.createURI(RESOURCE.URI + bcomponentID), f.createURI(RESOURCE.URI + "playsRole"), f.createURI(RESOURCE.URI + v1.getKey()));
//        }
//        connection.commit();
//    }

    public void removeRoles(String bcomponentID, Object[] selectedValues) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Object v : selectedValues) {
            KeyValue v1 = (KeyValue) v;
            connection.remove(f.createURI(RESOURCE.URI + bcomponentID), f.createURI(RESOURCE.URI + "playsRole"), f.createURI(RESOURCE.URI + v1.getKey()));
        }
        connection.commit();
    }

    public void addPlaysRoles(String bcomponentID, SortedListKeyValuePatternModel selectedRolesList) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        
        for (Iterator it = selectedRolesList.iterator(); it.hasNext();) {
            SortedListKeyValuePatternDataModel object = (SortedListKeyValuePatternDataModel) it.next();
            connection.add(f.createURI(RESOURCE.URI + bcomponentID), f.createURI(RESOURCE.URI + "playsRole"), f.createURI(RESOURCE.URI + object.getKey()));
        }
        
        connection.commit();
    }

    
}
