package eu.opensme.cope.knowledgemanager.api.dto;

import java.util.ArrayList;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentRelationDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.InterfaceDataModel;

public class BusinessComponentDetails {
    private ArrayList<ComponentRelationDataModel> enterpriseTier;
    private ArrayList<ComponentRelationDataModel> resourceTier;
    private ArrayList<ComponentRelationDataModel> userTier;
    private ArrayList<ComponentRelationDataModel> workspaceTier;
    ArrayList<KeyValue> roles;
    
    public BusinessComponentDetails() {
    }

    public ArrayList<ComponentRelationDataModel> getEnterpriseTier() {
        return enterpriseTier;
    }

    public void setEnterpriseTier(ArrayList<ComponentRelationDataModel> enterpriseTier) {
        this.enterpriseTier = enterpriseTier;
    }

    public ArrayList<ComponentRelationDataModel> getResourceTier() {
        return resourceTier;
    }

    public void setResourceTier(ArrayList<ComponentRelationDataModel> resourceTier) {
        this.resourceTier = resourceTier;
    }

    public ArrayList<ComponentRelationDataModel> getUserTier() {
        return userTier;
    }

    public void setUserTier(ArrayList<ComponentRelationDataModel> userTier) {
        this.userTier = userTier;
    }

    public ArrayList<ComponentRelationDataModel> getWorkspaceTier() {
        return workspaceTier;
    }

    public void setWorkspaceTier(ArrayList<ComponentRelationDataModel> workspaceTier) {
        this.workspaceTier = workspaceTier;
    }

    public ArrayList<KeyValue> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<KeyValue> roles) {
        this.roles = roles;
    }
    
    

    
}
