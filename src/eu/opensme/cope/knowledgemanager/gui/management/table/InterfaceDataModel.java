package eu.opensme.cope.knowledgemanager.gui.management.table;

public class InterfaceDataModel {

    private String id;
    private String interfaceName;
    private String version;

    public InterfaceDataModel() {
    }

    public InterfaceDataModel(String id, String interfaceName, String version) {
        this.id = id;
        this.interfaceName = interfaceName;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    
}
