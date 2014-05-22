package eu.opensme.cope.knowledgemanager.database;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

public class ListStatements {

    public static void main(String[] args) {
        try {
            Database db = new Database();
        } catch (RepositoryException ex) {
            Logger.getLogger(ListStatements.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(ListStatements.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ListStatements.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
