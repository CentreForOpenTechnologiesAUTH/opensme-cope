package eu.opensme.cope.knowledgemanager.api.actions;

import java.util.ArrayList;
import java.util.Iterator;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
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
import eu.opensme.cope.knowledgemanager.api.dto.DomainTreeInfo;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;
import eu.opensme.cope.knowledgemanager.gui.classification.tree.TreeDomainNodeData;

public class Concept {

    private ReuseApi api;
    private boolean delay = false;

    public Concept(ReuseApi api) {
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

    private String buildGetDirectSubclasses(String baseClass) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdfs:subClassOf opensme2:").append(baseClass).append("; \n").
                append("rdfs:label ?Name. \n").
                append("FILTER ( isURI(?ID) && ?ID != opensme2:").append(baseClass).append("). ").
                append("FILTER NOT EXISTS { \n").
                append("?ID rdfs:subClassOf ?Y. ?Y rdfs:subClassOf opensme2:").append(baseClass).append(". FILTER (isURI(?Y) && ?Y != ?ID && ?Y != opensme2:").append(baseClass).append("). \n").
                append("} \n");

        query.append("} ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<TreeDomainNodeData> getDirectSubclasses(String baseClass) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<TreeDomainNodeData> result = new ArrayList<TreeDomainNodeData>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetDirectSubclasses(baseClass);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            TreeDomainNodeData pair = new TreeDomainNodeData();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                pair.setId(classID);
                pair.setName(it1.next().getValue().stringValue());
                result.add(pair);
            }
        }
        evaluate.close();
        return result;
    }

    public TreeDomainNodeData buildConceptHierarchy() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);

        ArrayList<TreeDomainNodeData> expandSet = new ArrayList<TreeDomainNodeData>();
        TreeDomainNodeData root = new TreeDomainNodeData("CONCEPT", "Concepts");
        expandSet.add(root);

        while (!expandSet.isEmpty()) {
            TreeDomainNodeData current = expandSet.remove(0);
            ArrayList<TreeDomainNodeData> directSubclasses = getDirectSubclasses(current.getId());
            current.setChildren(directSubclasses);
            expandSet.addAll(directSubclasses);
        }


        return root;
    }

    public DomainTreeInfo addConceptSubclass(String baseClassID, String newClassName) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID("conc");

        connection.add(f.createURI(RESOURCE.URI2 + id), RDF.TYPE, OWL.CLASS);
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.LABEL, f.createLiteral(newClassName));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + baseClassID));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + "CONCEPT"));

        connection.add(f.createURI(RESOURCE.URI2 + "_" + id), RDF.TYPE, f.createURI(RESOURCE.URI2 + id));
        connection.add(f.createURI(RESOURCE.URI2 + "_" + id), f.createURI(RESOURCE.URI2 + "CONCEPT-NAME"), f.createLiteral(newClassName));

        connection.commit();
        return new DomainTreeInfo(id, newClassName);
    }

    public void renameConcept(String conceptClassID, String newName) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();

        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI2 + conceptClassID);
        URI instanceURI = connection.getValueFactory().createURI(RESOURCE.URI2 + "_" + conceptClassID);
        URI propURI = connection.getValueFactory().createURI(RESOURCE.URI2 + "CONCEPT-NAME");

        //rename the class label
        connection.remove(compURI, RDFS.LABEL, null);
        connection.add(compURI, RDFS.LABEL, connection.getValueFactory().createLiteral(newName));

        //rename the instance property
        connection.remove(instanceURI, propURI, null);
        connection.add(instanceURI, propURI, connection.getValueFactory().createLiteral(newName));

        connection.commit();
    }

