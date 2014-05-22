package eu.opensme.cope.knowledgemanager.api.actions;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.domain.GeneratedComponent;
import eu.opensme.cope.knowledgemanager.api.CompareInterfaceException;
import eu.opensme.cope.knowledgemanager.api.InvalidTierException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import eu.opensme.cope.knowledgemanager.api.dto.ComponentDetails;
import eu.opensme.cope.knowledgemanager.api.dto.ComponentTreeInfo;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListNameVersionTierDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.tree.TreeNodeData;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentListDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentRelationDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.InterfaceDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.SearchComponentDataModel;
import eu.opensme.cope.knowledgemanager.icompare.ComponentDTO;
import eu.opensme.cope.knowledgemanager.xml.CompareSynchronizer;
import eu.opensme.cope.knowledgemanager.xml.SYNC;
import java.util.HashMap;
import org.openrdf.model.vocabulary.RDFS;

public class OpenSMEComponent {

    private ReuseApi api;
    private boolean delay = false;

    public OpenSMEComponent(ReuseApi api) {
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

    String flattenArray(String[] array) {
        String result = "";
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.length; i++) {
            result += array[i];
            if (i < array.length - 1) {
                result += ", ";
            }
        }
        return result;
    }

    public boolean componentExists(String componentName) throws RepositoryException {
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        URI preURI = f.createURI(RESOURCE.URI + "componentName");
        return connection.hasStatement((URI) null, preURI, f.createLiteral(componentName), true);
    }

    public void addComponents(HashMap<String, GeneratedComponent> _components, String tier, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException, MalformedQueryException, QueryEvaluationException {
        Set<String> components = _components.keySet();

        for (String generatedComponentName : components) {
            if (!componentExists(generatedComponentName)) {
                GeneratedComponent generatedComponent = _components.get(generatedComponentName);
                ComponentTreeInfo info = addComponent(tier, generatedComponent.getComponentName(), shouldUpdate);
                Set<String> _providedInterfaces = new HashSet<String>();
                if (generatedComponent.getProvidedInterfacesMap() != null) {
                    _providedInterfaces = generatedComponent.getProvidedInterfacesMap().keySet();
                }
                for (String inter : _providedInterfaces) {
                    setProvidedInterface(info.getId(), inter, "1");
                }

                Set<String> _requiredInterfaces = new HashSet<String>();
                if (generatedComponent.getRequiredInterfacesMap() != null) {
                    _requiredInterfaces = generatedComponent.getRequiredInterfacesMap().keySet();
                }
                for (String inter : _requiredInterfaces) {
                    setRequiredInterface(info.getId(), inter, "1");
                }

                ArrayList<InterfaceDataModel> providedInterfaces = getComponentProvidedInterfaces(info.getId());
                ArrayList<InterfaceDataModel> requiredInterfaces = getComponentRequiredInterfaces(info.getId());
                OpenSMEInterface interfaceAPI = new OpenSMEInterface(api);
                for (InterfaceDataModel providedInterface : providedInterfaces) {
                    String id = providedInterface.getId();
                    Set<MethodSignature> methods = generatedComponent.getMethodsOfInterface(providedInterface.getInterfaceName());
                    if (methods != null) {
                        for (MethodSignature method : methods) {
                            interfaceAPI.addMethod(id, method.getName(), flattenArray(method.getParameters()), method.getReturnType(), flattenArray(method.getThrows()));
                        }
                    }
                }
                for (InterfaceDataModel requiredInterface : requiredInterfaces) {
                    String id = requiredInterface.getId();
                    Set<MethodSignature> methods = generatedComponent.getMethodsOfInterface(requiredInterface.getInterfaceName());
                    if (methods != null) {
                        for (MethodSignature method : methods) {
                            interfaceAPI.addMethod(id, method.getName(), flattenArray(method.getParameters()), method.getReturnType(), flattenArray(method.getThrows()));
                        }
                    }
                }
            }
        }
    }

    public ComponentTreeInfo addComponent(String tier, String name, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {

        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String id = api.getUtils().generateUniqueID(tier.substring(0, 1));

        if (tier.equals("Enterprise")) {
            connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "EComponent"));
        } else if (tier.equals("User")) {
            connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "UComponent"));
        } else if (tier.equals("Resource")) {
            connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "RComponent"));
        } else if (tier.equals("Workspace")) {
            connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "WComponent"));
        } else if (tier.equals("_Unknown")) {
            connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + "_Component"));
        }
        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "componentName"), f.createLiteral(name));
        connection.add(f.createURI(RESOURCE.URI + id), RDFS.LABEL, f.createLiteral(tier));
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.INSERT);
        }

