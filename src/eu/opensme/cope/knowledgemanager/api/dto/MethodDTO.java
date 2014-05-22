package eu.opensme.cope.knowledgemanager.api.dto;

public class MethodDTO {
    private String id;
    private String name;
    private String parameters;
    private String _return;
    private String _throws;

    public MethodDTO() {
    }

    public MethodDTO(String id, String name, String parameters, String _return, String _throws) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
        this._return = _return;
        this._throws = _throws;
    }

    public String getReturn() {
        return _return;
    }

    public void setReturn(String _return) {
        this._return = _return;
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
    
    
}
