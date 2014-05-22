package eu.opensme.cope.knowledgemanager.api.actions;

import eu.opensme.cope.knowledgemanager.api.ReuseApi;
import java.io.File;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class Import {
    private ReuseApi api;
    private boolean delay = false;

    public Import(ReuseApi api) {
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

    public void CleanAndLoad(File ontology) throws RepositoryException {
        RepositoryConnection connection = api.getDatabase().getConnection();
        connection.clear();
        connection.commit();
        
        api.getDatabase().loadOntology(ontology);
    }
    
}