//    private String buildGetConceptInstances() {
//        StringBuilder query = new StringBuilder();
//        query.append(api.getUtils().getPrefixes());
//        query.append("SELECT DISTINCT ?ID ?Name \n").
//                append("WHERE { \n").
//                append("?ID rdf:type opensme2:CONCEPT; \n").
//                append("opensme2:CONCEPT-NAME ?Name. \n");
//
//        query.append("} ORDER BY ASC(?Name) \n");
//        return query.toString();
//    }
//
//    public ArrayList<KeyValue> getConceptInstances() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
//        delay(100);
//        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
//        RepositoryConnection connection = api.getDatabase().getConnection();
//        String query = buildGetConceptInstances();
//        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
//        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
//        while (evaluate.hasNext()) {
//            KeyValue pair = new KeyValue();
//            BindingSet next = evaluate.next();
//            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
//                String compID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
//                pair.setKey(compID);
//                pair.setValue(it1.next().getValue().stringValue());
//                result.add(pair);
//            }
//        }
//        evaluate.close();
//        return result;
//    }
    
    
    private String buildGetConceptDomainsAsInstances(String conceptClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?DomainInstance ?Name \n").
                append("WHERE { \n").
                append("opensme2:_").append(conceptClassID).append(" rdf:type opensme2:CONCEPT; \n").
                append("opensme2:HAS-DOMAIN ?DomainInstance. \n").
                append("?DomainInstance opensme2:DOMAIN-NAME ?Name. \n");

        query.append("} ORDER BY ASC(?Name) \n");
        return query.toString();
    }
    
//    private String buildGetConceptDomains(String conceptClassID) {
//        StringBuilder query = new StringBuilder();
//        query.append(api.getUtils().getPrefixes());
//        query.append("SELECT DISTINCT ?DomainClassID ?Name \n").
//                append("WHERE { \n").
//                append("?DomainClassID rdfs:subClassOf ?Restriction; \n").
//                append("rdfs:label ?Name. \n").
//                append("?Restriction rdf:type owl:Restriction; \n").
//                append("owl:onProperty opensme2:HAS-CONCEPT; \n").
//                append("owl:allValuesFrom opensme2:").append(conceptClassID).append(". \n");
//                
//        query.append("} ORDER BY ASC(?Name) \n");
//        return query.toString();
//    }
    
    

    //instances!!
//    private String buildGetConceptDomains(String conceptClassID) {
//        StringBuilder query = new StringBuilder();
//        query.append(api.getUtils().getPrefixes());
//        query.append("SELECT DISTINCT ?DomainInstance ?Name \n").
//                append("WHERE { \n").
//                append("opensme2:_").append(conceptClassID).append(" rdf:type opensme2:CONCEPT; \n").
//                append("opensme2:HAS-DOMAIN ?DomainInstance. \n").
//                append("?DomainInstance opensme2:DOMAIN-NAME ?Name. \n");
//
//        query.append("} ORDER BY ASC(?Name) \n");
//        return query.toString();
//    }
    
    //instances!!
    public ArrayList<KeyValue> getConceptDomainsAsInstances(String conceptClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();

        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetConceptDomainsAsInstances(conceptClassID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            KeyValue pair = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String instanceID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                pair.setKey(instanceID);
                pair.setValue(it1.next().getValue().stringValue());

                //if direct relevant restriction exists...
                String query2 = buildGetDirectRestrictionOnHasDomainWithKnownHasValue(conceptClassID, pair.getKey());
                TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
                TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
                pair.setExplicit(evaluate2.hasNext());
                evaluate2.close();
                
                result.add(pair);
            }
        }
        evaluate.close();
        
        ArrayList<KeyValue> r2 = new ArrayList<KeyValue>();
//        for (KeyValue keyValue : result) {
//            String query2 = buildFindAllSubclasses(keyValue.getKey());
//            TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
//            TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
//            while (evaluate2.hasNext()) {
//                KeyValue pair = new KeyValue();
//                BindingSet next2 = evaluate2.next();
//                for (Iterator<Binding> it1 = next2.iterator(); it1.hasNext();) {
//                    String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
//                    pair.setKey(classID);
//                    pair.setValue(it1.next().getValue().stringValue());
//                    r2.add(pair);
//                }
//            }
//            evaluate2.close();
//        }

        result.addAll(r2);
        
        return result;
    }

    public void addDomain(String conceptClassID, String domainClassID) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //create restriction has-value
        String restID = api.getUtils().generateUniqueID("rest");
        connection.add(f.createURI(RESOURCE.URI2 + restID), RDF.TYPE, OWL.RESTRICTION);
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "HAS-DOMAIN"));
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.HASVALUE, f.createURI(RESOURCE.URI2 + "_" + domainClassID));
        connection.add(f.createURI(RESOURCE.URI2 + conceptClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID));
        
        //create restriction all-values
