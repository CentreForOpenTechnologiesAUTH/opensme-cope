package eu.opensme.cope.knowledgemanager.database;

import eu.opensme.cope.knowledgemanager.gui.configtool.ConfigUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import eu.opensme.cope.knowledgemanager.resources.RESOURCE;
import java.io.File;

public class Database {

    private RepositoryManager repositoryManager;
    private Repository repository;
    private RepositoryConnection repositoryConnection;

    public Database() throws RepositoryException, RepositoryConfigException, Exception {
        
        //repositoryManager = new RemoteRepositoryManager("http://localhost:8084/openrdf-sesame");
        //repositoryManager = new RemoteRepositoryManager("http://irisportal.csd.auth.gr:8084/openrdf-sesame");

        Properties property = ConfigUtils.readPropertyFile();
        repositoryManager = new RemoteRepositoryManager(property.getProperty("URL"));
        repositoryManager.initialize();
        repository = repositoryManager.getRepository(property.getProperty("Name"));
        repositoryConnection = repository.getConnection();
        repositoryConnection.setAutoCommit(false);
        

        //listStatements2();
    }

    public void close() {
        try {
            repositoryConnection.close();
            repository.shutDown();
            repositoryManager.shutDown();
//            if(Management.progressBar != null){
//                Management.progressBar.dispose();
//            }
        } catch (Exception e) {
            System.out.println("An exception occurred during shutdown: " + e.getMessage());
        }
    }

    public void loadOntology(File ontology) {
        try {
            Reader reader = null;
            reader = new BufferedReader(new FileReader(ontology), 1024 * 1024);
            repositoryConnection.add(reader, RESOURCE.URI, RDFFormat.RDFXML);
            repositoryConnection.commit();
            this.close();
        } catch (RDFParseException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.close();
        }
    }

    public RepositoryConnection getConnection() {
        return repositoryConnection;
    }

    private void listStatements2() {
        try {
            ValueFactory f = repositoryConnection.getValueFactory();
            RepositoryResult<Statement> statements = repositoryConnection.getStatements(null, null, null, true);
            while (statements.hasNext()) {
                Statement next = statements.next();
                System.out.println(next.getSubject() + " " + next.getPredicate() + " " + next.getObject());
            }
            close();
            System.exit(1);
        } catch (RepositoryException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
