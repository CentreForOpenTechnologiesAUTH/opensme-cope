package eu.opensme.cope.knowledgemanager.xml;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "repository")
public class XMLRepository {

    @XmlElement(name = "component")
    private ArrayList<XMLComponent> components;

    public XMLRepository() {
        components = new ArrayList<XMLComponent>();
    }

    public ArrayList<XMLComponent> getComponentList() {
        return components;
    }

    public void addComponent(String id, SYNC status) {
        XMLComponent comp = new XMLComponent();
        comp.setId(id);
        comp.setStatus(status);
        if (!components.contains(comp)) {
            components.add(comp);
        }
    }

    public void clear() {
        components.clear();
    }
}
