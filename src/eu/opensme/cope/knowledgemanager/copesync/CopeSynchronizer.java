package eu.opensme.cope.knowledgemanager.copesync;

import eu.opensme.cope.domain.GeneratedComponent;
import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.knowledgemanager.api.CompareInterfaceException;
import eu.opensme.cope.knowledgemanager.api.ReuseApi;
import eu.opensme.cope.knowledgemanager.api.actions.OpenSMEComponent;
import eu.opensme.cope.knowledgemanager.gui.management.Management;
import eu.opensme.cope.knowledgemanager.utils.MockRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

/**
 *
 * @author George
 */
public class CopeSynchronizer {

    private ReuseProject reuseProject;

    public CopeSynchronizer(ReuseProject reuseProject) {
        this.reuseProject = reuseProject;
    }

    private HashMap<String, GeneratedComponent> getComponents() {
//        MockRepository mock = new MockRepository(1, 1, 1, 1, 0);
//        return mock.init();
        if (reuseProject != null) {
            return reuseProject.getGeneratedComponents();
        } else {
            return null;
        }

    }

    public void start() {
        HashMap<String, GeneratedComponent> components = getComponents();
        if (components == null) {
            return;
        }

        ReuseApi api = getAPI();
        OpenSMEComponent comp = new OpenSMEComponent(api);
        try {
            comp.addComponents(components, "_Unknown", false);
        } catch (RepositoryException ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CompareInterfaceException ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }
    }

    private ReuseApi getAPI() {
        ReuseApi api = null;
        try {
            api = new ReuseApi();
        } catch (RepositoryException ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return api;
        }
    }
}
