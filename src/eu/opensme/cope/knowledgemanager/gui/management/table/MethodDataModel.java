package eu.opensme.cope.knowledgemanager.gui.management.table;

public class MethodDataModel {

    private String id;
    private String name;
    private String parameters;
    private String returns;
    private String _throws;

    public MethodDataModel() {
    }

    public MethodDataModel(String id, String name, String parameters, String returns, String _throws) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
        this.returns = returns;
        this._throws = _throws;
    }

    public String getThrows() {
        return _throws;
    }

    public void setThrows(String _throws) {
        this._throws = _throws;
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }
    
    

    
}
