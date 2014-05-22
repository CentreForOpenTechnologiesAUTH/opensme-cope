package eu.opensme.cope.knowledgemanager.api.actions;

import eu.opensme.cope.knowledgemanager.gui.classification.tree.TreeMetaModelNodeData;
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
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

public class MetaModel {

    private ReuseApi api;
    private boolean delay = false;

    public MetaModel(ReuseApi api) {
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
        query.append("SELECT DISTINCT ?ID ?Name ?Type \n").
                append("WHERE { \n").
                append("?ID rdfs:subClassOf opensme2:").append(baseClass).append("; \n").
                append("rdfs:label ?Name; \n").
                append("rdfs:comment ?Type. \n").
                append("FILTER ( isURI(?ID) && ?ID != opensme2:").append(baseClass).append("). ").
                append("FILTER NOT EXISTS { \n").
                append("?ID rdfs:subClassOf ?Y. ?Y rdfs:subClassOf opensme2:").append(baseClass).append(". FILTER (isURI(?Y) && ?Y != ?ID && ?Y != opensme2:").append(baseClass).append("). \n").
                append("} \n");

        query.append("} ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<TreeMetaModelNodeData> getDirectSubclasses(String baseClass) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<TreeMetaModelNodeData> result = new ArrayList<TreeMetaModelNodeData>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetDirectSubclasses(baseClass);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            TreeMetaModelNodeData pair = new TreeMetaModelNodeData();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                pair.setId(classID);
                pair.setName(it1.next().getValue().stringValue());
                pair.setType(it1.next().getValue().stringValue());
                result.add(pair);
            }
        }
        evaluate.close();
        return result;
    }

    public TreeMetaModelNodeData buildMetaModelHierarchy() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);

        ArrayList<TreeMetaModelNodeData> expandSet = new ArrayList<TreeMetaModelNodeData>();
        TreeMetaModelNodeData root = new TreeMetaModelNodeData("COMPONENT-METAMODEL", "MetaModels", "root");
        expandSet.add(root);

        while (!expandSet.isEmpty()) {
            TreeMetaModelNodeData current = expandSet.remove(0);
            ArrayList<TreeMetaModelNodeData> directSubclasses = getDirectSubclasses(current.getId());
            current.setChildren(directSubclasses);
            expandSet.addAll(directSubclasses);
        }
        return root;
    }

    public TreeMetaModelNodeData addGroup(String baseClassID, String name) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID("group");

        connection.add(f.createURI(RESOURCE.URI2 + id), RDF.TYPE, OWL.CLASS);
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + baseClassID));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + "COMPONENT-METAMODEL"));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.COMMENT, f.createLiteral("group"));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.LABEL, f.createLiteral(name));
        connection.commit();
        return new TreeMetaModelNodeData(id, name, "group");
    }

    public void renameGroup(String metaClassID, String newName) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        connection.remove(f.createURI(RESOURCE.URI2 + metaClassID), RDFS.LABEL, null);
        connection.add(f.createURI(RESOURCE.URI2 + metaClassID), RDFS.LABEL, f.createLiteral(newName));

        connection.commit();
    }

    private void deleteGroupOrMetaModel(String baseClassID, RepositoryConnection connection, ValueFactory f) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        //find and remove direct restrictions on BELONGS-TO-DOMAIN (if any)
        String query = buildGetDirectRestrictionsOnBelongsToDomain(baseClassID);
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

        //find and remove direct restrictions on BELONGS-TO-CONCEPT (if any)
        String query2 = buildGetDirectRestrictionsOnBelongsToConcept(baseClassID);
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

        //find and remove direct restrictions on HAS-TIER (if any)
        String query3 = buildGetDirectRestrictionsOnHasTier(baseClassID);
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

        //remove the class
        connection.remove(f.createURI(RESOURCE.URI2 + baseClassID), null, null);
        connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + baseClassID));

    }

    public void deleteHierarchy(String baseClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //find all subclasses
        ArrayList<String> hierarchy = new ArrayList<String>();
        hierarchy.add(baseClassID);
        String query = buildFindAllSubclasses(baseClassID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                hierarchy.add(classID);
            }
        }
        evaluate.close();

        for (String c : hierarchy) {
            deleteGroupOrMetaModel(c, connection, f);
        }
        connection.commit();
    }

    public void addDomainToGroup(String groupClassID, String domainClassID) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //create restriction all-values
        String restID = api.getUtils().generateUniqueID("rest");
        connection.add(f.createURI(RESOURCE.URI2 + restID), RDF.TYPE, OWL.RESTRICTION);
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "BELONGS-TO-DOMAIN"));
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.HASVALUE, f.createURI(RESOURCE.URI2 + "_" + domainClassID));
        connection.add(f.createURI(RESOURCE.URI2 + groupClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID));

        connection.commit();
    }

    private String buildGetGroupDomainsAsInstances(String groupClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?DomainInstanceID ?Name \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:BELONGS-TO-DOMAIN; \n").
                append("owl:hasValue ?DomainInstanceID. \n").
                append("?DomainInstanceID opensme2:DOMAIN-NAME ?Name. \n");
        query.append("} ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    private String buildFindAllSubclasses(String groupClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Subclass ?Name \n").
                append("WHERE { \n").
                append("?Subclass rdfs:subClassOf opensme2:").append(groupClassID).append("; \n").
                append("rdfs:label ?Name. \n").
                append("FILTER (?Subclass != opensme2:").append(groupClassID).append(" && isURI(?Subclass)).");

        query.append("} \n");
        return query.toString();
    }

    private String buildGetDirectRestrictionOnBelongsToDomainWithKnownHasValue(String groupClassID, String domainInstanceID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:BELONGS-TO-DOMAIN; \n").
                append("owl:hasValue opensme2:").append(domainInstanceID).append(". \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    public ArrayList<KeyValue> getGroupDomains(String groupClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();

        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetGroupDomainsAsInstances(groupClassID);
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
                String query2 = buildGetDirectRestrictionOnBelongsToDomainWithKnownHasValue(groupClassID, pair.getKey());
                TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
                TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
                pair.setExplicit(evaluate2.hasNext());
                evaluate2.close();
                //////////////////////////////////////////
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

//    private String buildGetDirectRestrictionOnHasConceptWithKnownHasValue(String groupClassID, String domainClassID) {
//        StringBuilder query = new StringBuilder();
//        query.append(api.getUtils().getPrefixes());
//        query.append("SELECT DISTINCT ?Restriction \n").
//                append("WHERE { \n").
//                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
//                append("?Restriction rdf:type owl:Restriction; \n").
//                append("owl:onProperty opensme2:BELONGS-TO-DOMAIN; \n").
//                append("owl:allValuesFrom opensme2:").append(domainClassID).append(". \n").
//                append("FILTER NOT EXISTS { \n").
//                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
//                append("} \n");
//
//        query.append("} \n");
//        return query.toString();
//    }
    public void removeDomainsFromGroup(String groupClassID, Object[] instanceValues) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        boolean flag = false;
        for (Object v : instanceValues) {
            KeyValue instance = (KeyValue) v;
            //remove from instance
            //connection.remove(f.createURI(RESOURCE.URI2 + "_" + domainClassID), f.createURI(RESOURCE.URI2 + "HAS-CONCEPT"), f.createURI(RESOURCE.URI2 + v1.getKey()));

            //remove restriction
            String query = buildGetDirectRestrictionOnBelongsToDomainWithKnownHasValue(groupClassID, instance.getKey());
            TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult evaluate = prepareGraphQuery.evaluate();
            while (evaluate.hasNext()) {
                flag = true;
                BindingSet next = evaluate.next();
                for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                    String restrictionID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                    connection.remove(f.createURI(RESOURCE.URI2 + restrictionID), null, null);
                    connection.remove((URI) null, null, f.createURI(RESOURCE.URI2 + restrictionID));
                }
            }
            evaluate.close();
        }


        //i should also remove any concept since there is only one domain!!!
        //i should remove any concept for every subclass...
        if (flag) {
            ArrayList<String> hierarchy = new ArrayList<String>();
            hierarchy.add(groupClassID);
            String query = buildFindAllSubclasses(groupClassID);
            TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult evaluate = prepareGraphQuery.evaluate();
            while (evaluate.hasNext()) {
                BindingSet next = evaluate.next();
                for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                    String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                    hierarchy.add(classID);
                }
            }
            evaluate.close();

            for (String h : hierarchy) {
                String query2 = buildGetDirectRestrictionsOnBelongsToConcept(h);
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
                evaluate.close();
            }
        }
        connection.commit();
    }

    public TreeMetaModelNodeData addMetaModel(String groupBaseClass, String name) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID("metamodel");

        connection.add(f.createURI(RESOURCE.URI2 + id), RDF.TYPE, OWL.CLASS);
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + groupBaseClass));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + "COMPONENT-METAMODEL"));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.COMMENT, f.createLiteral("leaf"));
        connection.add(f.createURI(RESOURCE.URI2 + id), RDFS.LABEL, f.createLiteral(name));
        connection.commit();
        return new TreeMetaModelNodeData(id, name, "leaf");
    }

    private String buildGetGroupConceptsAsInstances(String groupClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ConceptInstanceID ?Name \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:BELONGS-TO-CONCEPT; \n").
                append("owl:hasValue ?ConceptInstanceID. \n").
                append("?ConceptInstanceID opensme2:CONCEPT-NAME ?Name. \n");
        query.append("} ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    private String buildGetDirectRestrictionOnBelongsToConceptWithKnownHasValue(String groupClassID, String conceptInstanceID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:BELONGS-TO-CONCEPT; \n").
                append("owl:hasValue opensme2:").append(conceptInstanceID).append(". \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    public ArrayList<KeyValue> getGroupConcepts(String leafClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();

        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetGroupConceptsAsInstances(leafClassID);
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
                String query2 = buildGetDirectRestrictionOnBelongsToConceptWithKnownHasValue(leafClassID, pair.getKey());
                TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
                TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
                pair.setExplicit(evaluate2.hasNext());
                evaluate2.close();
                //////////////////////////////////////////
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

    public void addConceptToGroup(String leafClassID, String conceptInstanceID) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //create restriction all-values
        String restID = api.getUtils().generateUniqueID("rest");
        connection.add(f.createURI(RESOURCE.URI2 + restID), RDF.TYPE, OWL.RESTRICTION);
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "BELONGS-TO-CONCEPT"));
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.HASVALUE, f.createURI(RESOURCE.URI2 + conceptInstanceID));
        connection.add(f.createURI(RESOURCE.URI2 + leafClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID));

        connection.commit();
    }

