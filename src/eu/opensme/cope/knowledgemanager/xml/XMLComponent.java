package eu.opensme.cope.knowledgemanager.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "component")
public class XMLComponent {

    String id;
    SYNC status;

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(String id) {
        this.id = id;
    }

    public SYNC getStatus() {
        return status;
    }

    @XmlElement
    public void setStatus(SYNC status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        XMLComponent o = (XMLComponent) obj;
        if (id.equals(o.getId()) && status.equals(o.getStatus())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 41 * hash + (this.status != null ? this.status.hashCode() : 0);
        return hash;
    }

}
