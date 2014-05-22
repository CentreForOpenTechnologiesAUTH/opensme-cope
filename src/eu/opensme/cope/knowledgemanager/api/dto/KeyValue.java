package eu.opensme.cope.knowledgemanager.api.dto;

public class KeyValue {

    String key, value;
    boolean explicit;

    public KeyValue() {
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

   
    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyValue) {
            KeyValue kv = (KeyValue) obj;
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
