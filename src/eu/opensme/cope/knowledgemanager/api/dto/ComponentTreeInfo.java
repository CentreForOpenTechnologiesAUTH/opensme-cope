package eu.opensme.cope.knowledgemanager.api.dto;

public class ComponentTreeInfo {

    private String id;
    private String name;
    private String tier;

    public ComponentTreeInfo() {
    }

    public ComponentTreeInfo(String id, String name, String tier) {
        this.id = id;
        this.name = name;
        this.tier = tier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }
}
