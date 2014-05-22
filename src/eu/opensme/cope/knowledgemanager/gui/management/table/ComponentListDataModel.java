package eu.opensme.cope.knowledgemanager.gui.management.table;

public class ComponentListDataModel {

    private String id;
    private String componentName;
    private String version;
    private String tier;
    private String technology;
    private String language;

    public ComponentListDataModel() {
        version = "";
        technology = "";
        language = "";
    }

    public ComponentListDataModel(String id, String componentName, String version, String tier, String technology, String language) {
        this.id = id;
        this.componentName = componentName;
        this.version = version;
        this.tier = tier;
        this.technology = technology;
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
//        if (tier.equals("E")) {
//            this.tier = "Enterprise";
//        } else if (tier.equals("R")) {
//            this.tier = "Resource";
//        } else if (tier.equals("U")) {
//            this.tier = "User";
//        } else if (tier.equals("W")) {
//            this.tier = "Workspace";
//        } else if (tier.equals("_")) {
//            this.tier = "_Unknown";
//        } else {
//            throw new RuntimeException();
//        }
        this.tier = tier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