//        String restID1 = api.getUtils().generateUniqueID("rest");
//        connection.add(f.createURI(RESOURCE.URI2 + restID1), RDF.TYPE, OWL.RESTRICTION);
//        connection.add(f.createURI(RESOURCE.URI2 + restID1), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "HAS-DOMAIN"));
//        connection.add(f.createURI(RESOURCE.URI2 + restID1), OWL.ALLVALUESFROM, f.createURI(RESOURCE.URI2 + domainClassID));
//        connection.add(f.createURI(RESOURCE.URI2 + conceptClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID1));

        //create the inverse restriction has-value
//        String restID2 = api.getUtils().generateUniqueID("rest");
//        connection.add(f.createURI(RESOURCE.URI2 + restID2), RDF.TYPE, OWL.RESTRICTION);
//        connection.add(f.createURI(RESOURCE.URI2 + restID2), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "HAS-CONCEPT"));
//        connection.add(f.createURI(RESOURCE.URI2 + restID2), OWL.HASVALUE, f.createURI(RESOURCE.URI2 + "_" + conceptClassID));
//        connection.add(f.createURI(RESOURCE.URI2 + domainClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID2));
//        
        //create the inverse restriction all-values
//        String restID4 = api.getUtils().generateUniqueID("rest");
//        connection.add(f.createURI(RESOURCE.URI2 + restID4), RDF.TYPE, OWL.RESTRICTION);
//        connection.add(f.createURI(RESOURCE.URI2 + restID4), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "HAS-CONCEPT"));
//        connection.add(f.createURI(RESOURCE.URI2 + restID4), OWL.ALLVALUESFROM, f.createURI(RESOURCE.URI2 + conceptClassID));
//        connection.add(f.createURI(RESOURCE.URI2 + domainClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID4));


        //manually in order to be able to separate explicit and non-explicit statements...
        //connection.add(f.createURI(RESOURCE.URI2 + "_" + conceptClassID), f.createURI(RESOURCE.URI2 + "HAS-DOMAIN"), f.createURI(RESOURCE.URI2 + "_" + domainClassID));

        connection.commit();
    }

    private String buildGetDirectRestrictionOnHasDomainWithKnownHasValue(String conceptClassID, String domainInstanceID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(conceptClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:HAS-DOMAIN; \n").
                append("owl:hasValue opensme2:").append(domainInstanceID).append(". \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(conceptClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(conceptClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    private String buildGetInverseRestriction(String domainClassID, String conceptClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(domainClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:HAS-CONCEPT; \n").
                append("owl:allValuesFrom opensme2:").append(conceptClassID).append(". \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(domainClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(domainClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    public void removeDomainsFromConcept(String conceptClassID, Object[] instanceValues) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (Object v : instanceValues) {
            KeyValue instance = (KeyValue) v;
            //remove from instance
            //connection.remove(f.createURI(RESOURCE.URI2 + "_" + conceptClassID), f.createURI(RESOURCE.URI2 + "HAS-DOMAIN"), f.createURI(RESOURCE.URI2 + v1.getKey()));

            //remove restriction
            String query = buildGetDirectRestrictionOnHasDomainWithKnownHasValue(conceptClassID, instance.getKey());
            TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult evaluate = prepareGraphQuery.evaluate();
            while (evaluate.hasNext()) {
                BindingSet next = evaluate.next();
                for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                    String restrictionID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                    connection.remove(f.createURI(RESOURCE.URI2 + restrictionID), null, null);
                    connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + restrictionID));
                }
            }
            evaluate.close();

            //remove the inverse restriction