//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setName(name);
//            boolean result = insertComponent(dto, CODE);
//            if (!result) {
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }

        return new ComponentTreeInfo(id, name, tier);
    }

    private String buildGetComponents(String tier) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name \n").
                append("WHERE { \n").
                append("?ID rdf:type ").append("opensme:").append(tier).append("; \n").
                append("opensme:componentName ?Name. \n").
                append("FILTER NOT EXISTS { \n").
                append("?ID rdf:type ?Y. ?Y rdfs:subClassOf opensme:").append(tier).append(". FILTER (isURI(?Y) && ?Y != opensme:").append(tier).append("). }} \n");
        //System.out.println(query.toString());
        return query.toString();
    }

    public ArrayList<TreeNodeData> getComponents(String tier) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        delay(100);
        ArrayList<TreeNodeData> result = new ArrayList<TreeNodeData>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponents(api.getUtils().convertToOntologyClassName(tier));
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            TreeNodeData sum = new TreeNodeData();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String compID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                sum.setId(compID);
                sum.setName(it1.next().getValue().stringValue());

                if (getComponentMetaClass(compID) == null) {
                    sum.setClassified(false);
                } else {
                    sum.setClassified(true);
                }

                result.add(sum);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentName(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:componentName ?Name. } \n");

        return query.toString();
    }

    public String getComponentName(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        String name = "";
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentName(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                name = it1.next().getValue().stringValue();
            }
        }
        evaluate.close();
        return name;
    }

    private String buildGetComponentVersion(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Version \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:version ?Version. } \n");

        return query.toString();
    }

    public String getComponentVersion(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        String version = "";
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentVersion(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                version = it1.next().getValue().stringValue();
            }
        }
        evaluate.close();
        return version;
    }

    private String buildGetComponentSvn(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Svn \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:svn ?Svn. } \n");

        return query.toString();
    }

    public String getComponentSvn(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        String svn = "";
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentSvn(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                svn = it1.next().getValue().stringValue();
            }
        }
        evaluate.close();
        return svn;
    }

    private String buildGetComponentLicense(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?License \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:license ?License. } \n");

        return query.toString();
    }

    public String getComponentLicense(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        String version = "";
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentLicense(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                version = it1.next().getValue().stringValue();
            }
        }
        evaluate.close();
        return version;
    }

    private String buildGetComponentDescription(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?Desc \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:description ?Desc. } \n");

        return query.toString();
    }

    public String getComponentDescription(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        String description = "";
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentDescription(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                description = it1.next().getValue().stringValue();
            }
        }
        evaluate.close();
        return description;
    }

    private String buildGetComponentTechnology(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?TechID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:hasTechnology ?TechID. \n").
                append("?TechID opensme:technologyName ?Name. \n");

        query.append("}");
        return query.toString();
    }

    public KeyValue getComponentTechnology(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        KeyValue result = null;
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentTechnology(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            result = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                result.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                result.setValue(it1.next().getValue().stringValue());
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentLanguage(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?LangID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:hasLanguage ?LangID. \n").
                append("?LangID opensme:languageName ?Name. \n");

        query.append("}");
        return query.toString();
    }

    public KeyValue getComponentLanguage(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        KeyValue result = null;
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentLanguage(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            result = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                result.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                result.setValue(it1.next().getValue().stringValue());
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentUses(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?UsesID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:uses ?UsesID. \n").
                append("?UsesID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?UsesID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        //System.out.println(query.toString());
        return query.toString();
    }

    public ArrayList<ComponentRelationDataModel> getComponentUses(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();

        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentUses(id);
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
                URI preURI = f.createURI(RESOURCE.URI + "uses");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentUsedBy(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?UsedByID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:usedBy ?UsedByID. \n").
                append("?UsedByID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?UsedByID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }

    public ArrayList<ComponentRelationDataModel> getComponentUsedBy(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();

        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentUsedBy(id);
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
                URI preURI = f.createURI(RESOURCE.URI + "usedBy");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentCalls(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?CallsID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:calls ?CallsID. \n").
                append("?CallsID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?CallsID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }

    public ArrayList<ComponentRelationDataModel> getComponentCalls(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();

        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentCalls(id);
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
                URI preURI = f.createURI(RESOURCE.URI + "calls");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentCalledBy(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?CalledByID ?Name ?Version ?Tier \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:calledBy ?CalledByID. \n").
                append("?CalledByID opensme:componentName ?Name; \n").
                append("rdfs:label ?Tier. \n").
                append("OPTIONAL { ?CalledByID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        return query.toString();
    }

    public ArrayList<ComponentRelationDataModel> getComponentCalledBy(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<ComponentRelationDataModel> result = new ArrayList<ComponentRelationDataModel>();

        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentCalledBy(id);
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
                URI preURI = f.createURI(RESOURCE.URI + "calledBy");
                URI objURI = f.createURI(RESOURCE.URI + u.getId());
                u.setExplicit(connection.hasStatement(subURI, preURI, objURI, false));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentProvidedInterfaces(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?InterfaceID ?InterfaceName ?InterfaceVersion \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:providedInterface ?InterfaceID. \n").
                append("?InterfaceID opensme:interfaceName ?InterfaceName; \n").
                append("opensme:interfaceVersion ?InterfaceVersion. \n");

        query.append("} ORDER BY ASC(?InterfaceName) ");
        return query.toString();
    }

    public ArrayList<InterfaceDataModel> getComponentProvidedInterfaces(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<InterfaceDataModel> result = new ArrayList<InterfaceDataModel>();

        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentProvidedInterfaces(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            InterfaceDataModel u = new InterfaceDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setInterfaceName(it1.next().getValue().stringValue());
                u.setVersion(it1.next().getValue().stringValue());

//                OpenSMEInterface i = new OpenSMEInterface(api);
//                u.setMethods(i.getProvidedInterfaceMethods(u.getId()));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentRequiredInterfaces(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?InterfaceID ?InterfaceName ?InterfaceVersion \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme:requiredInterface ?InterfaceID. \n").
                append("?InterfaceID opensme:interfaceName ?InterfaceName; \n").
                append("opensme:interfaceVersion ?InterfaceVersion. \n");

        query.append("} ORDER BY ASC(?InterfaceName) ");
        return query.toString();
    }

    public ArrayList<InterfaceDataModel> getComponentRequiredInterfaces(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<InterfaceDataModel> result = new ArrayList<InterfaceDataModel>();

        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentRequiredInterfaces(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            InterfaceDataModel u = new InterfaceDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                u.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                u.setInterfaceName(it1.next().getValue().stringValue());
                u.setVersion(it1.next().getValue().stringValue());

//                OpenSMEInterface i = new OpenSMEInterface(api);
//                u.setMethods(i.getProvidedInterfaceMethods(u.getId()));
                result.add(u);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetDirectComponentMetaClass(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?MetaClassID ?MetaClassName \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("rdf:type ?MetaClassID. \n").
                append("?MetaClassID rdfs:subClassOf opensme2:COMPONENT-METAMODEL; \n").
                append("rdfs:comment \"leaf\"; \n").
                append("rdfs:label ?MetaClassName. \n");

        query.append("}");
        return query.toString();
    }

    public KeyValue getComponentMetaClass(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        RepositoryConnection connection = api.getDatabase().getConnection();
        KeyValue result = null;

        String query = buildGetDirectComponentMetaClass(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            result = new KeyValue();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                result.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                result.setValue(it1.next().getValue().stringValue());
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentDomain(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?DomainInstanceID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme2:BELONGS-TO-DOMAIN ?DomainInstanceID. \n").
                append("?DomainInstanceID opensme2:DOMAIN-NAME ?Name. \n");

        query.append("}");
        return query.toString();
    }

    private String buildGetComponentConcepts(String id) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ConceptInstanceID ?Name \n").
                append("WHERE { \n").
                append("opensme:").append(id).append(" rdf:type opensme:Component; \n").
                append("opensme2:BELONGS-TO-CONCEPT ?ConceptInstanceID. \n").
                append("?ConceptInstanceID opensme2:CONCEPT-NAME ?Name. \n");

        query.append("}");
        return query.toString();
    }

    public ComponentDetails getComponentDetails(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ComponentDetails details = new ComponentDetails();
        details.setVersion(getComponentVersion(id));
        details.setSvn(getComponentSvn(id));
        details.setLicense(getComponentLicense(id));
        details.setDescription(getComponentDescription(id));
        details.setTechnology(getComponentTechnology(id));
        details.setLanguage(getComponentLanguage(id));
        details.setUses(getComponentUses(id));
        details.setUsedBy(getComponentUsedBy(id));
        details.setCalls(getComponentCalls(id));
        details.setCalledBy(getComponentCalledBy(id));
        details.setProvidedInterfaces(getComponentProvidedInterfaces(id));
        details.setRequiredInterfaces(getComponentRequiredInterfaces(id));

        //get classification details...
        KeyValue metaclass = getComponentMetaClass(id);
        if (metaclass != null) { //it is classified!
            details.setMetaClass(metaclass);

            RepositoryConnection connection = api.getDatabase().getConnection();
            String query = buildGetComponentDomain(id);
            TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult evaluate = prepareGraphQuery.evaluate();
            KeyValue result = null;
            while (evaluate.hasNext()) {
                result = new KeyValue();
                BindingSet next = evaluate.next();
                for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                    result.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                    result.setValue(it1.next().getValue().stringValue());
                }
            }
            evaluate.close();
            if (result != null) {
                details.setDomain(result);
            }

            String query2 = buildGetComponentConcepts(id);
            TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
            TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
            ArrayList<KeyValue> result2 = new ArrayList<KeyValue>();
            while (evaluate2.hasNext()) {
                KeyValue temp = new KeyValue();
                BindingSet next2 = evaluate2.next();
                for (Iterator<Binding> it1 = next2.iterator(); it1.hasNext();) {
                    temp.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                    temp.setValue(it1.next().getValue().stringValue());
                    result2.add(temp);
                }
            }
            evaluate.close();
            if (!result2.isEmpty()) {
                details.setConcepts(result2);
            }
        }
        return details;
    }

    public ComponentDTO getComponentDTO(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ComponentDTO dto = new ComponentDTO();
        dto.setId(id);
        dto.setName(getComponentName(id));
        dto.setLicense(getComponentLicense(id));
        dto.setSvnPath(getComponentSvn(id));
        dto.setDescription(getComponentDescription(id));
        KeyValue tech = getComponentTechnology(id);
        dto.setPlatform(tech == null ? "" : tech.getValue());
        KeyValue lang = getComponentLanguage(id);
        dto.setLanguage(lang == null ? "" : lang.getValue());

        //get classification details...
        KeyValue metaclass = getComponentMetaClass(id);
        if (metaclass != null) { //it is classified!

            RepositoryConnection connection = api.getDatabase().getConnection();
            String query = buildGetComponentDomain(id);
            TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult evaluate = prepareGraphQuery.evaluate();
            KeyValue result = null;
            while (evaluate.hasNext()) {
                result = new KeyValue();
                BindingSet next = evaluate.next();
                for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                    result.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                    result.setValue(it1.next().getValue().stringValue());
                }
            }
            evaluate.close();
            if (result != null) {
                dto.setCategory(result.getValue());
            } else {
                dto.setCategory("");
            }

            String query2 = buildGetComponentConcepts(id);
            TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
            TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
            ArrayList<KeyValue> result2 = new ArrayList<KeyValue>();
            while (evaluate2.hasNext()) {
                KeyValue temp = new KeyValue();
                BindingSet next2 = evaluate2.next();
                for (Iterator<Binding> it1 = next2.iterator(); it1.hasNext();) {
                    temp.setKey(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                    temp.setValue(it1.next().getValue().stringValue());
                    result2.add(temp);
                }
            }
            evaluate.close();
            if (!result2.isEmpty()) {
                dto.setSubcategory(result2.get(0).getValue());
            } else {
                dto.setSubcategory("");
            }
        } else {
            dto.setCategory("");
            dto.setSubcategory("");
        }
        return dto;
    }

    public void deleteComponent(String id, boolean shouldUpdate) throws RepositoryException, MalformedQueryException, QueryEvaluationException, CompareInterfaceException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        connection.remove(compURI, null, null);
        connection.remove((URI) null, null, compURI);

        OpenSMEInterface inter = new OpenSMEInterface(api);
        ArrayList<InterfaceDataModel> componentProvidedInterfaces = getComponentProvidedInterfaces(id);
        for (InterfaceDataModel i : componentProvidedInterfaces) {
            inter.removeProvidedInterface(i.getId(), false);
        }

        ArrayList<InterfaceDataModel> componentRequiredInterfaces = getComponentRequiredInterfaces(id);
        for (InterfaceDataModel i : componentRequiredInterfaces) {
            inter.removeRequiredInterface(i.getId(), false);
        }

        connection.commit();
        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.DELETE);
        }

//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            boolean result = deleteComponent_1(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    public void renameComponent(String id, String newName, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoName = connection.getValueFactory().createURI(RESOURCE.URI + "componentName");
        connection.remove(compURI, compoName, null);
        connection.add(compURI, compoName, connection.getValueFactory().createLiteral(newName));
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }

//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setName(newName);
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }

    }

    public void setVersion(String id, String versionText) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoVersion = connection.getValueFactory().createURI(RESOURCE.URI + "version");

        if (connection.hasStatement(compURI, compoVersion, null, true)) {
            connection.remove(compURI, compoVersion, null);
        }
        connection.add(compURI, compoVersion, connection.getValueFactory().createLiteral(versionText));
        connection.commit();


    }

    public void setSvn(String id, String svnPath, boolean shouldUpdate) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoSvn = connection.getValueFactory().createURI(RESOURCE.URI + "svn");

        if (connection.hasStatement(compURI, compoSvn, null, true)) {
            connection.remove(compURI, compoSvn, null);
        }
        connection.add(compURI, compoSvn, connection.getValueFactory().createLiteral(svnPath));
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }

    }

    public void setLicense(String id, String licenseText, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoLicense = connection.getValueFactory().createURI(RESOURCE.URI + "license");

        if (connection.hasStatement(compURI, compoLicense, null, true)) {
            connection.remove(compURI, compoLicense, null);
        }
        connection.add(compURI, compoLicense, connection.getValueFactory().createLiteral(licenseText));
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }
//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setLicense(licenseText);
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    public void setDescription(String id, String descriptionText, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoDesc = connection.getValueFactory().createURI(RESOURCE.URI + "description");

        if (connection.hasStatement(compURI, compoDesc, null, true)) {
            connection.remove(compURI, compoDesc, null);
        }
        connection.add(compURI, compoDesc, connection.getValueFactory().createLiteral(descriptionText));
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }
//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setLicense(licenseText);
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    public void removeVersion(String id) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoVersion = connection.getValueFactory().createURI(RESOURCE.URI + "version");
        connection.remove(compURI, compoVersion, null);
        connection.commit();

    }

    public void removeSvn(String id, boolean shouldUpdate) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoSvn = connection.getValueFactory().createURI(RESOURCE.URI + "svn");
        connection.remove(compURI, compoSvn, null);
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }
    }

    public void removeLicense(String id, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoLicense = connection.getValueFactory().createURI(RESOURCE.URI + "license");
        connection.remove(compURI, compoLicense, null);
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }

//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setLicense("");
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    public void removeDescription(String id, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI compURI = connection.getValueFactory().createURI(RESOURCE.URI + id);
        URI compoDesc = connection.getValueFactory().createURI(RESOURCE.URI + "description");
        connection.remove(compURI, compoDesc, null);
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }
//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setLicense("");
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    public void setTechnology(String id, String key) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI techURI = f.createURI(RESOURCE.URI + key);
        URI hasTech = f.createURI(RESOURCE.URI + "hasTechnology");

        if (connection.hasStatement(compURI, hasTech, null, true)) {
            connection.remove(compURI, hasTech, null);
        }

        connection.add(compURI, hasTech, techURI);
        connection.commit();
    }

    public void removeTechnology(String id) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI hasTech = f.createURI(RESOURCE.URI + "hasTechnology");
        connection.remove(compURI, hasTech, null);
        connection.commit();
    }

    public void setLanguage(String id, String key, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI langURI = f.createURI(RESOURCE.URI + key);
        URI hasLang = f.createURI(RESOURCE.URI + "hasLanguage");

        if (connection.hasStatement(compURI, hasLang, null, true)) {
            connection.remove(compURI, hasLang, null);
        }

        connection.add(compURI, hasLang, langURI);
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }

//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setLanguage(key);
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    public void removeLanguage(String id, boolean shouldUpdate) throws RepositoryException, CompareInterfaceException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI hasLang = f.createURI(RESOURCE.URI + "hasLanguage");
        connection.remove(compURI, hasLang, null);
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.UPDATE);
        }
//        try {
//            ComponentDTO dto = new ComponentDTO();
//            dto.setId(id);
//            dto.setLanguage("");
//            boolean result = updateComponent(dto, CODE);
//            if(!result){
//                connection.rollback();
//                throw new CompareInterfaceException();
//            }
//        } catch (Exception ex) {
//            connection.rollback();
//            throw new CompareInterfaceException();
//        }
    }

    private String buildGetComponentsInList(
            HashSet<String> keywords,
            ArrayList<String> in,
            String tier,
            String language,
            String technology) {

        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Language ?Technology ?Tier \n").
                append("WHERE { \n");
        if (tier.trim().isEmpty()) {
            query.append("?ID rdf:type opensme:Component; \n");
        } else {
            query.append("?ID rdf:type opensme:").append(tier).append("; \n");
        }
        query.append("rdfs:label ?Tier; \n");
        query.append("opensme:componentName ?Name. \n");
        if (!keywords.isEmpty()) {
            query.append("FILTER ( \n");
            for (Iterator<String> it = keywords.iterator(); it.hasNext();) {
                String description = it.next();
                query.append("( regex(?Name, \"").append(description).append("\", \"i\")) ");
                if (it.hasNext()) {
                    query.append("&& ");
                }
            }
            query.append(").\n");
        }

        if (language.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme:hasLanguage ?LanguageID. \n");
            query.append("?LanguageID opensme:languageName ?Language. }\n");
        } else {
            query.append("?ID opensme:hasLanguage opensme:").append(language).append(". \n");
            query.append("opensme:").append(language).append(" opensme:languageName ?Language. \n");
        }

        if (technology.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme:hasTechnology ?TechnologyID. \n");
            query.append("?TechnologyID opensme:technologyName ?Technology. }\n");
        } else {
            query.append("?ID opensme:hasTechnology opensme:").append(technology).append(". \n");
            query.append("opensme:").append(technology).append(" opensme:technologyName ?Technology. \n");
        }

        query.append("OPTIONAL { \n");
        query.append("?ID opensme:version ?Version. }\n");

        query.append("} ORDER BY ASC(?Name) ");
        //System.out.println(query.toString());
        return query.toString();
    }

    public ArrayList<ComponentListDataModel> getComponentsInList(
            HashSet<String> keywords,
            ArrayList<String> in,
            String tier,
            String language,
            String technology) throws RepositoryException, MalformedQueryException, QueryEvaluationException {


        delay(100);

        ArrayList<ComponentListDataModel> result = new ArrayList<ComponentListDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildGetComponentsInList(
                keywords,
                in,
                api.getUtils().convertToOntologyClassName(tier),
                language,
                technology);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            ComponentListDataModel r = new ComponentListDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                r.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                r.setComponentName(it1.next().getValue().stringValue());
                if (next.hasBinding("Version")) {
                    r.setVersion(it1.next().getValue().stringValue());
                }
                if (next.hasBinding("Language")) {
                    r.setLanguage(it1.next().getValue().stringValue());
                }
                if (next.hasBinding("Technology")) {
                    r.setTechnology(it1.next().getValue().stringValue());
                }
                r.setTier(it1.next().getValue().stringValue());
                result.add(r);
            }
        }
        evaluate.close();
        return result;
    }

    private String buildGetComponentsInSearchList(
            HashSet<String> keywords,
            ArrayList<String> in,
            String tier,
            String language,
            String technology,
            String domain,
            String concept) {

        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Language ?Technology ?Domain (GROUP_CONCAT(?Concept; SEPARATOR=',') as ?Concepts) ?Tier \n").
                append("WHERE { \n");
        if (tier.trim().isEmpty()) {
            query.append("?ID rdf:type opensme:Component; \n");
        } else {
            query.append("?ID rdf:type opensme:").append(tier).append("; \n");
        }
        query.append("rdfs:label ?Tier; \n");
        query.append("opensme:componentName ?Name. \n");
        if (!keywords.isEmpty()) {
            query.append("FILTER ( \n");
            for (Iterator<String> it = keywords.iterator(); it.hasNext();) {
                String description = it.next();
                query.append("( regex(?Name, \"").append(description).append("\", \"i\")) ");
                if (it.hasNext()) {
                    query.append("&& ");
                }
            }
            query.append(").\n");
        }

        if (language.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme:hasLanguage ?LanguageID. \n");
            query.append("?LanguageID opensme:languageName ?Language. }\n");
        } else {
            query.append("?ID opensme:hasLanguage opensme:").append(language).append(". \n");
            query.append("opensme:").append(language).append(" opensme:languageName ?Language. \n");
        }

        if (technology.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme:hasTechnology ?TechnologyID. \n");
            query.append("?TechnologyID opensme:technologyName ?Technology. }\n");
        } else {
            query.append("?ID opensme:hasTechnology opensme:").append(technology).append(". \n");
            query.append("opensme:").append(technology).append(" opensme:technologyName ?Technology. \n");
        }

        query.append("OPTIONAL { \n");
        query.append("?ID opensme:version ?Version. }\n");


        if (domain.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme2:BELONGS-TO-DOMAIN ?DomainInstanceID. \n").
                    append("?DomainInstanceID opensme2:DOMAIN-NAME ?Domain.} \n");
        } else {
            query.append("?ID opensme2:BELONGS-TO-DOMAIN opensme2:").append(domain).append(". \n").
                    append("opensme2:").append(domain).append(" opensme2:DOMAIN-NAME ?Domain. \n");
        }

        if (!concept.equals("-1")) {
            query.append("FILTER EXISTS {?ID opensme2:BELONGS-TO-CONCEPT opensme2:").append(concept).append(". } \n");

        }

        query.append("?ID opensme2:BELONGS-TO-CONCEPT ?ConceptInstanceID. \n").
                append("?ConceptInstanceID opensme2:CONCEPT-NAME ?Concept. \n");


        query.append("} GROUP BY ?ID ?Name ?Version ?Language ?Technology ?Domain ?Tier ORDER BY ASC(?Name) ");
        //System.out.println(query.toString());
        return query.toString();
    }

    private String buildGetComponentsInSearchList2(
            HashSet<String> keywords,
            ArrayList<String> in,
            String tier,
            String language,
            String technology,
            String domain) {

        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?ID ?Name ?Version ?Language ?Technology ?Domain ?Tier \n").
                append("WHERE { \n");
        if (tier.trim().isEmpty()) {
            query.append("?ID rdf:type opensme:Component; \n");
        } else {
            query.append("?ID rdf:type opensme:").append(tier).append("; \n");
        }
        query.append("rdfs:label ?Tier; \n");
        query.append("opensme:componentName ?Name. \n");
        if (!keywords.isEmpty()) {
            query.append("FILTER ( \n");
            for (Iterator<String> it = keywords.iterator(); it.hasNext();) {
                String description = it.next();
                query.append("( regex(?Name, \"").append(description).append("\", \"i\")) ");
                if (it.hasNext()) {
                    query.append("&& ");
                }
            }
            query.append(").\n");
        }

        if (language.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme:hasLanguage ?LanguageID. \n");
            query.append("?LanguageID opensme:languageName ?Language. }\n");
        } else {
            query.append("?ID opensme:hasLanguage opensme:").append(language).append(". \n");
            query.append("opensme:").append(language).append(" opensme:languageName ?Language. \n");
        }

        if (technology.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme:hasTechnology ?TechnologyID. \n");
            query.append("?TechnologyID opensme:technologyName ?Technology. }\n");
        } else {
            query.append("?ID opensme:hasTechnology opensme:").append(technology).append(". \n");
            query.append("opensme:").append(technology).append(" opensme:technologyName ?Technology. \n");
        }

        query.append("OPTIONAL { \n");
        query.append("?ID opensme:version ?Version. }\n");


        if (domain.equals("-1")) {
            query.append("OPTIONAL { \n");
            query.append("?ID opensme2:BELONGS-TO-DOMAIN ?DomainInstanceID. \n").
                    append("?DomainInstanceID opensme2:DOMAIN-NAME ?Domain.} \n");
        } else {
            query.append("?ID opensme2:BELONGS-TO-DOMAIN opensme2:").append(domain).append(". \n").
                    append("opensme2:").append(domain).append(" opensme2:DOMAIN-NAME ?Domain. \n");
        }

        query.append("FILTER NOT EXISTS {?ID opensme2:BELONGS-TO-CONCEPT ?ConceptInstanceID.} \n");

        query.append("} ORDER BY ASC(?Name) ");
        //System.out.println(query.toString());
        return query.toString();
    }

    public ArrayList<SearchComponentDataModel> getComponentsInSearchList(
            HashSet<String> keywords,
            ArrayList<String> in,
            String tier,
            String language,
            String technology,
            String domain,
            String concept) throws RepositoryException, MalformedQueryException, QueryEvaluationException {


        delay(100);

        ArrayList<SearchComponentDataModel> result = new ArrayList<SearchComponentDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();

        //with concepts...
        String query = buildGetComponentsInSearchList(
                keywords,
                in,
                api.getUtils().convertToOntologyClassName(tier),
                language,
                technology,
                domain,
                concept);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            SearchComponentDataModel r = new SearchComponentDataModel();
            BindingSet next = evaluate.next();
            //System.out.println(next);
            //if there is no classified component 
            Binding binding = next.getBinding("Concepts");
            if (binding != null) {
                if (binding.getValue().stringValue().isEmpty()) {
                    break;
                }
            }

            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                r.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                r.setComponentName(it1.next().getValue().stringValue());
                if (next.hasBinding("Version")) {
                    r.setVersion(it1.next().getValue().stringValue());
                }
                if (next.hasBinding("Language")) {
                    r.setLanguage(it1.next().getValue().stringValue());
                }
                if (next.hasBinding("Technology")) {
                    r.setTechnology(it1.next().getValue().stringValue());
                }
                if (next.hasBinding("Domain")) {
                    r.setDomain(it1.next().getValue().stringValue());
                }
                if (next.hasBinding("Concepts")) {
                    r.setConcepts(it1.next().getValue().stringValue());
                }
                r.setTier(it1.next().getValue().stringValue());
                result.add(r);
            }
        }
        evaluate.close();

        if (concept.equals("-1")) {
            ArrayList<SearchComponentDataModel> result2 = new ArrayList<SearchComponentDataModel>();
            String query2 = buildGetComponentsInSearchList2(
                    keywords,
                    in,
                    api.getUtils().convertToOntologyClassName(tier),
                    language,
                    technology,
                    domain);
            TupleQuery prepareGraphQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, query2);
            TupleQueryResult evaluate2 = prepareGraphQuery2.evaluate();
            while (evaluate2.hasNext()) {
                SearchComponentDataModel r = new SearchComponentDataModel();
                BindingSet next = evaluate2.next();
                //System.out.println(next);
                //if there is no classified component 
                Binding binding = next.getBinding("Concepts");
                if (binding != null) {
                    if (binding.getValue().stringValue().isEmpty()) {
                        break;
                    }
                }

                for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                    r.setId(api.getUtils().getLocalName(it1.next().getValue().stringValue()));
                    r.setComponentName(it1.next().getValue().stringValue());
                    if (next.hasBinding("Version")) {
                        r.setVersion(it1.next().getValue().stringValue());
                    }
                    if (next.hasBinding("Language")) {
                        r.setLanguage(it1.next().getValue().stringValue());
                    }
                    if (next.hasBinding("Technology")) {
                        r.setTechnology(it1.next().getValue().stringValue());
                    }
                    if (next.hasBinding("Domain")) {
                        r.setDomain(it1.next().getValue().stringValue());
                    }
                    if (next.hasBinding("Concepts")) {
                        r.setConcepts(it1.next().getValue().stringValue());
                    }
                    r.setTier(it1.next().getValue().stringValue());
                    result2.add(r);
                }
            }
            evaluate2.close();
            result.addAll(result2);
        }

        return result;
    }

    public void setUsesComponents(String id, List<SortedListNameVersionTierDataModel> uses) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : uses) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "uses");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }

    public void removeUses(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "uses");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }

    public void setUsedByComponents(String id, List<SortedListNameVersionTierDataModel> usedBy) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : usedBy) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "usedBy");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }

    public void removeUsedBy(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "usedBy");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }

    public void setCallsComponents(String id, List<SortedListNameVersionTierDataModel> calls) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : calls) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "calls");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }

    public void removeCalls(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "calls");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }

    public void setCalledByComponents(String id, List<SortedListNameVersionTierDataModel> calledBy) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        for (SortedListNameVersionTierDataModel c : calledBy) {
            URI compURI = f.createURI(RESOURCE.URI + id);
            URI usesURI = f.createURI(RESOURCE.URI + "calledBy");
            URI usedC = f.createURI(RESOURCE.URI + c.getId());
            connection.add(compURI, usesURI, usedC);
        }
        connection.commit();
    }

    public void removeCalledBy(String id, String toRemove) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI compURI = f.createURI(RESOURCE.URI + id);
        URI usesURI = f.createURI(RESOURCE.URI + "calledBy");
        URI toREmoveURI = f.createURI(RESOURCE.URI + toRemove);

        connection.remove(compURI, usesURI, toREmoveURI);
        connection.commit();
    }
