package eu.opensme.cope.knowledgemanager.gui.management.list;

public class SortedListKeyValuePatternDataModel {

    String key, value, patternID, patternName;
    boolean explicit;

    public SortedListKeyValuePatternDataModel() {
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
    public SortedListKeyValuePatternDataModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getPatternID() {
        return patternID;
    }

    public void setPatternID(String patternID) {
        this.patternID = patternID;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }
    

    @Override
    public String toString() {
        return value + " (" + patternName + ")";
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

   
    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SortedListKeyValuePatternDataModel) {
            SortedListKeyValuePatternDataModel kv = (SortedListKeyValuePatternDataModel) obj;
            return (kv.value.equals(this.value));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
