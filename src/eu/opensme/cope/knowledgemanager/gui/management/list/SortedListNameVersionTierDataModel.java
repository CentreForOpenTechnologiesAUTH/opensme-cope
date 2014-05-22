package eu.opensme.cope.knowledgemanager.gui.management.list;

public class SortedListNameVersionTierDataModel {

    String id, name, version, tier;

    public SortedListNameVersionTierDataModel() {
    }

    public SortedListNameVersionTierDataModel(String id, String name, String version, String tier) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.tier = tier;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }
    

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getTier() {
        return tier;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SortedListNameVersionTierDataModel) {
            SortedListNameVersionTierDataModel kv = (SortedListNameVersionTierDataModel) obj;
            return (kv.id.equals(this.id));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
