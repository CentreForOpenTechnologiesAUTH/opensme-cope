package eu.opensme.cope.knowledgemanager.api.dto;

public class DomainTreeInfo {

    private String id;
    private String name;
    //private String superclass;

    public DomainTreeInfo() {
    }

    public DomainTreeInfo(String id, String name) {
        this.id = id;
        this.name = name;
        //this.superclass = superclass;
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

//    public String getSuperclass() {
//        return superclass;
//    }
//
//    public void setSuperclass(String superclass) {
//        this.superclass = superclass;
//    }
}