//    public boolean isExplicit (String sub, String pre, String obj) throws RepositoryException{
//        RepositoryConnection connection = api.getDatabase().getConnection();
//        ValueFactory f = connection.getValueFactory();
//
//        URI subURI = f.createURI(RESOURCE.URI + sub);
//        URI preURI = f.createURI(RESOURCE.URI + pre);
//        URI objURI = f.createURI(RESOURCE.URI +obj);
//        
//        return connection.hasStatement(subURI, preURI, objURI, false);
//    }

    public void setProvidedInterface(String id, String name, String version) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String interID = api.getUtils().generateUniqueID("inter");

        connection.add(f.createURI(RESOURCE.URI + interID), RDF.TYPE, f.createURI(RESOURCE.URI + "Interface"));
        connection.add(f.createURI(RESOURCE.URI + interID), f.createURI(RESOURCE.URI + "interfaceName"), f.createLiteral(name));
        connection.add(f.createURI(RESOURCE.URI + interID), f.createURI(RESOURCE.URI + "interfaceVersion"), f.createLiteral(version));
        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "providedInterface"), f.createURI(RESOURCE.URI + interID));

        connection.commit();
    }

    public void setRequiredInterface(String id, String name, String version) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String interID = api.getUtils().generateUniqueID("inter");

        connection.add(f.createURI(RESOURCE.URI + interID), RDF.TYPE, f.createURI(RESOURCE.URI + "Interface"));
        connection.add(f.createURI(RESOURCE.URI + interID), f.createURI(RESOURCE.URI + "interfaceName"), f.createLiteral(name));
        connection.add(f.createURI(RESOURCE.URI + interID), f.createURI(RESOURCE.URI + "interfaceVersion"), f.createLiteral(version));
        connection.add(f.createURI(RESOURCE.URI + id), f.createURI(RESOURCE.URI + "requiredInterface"), f.createURI(RESOURCE.URI + interID));

        connection.commit();
    }

    public void changeTier(String id, String oldTier, String newTier, boolean shouldUpdate) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String _oldTier = api.getUtils().convertToOntologyClassName(oldTier);
        String _newTier = api.getUtils().convertToOntologyClassName(newTier);

        connection.remove(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + _oldTier));
        connection.remove(f.createURI(RESOURCE.URI + id), RDFS.LABEL, null);
        connection.add(f.createURI(RESOURCE.URI + id), RDF.TYPE, f.createURI(RESOURCE.URI + _newTier));
        connection.add(f.createURI(RESOURCE.URI + id), RDFS.LABEL, f.createLiteral(newTier));

        if (newTier.equals("User")) {
            OpenSMEInterface inter = new OpenSMEInterface(api);
            ArrayList<InterfaceDataModel> componentProvidedInterfaces = getComponentProvidedInterfaces(id);
            for (InterfaceDataModel i : componentProvidedInterfaces) {
                inter.removeProvidedInterface(i.getId(), false);
            }

            ArrayList<InterfaceDataModel> componentRequiredInterfaces = getComponentRequiredInterfaces(id);
            for (InterfaceDataModel i : componentRequiredInterfaces) {
                inter.removeRequiredInterface(i.getId(), false);
            }
        }

        connection.commit();

        if (shouldUpdate) {//old tier is _Unknown
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(id, SYNC.INSERT);
        } else {
            if (newTier.equals("_Unknown")) {
                CompareSynchronizer sync = new CompareSynchronizer();
                sync.setComponentStatus(id, SYNC.DELETE);
            }
        }
    }

    public void addComponentToMetaClass(String componentID, String metaClassID, boolean shouldUpdate) throws RepositoryException, MalformedQueryException, QueryEvaluationException, InvalidTierException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        //check if the classification is valid based on the tier information
        MetaModel meta = new MetaModel(api);
        KeyValue tier = meta.getMetaModelTier(metaClassID);
        if (tier != null) {
            String actualTier = api.getUtils().convertToOntologyClassName(tier.getValue());
            if (!connection.hasStatement(f.createURI(RESOURCE.URI + componentID), RDF.TYPE, f.createURI(RESOURCE.URI + actualTier), true)) {
                throw new InvalidTierException("The component cannot be classified in this meta model (tier violation)");
            }
        }

        connection.add(f.createURI(RESOURCE.URI + componentID), RDF.TYPE, f.createURI(RESOURCE.URI2 + metaClassID));
        connection.commit();

        if (shouldUpdate) {
            CompareSynchronizer sync = new CompareSynchronizer();
            sync.setComponentStatus(componentID, SYNC.UPDATE);
        }
    }

    public void declassifyComponent(String componentID, boolean shouldUpdate) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        KeyValue metaclass = getComponentMetaClass(componentID);
        if (metaclass != null) {
            connection.remove(f.createURI(RESOURCE.URI + componentID), RDF.TYPE, f.createURI(RESOURCE.URI2 + metaclass.getKey()));
            connection.commit();
            if (shouldUpdate) {
                CompareSynchronizer sync = new CompareSynchronizer();
                sync.setComponentStatus(componentID, SYNC.UPDATE);
            }

        }
    }

    private static boolean insertComponent(eu.opensme.cope.knowledgemanager.icompare.ComponentDTO data, java.lang.String key) {
        eu.opensme.cope.knowledgemanager.icompare.Comparewsdl service = new eu.opensme.cope.knowledgemanager.icompare.Comparewsdl();
        eu.opensme.cope.knowledgemanager.icompare.ComparewsdlPortType port = service.getComparewsdlPort();
        return port.insertComponent(data, key);
    }

    private static boolean deleteComponent_1(eu.opensme.cope.knowledgemanager.icompare.ComponentDTO data, java.lang.String key) {
        eu.opensme.cope.knowledgemanager.icompare.Comparewsdl service = new eu.opensme.cope.knowledgemanager.icompare.Comparewsdl();
        eu.opensme.cope.knowledgemanager.icompare.ComparewsdlPortType port = service.getComparewsdlPort();
        return port.deleteComponent(data, key);
    }

    private static boolean updateComponent(eu.opensme.cope.knowledgemanager.icompare.ComponentDTO data, java.lang.String key) {
        eu.opensme.cope.knowledgemanager.icompare.Comparewsdl service = new eu.opensme.cope.knowledgemanager.icompare.Comparewsdl();
        eu.opensme.cope.knowledgemanager.icompare.ComparewsdlPortType port = service.getComparewsdlPort();
        return port.updateComponent(data, key);
    }

    private String buildCallTest() {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?COMPID ?Name ?T (GROUP_CONCAT(?Concept) as ?Concepts) \n").
                append("WHERE { \n").
                append("?COMPID rdf:type opensme:Component; \n").
                append("opensme:componentName ?Name; \n").
                append("opensme2:BELONGS-TO-CONCEPT ?ConceptInstanceID. \n").
                append("?ConceptInstanceID opensme2:CONCEPT-NAME ?Concept. \n");

        query.append("} GROUP BY ?COMPID ?Name ?T ");
        return query.toString();
    }

    public void callTest() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        RepositoryConnection connection = api.getDatabase().getConnection();

        String query = buildCallTest();
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            System.out.println(evaluate.next());
        }
        evaluate.close();
    }
}
