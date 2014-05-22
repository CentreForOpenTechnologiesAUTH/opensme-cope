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
import eu.opensme.cope.knowledgemanager.gui.management.table.MethodDataModel;

public class OpenSMEInterface {

    private ReuseApi api;
    private boolean delay = false;

    public OpenSMEInterface(ReuseApi api) {
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

    private String buildGetProvidedInterfaceMethods(String interfaceID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?MethodID ?Name ?Parameters ?Returns ?Throws \n").
                append("WHERE { \n").
                append("opensme:").append(interfaceID).append(" rdf:type opensme:Interface; \n").
                append("opensme:hasMethod ?MethodID. \n").
                append("?MethodID opensme:methodName ?Name;").
                append("opensme:methodParameters ?Parameters;").
                append("opensme:methodReturns ?Returns;").
                append("opensme:throws ?Throws. } ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<MethodDataModel> getProvidedInterfaceMethods(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<MethodDataModel> result = new ArrayList<MethodDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetProvidedInterfaceMethods(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            MethodDataModel method = new MethodDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String methodID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                method.setId(methodID);
                method.setName(it1.next().getValue().stringValue());
                method.setParameters(it1.next().getValue().stringValue());
                method.setReturns(it1.next().getValue().stringValue());
                method.setThrows(it1.next().getValue().stringValue());
                result.add(method);
            }
        }
        evaluate.close();
        return result;
    }

    public void addMethod(String interfaceID, String name, String parameters, String returns, String thr) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();
        String methID = api.getUtils().generateUniqueID("meth");

        connection.add(f.createURI(RESOURCE.URI + methID), RDF.TYPE, f.createURI(RESOURCE.URI + "Method"));
        connection.add(f.createURI(RESOURCE.URI + methID), f.createURI(RESOURCE.URI + "methodName"), f.createLiteral(name));
        connection.add(f.createURI(RESOURCE.URI + methID), f.createURI(RESOURCE.URI + "methodParameters"), f.createLiteral(parameters));
        connection.add(f.createURI(RESOURCE.URI + methID), f.createURI(RESOURCE.URI + "methodReturns"), f.createLiteral(returns));
        connection.add(f.createURI(RESOURCE.URI + methID), f.createURI(RESOURCE.URI + "throws"), f.createLiteral(thr));
        connection.add(f.createURI(RESOURCE.URI + interfaceID), f.createURI(RESOURCE.URI + "hasMethod"), f.createURI(RESOURCE.URI + methID));

        connection.commit();
    }

    public void removeMethod(String methodID) throws RepositoryException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        URI methodURI = f.createURI(RESOURCE.URI + methodID);

        connection.remove(methodURI, null, null);
        connection.remove((URI) null, null, methodURI);

        connection.commit();
    }

    public void removeProvidedInterface(String interfaceID, boolean commit) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        ArrayList<MethodDataModel> methods = getProvidedInterfaceMethods(interfaceID);
        for (MethodDataModel method : methods) {
            URI methodURI = f.createURI(RESOURCE.URI + method.getId());
            connection.remove(methodURI, null, null);
            connection.remove((URI) null, null, methodURI);
        }

        URI interURI = f.createURI(RESOURCE.URI + interfaceID);
        connection.remove(interURI, null, null);
        connection.remove((URI) null, null, interURI);
        if (commit) {
            connection.commit();
        }
    }

    public void removeRequiredInterface(String interfaceID, boolean commit) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        RepositoryConnection connection = api.getDatabase().getConnection();
        ValueFactory f = connection.getValueFactory();

        ArrayList<MethodDataModel> methods = getRequiredInterfaceMethods(interfaceID);
        for (MethodDataModel method : methods) {
            URI methodURI = f.createURI(RESOURCE.URI + method.getId());
            connection.remove(methodURI, null, null);
            connection.remove((URI) null, null, methodURI);
        }

        URI interURI = f.createURI(RESOURCE.URI + interfaceID);
        connection.remove(interURI, null, null);
        connection.remove((URI) null, null, interURI);
        if (commit) {
            connection.commit();
        }
    }

    public void editInterface(String interfaceID, String name, String version) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI interURI = connection.getValueFactory().createURI(RESOURCE.URI + interfaceID);
        URI prop1 = connection.getValueFactory().createURI(RESOURCE.URI + "interfaceName");
        URI prop2 = connection.getValueFactory().createURI(RESOURCE.URI + "interfaceVersion");

        connection.remove(interURI, prop1, null);
        connection.remove(interURI, prop2, null);
        connection.add(interURI, prop1, connection.getValueFactory().createLiteral(name));
        connection.add(interURI, prop2, connection.getValueFactory().createLiteral(version));
        connection.commit();
    }

    public void editMethod(String methodID, String name, String parameters, String returns, String thr) throws RepositoryException {
        delay(100);

        RepositoryConnection connection = api.getDatabase().getConnection();
        URI methodURI = connection.getValueFactory().createURI(RESOURCE.URI + methodID);
        URI prop1 = connection.getValueFactory().createURI(RESOURCE.URI + "methodName");
        URI prop2 = connection.getValueFactory().createURI(RESOURCE.URI + "methodParameters");
        URI prop3 = connection.getValueFactory().createURI(RESOURCE.URI + "methodReturns");
        URI prop4 = connection.getValueFactory().createURI(RESOURCE.URI + "throws");

        connection.remove(methodURI, prop1, null);
        connection.remove(methodURI, prop2, null);
        connection.remove(methodURI, prop3, null);
        connection.remove(methodURI, prop4, null);
        connection.add(methodURI, prop1, connection.getValueFactory().createLiteral(name));
        connection.add(methodURI, prop2, connection.getValueFactory().createLiteral(parameters));
        connection.add(methodURI, prop3, connection.getValueFactory().createLiteral(returns));
        connection.add(methodURI, prop4, connection.getValueFactory().createLiteral(thr));
        connection.commit();
    }

    private String buildGetRequiredInterfaceMethods(String interfaceID) {
        StringBuilder query = new StringBuilder();
        query.append(api.getUtils().getPrefixes());
        query.append("SELECT DISTINCT ?MethodID ?Name ?Parameters ?Returns ?Throws \n").
                append("WHERE { \n").
                append("opensme:").append(interfaceID).append(" rdf:type opensme:Interface; \n").
                append("opensme:hasMethod ?MethodID. \n").
                append("?MethodID opensme:methodName ?Name;").
                append("opensme:methodParameters ?Parameters;").
                append("opensme:methodReturns ?Returns;").
                append("opensme:throws ?Throws. } ORDER BY ASC(?Name) \n");
        return query.toString();
    }

    public ArrayList<MethodDataModel> getRequiredInterfaceMethods(String id) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        delay(100);
        ArrayList<MethodDataModel> result = new ArrayList<MethodDataModel>();
        RepositoryConnection connection = api.getDatabase().getConnection();
        String query = buildGetRequiredInterfaceMethods(id);
        TupleQuery prepareGraphQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult evaluate = prepareGraphQuery.evaluate();
        while (evaluate.hasNext()) {
            MethodDataModel method = new MethodDataModel();
            BindingSet next = evaluate.next();
            for (Iterator<Binding> it1 = next.iterator(); it1.hasNext();) {
                String methodID = api.getUtils().getLocalName(it1.next().getValue().stringValue());
                method.setId(methodID);
                method.setName(it1.next().getValue().stringValue());
                method.setParameters(it1.next().getValue().stringValue());
                method.setReturns(it1.next().getValue().stringValue());
                method.setThrows(it1.next().getValue().stringValue());
                result.add(method);
            }
        }
        evaluate.close();
        return result;
    }
}