//            String query2 = buildGetInverseRestriction(instance.getKey(), conceptClassID);
//            TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
//            TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
//            while (evaluate2.hasNext()) {
//                BindingSet next2 = evaluate2.next();
//                for (Iterator<Binding> it1 = next2.iterator(); it1.hasNext();) {
//                    String restrictionID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
//                    connection.remove(f.createURI(RESOURCE.URI2 + restrictionID), null, null);
//                    connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + restrictionID));
//                }
//            }
//            evaluate2.close();

        }
        connection.commit();
    }

    private String buildGetDirectRestrictionsOnHasDomain(String conceptClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(conceptClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:HAS-DOMAIN. \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(conceptClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(conceptClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }
    
    
    private String buildGetRestrictionsWithHasConcept(String conceptClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:").append("HAS-CONCEPT").append("; \n").
                append("owl:hasValue opensme2:_").append(conceptClassID).append(". \n");
        query.append("} \n");
        return query.toString();
    }
    
    private String buildGetRestrictionsWithBelongsToConcept(String conceptClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:").append("BELONGS-TO-CONCEPT").append("; \n").
                append("owl:hasValue opensme2:_").append(conceptClassID).append(". \n");
        query.append("} \n");
        return query.toString();
    }

    
    private void deleteConcept(String conceptClassID, RepositoryConnection connection, ValueFactory f) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        //find and remove direct restrictions on HAS-CONCEPT (if any)
        String query = buildGetDirectRestrictionsOnHasDomain(conceptClassID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String restrictionID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                connection.remove(f.createURI(RESOURCE.URI2 + restrictionID), null, null);
                connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + restrictionID));
            }
        }
        evaluate.close();
        
        
        //find and remove restrictions on BELONGS-TO-CONCEPT
        String query3 = buildGetRestrictionsWithBelongsToConcept(conceptClassID);
        TupleQuery prepareGraphQuery3 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query3);
        TupleQueryResult evaluate3 = prepareGraphQuery3.evaluate();
        while (evaluate3.hasNext()) {
            BindingSet next3 = evaluate3.next();
            for (Iterator<Binding> it1 = next3.iterator(); it1.hasNext();) {
                String restrictionID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                connection.remove(f.createURI(RESOURCE.URI2 + restrictionID), null, null);
                connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + restrictionID));
            }
        }
        evaluate3.close();

        //find and remove restrictions that refer to this concept (in domains - HAS-CONCEPT)
        String query2 = buildGetRestrictionsWithHasConcept(conceptClassID);
        TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
        TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
        while (evaluate2.hasNext()) {
            BindingSet next2 = evaluate2.next();
            for (Iterator<Binding> it1 = next2.iterator(); it1.hasNext();) {
                String restrictionID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                connection.remove(f.createURI(RESOURCE.URI2 + restrictionID), null, null);
                connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + restrictionID));
            }
        }
        evaluate2.close();

        //remove the instance
        connection.remove(f.createURI(RESOURCE.URI2 + "_" + conceptClassID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + "_" + conceptClassID));

        //remove the class
        connection.remove(f.createURI(RESOURCE.URI2 + conceptClassID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + conceptClassID));

    }
    
    
    
    private String buildFindAllSubclasses(String domainClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Subclass ?Name \n").
                append("WHERE { \n").
                append("?Subclass rdfs:subClassOf opensme2:").append(domainClassID).append("; \n").
                append("rdfs:label ?Name. \n").
                append("FILTER (?Subclass != opensme2:").append(domainClassID).append(" && isURI(?Subclass)).");

        query.append("} \n");
        return query.toString();
    }
    
    public void deleteHierarchy(String conceptClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //find all subclasses
        ArrayList<String> hierarchy = new ArrayList<String>();
        hierarchy.add(conceptClassID);
//        String query = buildFindAllSubclasses(conceptClassID);
//        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
//        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
//        while (evaluate.hasNext()) {
//            BindingSet next = evaluate.next();
//            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
//                String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
//                hierarchy.add(classID);
//            }
//        }
//        evaluate.close();

        for (String c : hierarchy) {
            deleteConcept(c, connection, f);
        }
        connection.commit();
    }

private String buildGetConceptsAsInstances() {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdf:type opensme2:CONCEPT; \n").
                append("opensme2:CONCEPT-NAME ?Name. \n");
        query.append("} ORDER BY asc(?Name)\n");
        return query.toString();
    }

    public ArrayList<KeyValue> getConceptsAsInstances() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        String query = buildGetConceptsAsInstances();
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            KeyValue kv = new KeyValue();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String id = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                kv.setKey(id);
                kv.setValue(it1.next().getValue().stringValue());
                result.add(kv);
            }
        }
        evaluate.close();
        return result;
    }    
}