//    private String buildGetDirectRestrictionOnBelongsToConceptWithKnownValue(String groupClassID, String domainClassID) {
//        StringBuilder query = new StringBuilder();
//        query.append(api.getUtils().getPrefixes());
//        query.append("SELECT DISTINCT ?Restriction \n").
//                append("WHERE { \n").
//                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
//                append("?Restriction rdf:type owl:Restriction; \n").
//                append("owl:onProperty opensme2:BELONGS-TO-CONCEPT; \n").
//                append("owl:allValuesFrom opensme2:").append(domainClassID).append(". \n").
//                append("FILTER NOT EXISTS { \n").
//                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
//                append("} \n");
//
//        query.append("} \n");
//        return query.toString();
//    }
    public void removeConceptsFromGroup(String groupClassID, Object[] instanceValues) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (Object v : instanceValues) {
            KeyValue instance = (KeyValue) v;
            //remove from instance
            //connection.remove(f.createURI(RESOURCE.URI2 + "_" + domainClassID), f.createURI(RESOURCE.URI2 + "HAS-CONCEPT"), f.createURI(RESOURCE.URI2 + v1.getKey()));

            //remove restriction
            String query = buildGetDirectRestrictionOnBelongsToConceptWithKnownHasValue(groupClassID, instance.getKey());
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
        }
        connection.commit();
    }

    private String buildGetDirectRestrictionsOnBelongsToDomain(String groupClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:BELONGS-TO-DOMAIN. \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    private String buildGetDirectRestrictionsOnBelongsToConcept(String groupClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:BELONGS-TO-CONCEPT. \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    public void addTierToMetaModel(String groupClassID, String tier) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //remove existing tier
        String query = buildGetDirectRestrictionsOnHasTier(groupClassID);
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


        //create restriction has-value
        String restID = api.getUtils().generateUniqueID("rest");
        connection.add(f.createURI(RESOURCE.URI2 + restID), RDF.TYPE, OWL.RESTRICTION);
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.ONPROPERTY, f.createURI(RESOURCE.URI2 + "HAS-TIER"));
        connection.add(f.createURI(RESOURCE.URI2 + restID), OWL.HASVALUE, f.createLiteral(tier));
        connection.add(f.createURI(RESOURCE.URI2 + groupClassID), RDFS.SUBCLASSOF, f.createURI(RESOURCE.URI2 + restID));

        connection.commit();
    }

    private String buildGetDirectRestrictionsOnHasTier(String groupClassID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Restriction ?Tier \n").
                append("WHERE { \n").
                append("opensme2:").append(groupClassID).append(" rdfs:subClassOf ?Restriction. \n").
                append("?Restriction rdf:type owl:Restriction; \n").
                append("owl:onProperty opensme2:HAS-TIER; \n").
                append("owl:hasValue ?Tier. \n").
                append("FILTER NOT EXISTS { \n").
                append("?X rdfs:subClassOf ?Restriction. opensme2:").append(groupClassID).append(" rdfs:subClassOf ?X. FILTER(isURI(?X) && ?X != ?Restriction && ?X != opensme2:").append(groupClassID).append(").").
                append("} \n");

        query.append("} \n");
        return query.toString();
    }

    public void removeTierFromMetaModel(String groupClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        String query = buildGetDirectRestrictionsOnHasTier(groupClassID);
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

        connection.commit();

    }

    public KeyValue getMetaModelTier(String leafClassID) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        KeyValue result = null;
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetDirectRestrictionsOnHasTier(leafClassID);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            result = new KeyValue();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String classID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                result.setKey(classID);
                result.setValue(it1.next().getValue().stringValue());
            }
        }
        evaluate.close();
        return result;
    }
}
