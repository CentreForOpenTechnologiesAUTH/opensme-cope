package eu.opensme.cope.knowledgemanager.utils;

import java.util.UUID;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import eu.opensme.cope.knowledgemanager.resources.RESOURCE;

public class Utils {

    private RepositoryConnection connection;
    private ValueFactory f;

    public Utils(RepositoryConnection connection) {
        this.connection = connection;
        f = connection.getValueFactory();
    }

    public String getPrefixes() {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n "
                + "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                + "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n"
                + "PREFIX opensme2:  <" + RESOURCE.URI2 + "> \n"
                + "PREFIX opensme:  <" + RESOURCE.URI + "> \n\n");
        return query.toString();
    }

    public URI getIRISClass(String id) {
        return f.createURI(RESOURCE.URI + id);
    }

    public URI getIRISProperty(String id) {
        return f.createURI(RESOURCE.URI + id);
    }

    public URI getIndividual(String id) {
        return f.createURI(RESOURCE.URI + id);
    }

    public String generateUniqueID(String prefix) {
        UUID id = UUID.randomUUID();
        return id.toString();
    }

    public String getLocalName(String uri) {
        int lastIndexOf = uri.lastIndexOf("#");
        return uri.substring(lastIndexOf + 1, uri.length());
    }

    public String convertToOntologyClassName(String name) {
        if (name.equals("Enterprise")) {
            return "EComponent";
        } else if (name.equals("User")) {
            return "UComponent";
        } else if (name.equals("Resource")) {
            return "RComponent";
        } else if (name.equals("Workspace")) {
            return "WComponent";
        } else if (name.equals("_Unknown")) {
            return "_Component";
        } else {
            return "";
        }
    }
}
