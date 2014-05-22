package eu.opensme.cope.knowledgemanager.xml;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public enum SYNC {

    @XmlEnumValue("insert")
    INSERT,
    @XmlEnumValue("update")
    UPDATE,
    @XmlEnumValue("delete")
    DELETE;
}
