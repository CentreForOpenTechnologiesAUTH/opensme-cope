
package eu.opensme.cope.knowledgemanager.icompare;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;



@WebServiceClient(name = "comparewsdl", targetNamespace = "urn:comparewsdl", wsdlLocation = "http://92.118.11.46:8000//wsdl.php?wsdl")
public class Comparewsdl
    extends Service
{

    private final static URL COMPAREWSDL_WSDL_LOCATION;
    private final static WebServiceException COMPAREWSDL_EXCEPTION;
    private final static QName COMPAREWSDL_QNAME = new QName("urn:comparewsdl", "comparewsdl");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://92.118.11.46:8000//wsdl.php?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        COMPAREWSDL_WSDL_LOCATION = url;
        COMPAREWSDL_EXCEPTION = e;
    }

    public Comparewsdl() {
        super(__getWsdlLocation(), COMPAREWSDL_QNAME);
    }

    public Comparewsdl(WebServiceFeature... features) {
        super(__getWsdlLocation(), COMPAREWSDL_QNAME);
    }

    public Comparewsdl(URL wsdlLocation) {
        super(wsdlLocation, COMPAREWSDL_QNAME);
    }

    public Comparewsdl(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, COMPAREWSDL_QNAME);
    }

    public Comparewsdl(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Comparewsdl(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName);
    }

    /**
     * 
     * @return
     *     returns ComparewsdlPortType
     */
    @WebEndpoint(name = "comparewsdlPort")
    public ComparewsdlPortType getComparewsdlPort() {
        return super.getPort(new QName("urn:comparewsdl", "comparewsdlPort"), ComparewsdlPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ComparewsdlPortType
     */
    @WebEndpoint(name = "comparewsdlPort")
    public ComparewsdlPortType getComparewsdlPort(WebServiceFeature... features) {
        return super.getPort(new QName("urn:comparewsdl", "comparewsdlPort"), ComparewsdlPortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (COMPAREWSDL_EXCEPTION!= null) {
            throw COMPAREWSDL_EXCEPTION;
        }
        return COMPAREWSDL_WSDL_LOCATION;
    }

}
