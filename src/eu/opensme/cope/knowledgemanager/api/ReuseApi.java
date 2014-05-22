package eu.opensme.cope.knowledgemanager.api;

import eu.opensme.cope.knowledgemanager.database.Database;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import eu.opensme.cope.knowledgemanager.utils.Utils;

public class ReuseApi {

    private Database database;
    private Utils utils;

    public ReuseApi() throws RepositoryException, RepositoryConfigException, Exception {
        database = new Database();
        utils = new Utils(database.getConnection());

    }

    public Utils getUtils() {
        return utils;
    }

    public Database getDatabase() {
        return database;
    }

}
