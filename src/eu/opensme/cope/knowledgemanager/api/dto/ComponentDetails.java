package eu.opensme.cope.knowledgemanager.api.dto;

import java.util.ArrayList;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentRelationDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.InterfaceDataModel;

public class ComponentDetails {
    private String version;
    private KeyValue technology;
    private KeyValue language;
    private String license;
    private String description;
    private String svn;
    private ArrayList<ComponentRelationDataModel> uses;
    private ArrayList<ComponentRelationDataModel> usedBy;
    private ArrayList<ComponentRelationDataModel> calls;
    private ArrayList<ComponentRelationDataModel> calledBy;
    private ArrayList<InterfaceDataModel> providedInterfaces;
    private ArrayList<InterfaceDataModel> requiredInterfaces;
    
    private KeyValue metaClass;
    private KeyValue domain;
    private ArrayList<KeyValue> concepts;

    public ComponentDetails() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSvn() {
        return svn;
    }

    public void setSvn(String svn) {
        this.svn = svn;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public KeyValue getTechnology() {
        return technology;
    }

    public void setTechnology(KeyValue technology) {
        this.technology = technology;
    }

    public KeyValue getLanguage() {
        return language;
    }

    public void setLanguage(KeyValue language) {
        this.language = language;
    }

    public void setUses(ArrayList<ComponentRelationDataModel> uses) {
        this.uses = uses;
    }

    public ArrayList<ComponentRelationDataModel> getUses() {
        return uses;
    }

    public ArrayList<ComponentRelationDataModel> getCalledBy() {
        return calledBy;
    }

    public void setCalledBy(ArrayList<ComponentRelationDataModel> calledBy) {
        this.calledBy = calledBy;
    }

    public ArrayList<ComponentRelationDataModel> getCalls() {
        return calls;
    }

    public void setCalls(ArrayList<ComponentRelationDataModel> calls) {
        this.calls = calls;
    }

    public ArrayList<ComponentRelationDataModel> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(ArrayList<ComponentRelationDataModel> usedBy) {
        this.usedBy = usedBy;
    }

    public ArrayList<InterfaceDataModel> getProvidedInterfaces() {
        return providedInterfaces;
    }

    public void setProvidedInterfaces(ArrayList<InterfaceDataModel> providedInterfaces) {
        this.providedInterfaces = providedInterfaces;
    }

    public ArrayList<InterfaceDataModel> getRequiredInterfaces() {
        return requiredInterfaces;
    }

    public void setRequiredInterfaces(ArrayList<InterfaceDataModel> requiredInterfaces) {
        this.requiredInterfaces = requiredInterfaces;
    }

    public KeyValue getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(KeyValue metaClass) {
        this.metaClass = metaClass;
    }

    public ArrayList<KeyValue> getConcepts() {
        return concepts;
    }

    public void setConcepts(ArrayList<KeyValue> concepts) {
        this.concepts = concepts;
    }

    public KeyValue getDomain() {
        return domain;
    }

    public void setDomain(KeyValue domain) {
        this.domain = domain;
    }
    

}
