/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.xmlparser;

/**
 *
 * @author Ampatzoglou
 */
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParser {

    public void readXML(String arg) {


        try {
            FileWriter fstream = new FileWriter(arg + ".txt");
            BufferedWriter out = new BufferedWriter(fstream);
            File file = new File(arg);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("pattern");

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElmnt = (Element) fstNode;
                    String aaa = fstElmnt.getAttribute("name");

                    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("instance");

                    for (int s1 = 0; s1 < fstNmElmntLst.getLength(); s1++) {

                        Node fstNode1 = fstNmElmntLst.item(s1);

                        if (fstNode1.getNodeType() == Node.ELEMENT_NODE) {

                            Element fstElmnt1 = (Element) fstNode1;
                            NodeList fstNmElmntLst1 = fstElmnt1.getElementsByTagName("role");

                            String aaa3 = "";

                            for (int s2 = 0; s2 < fstNmElmntLst1.getLength(); s2++) {

                                Node fstNode2 = fstNmElmntLst1.item(s2);

                                if (fstNode2.getNodeType() == Node.ELEMENT_NODE) {

                                    Element fstElmnt2 = (Element) fstNode2;
                                    String aaa1 = fstElmnt2.getAttribute("name");
                                    String aaa2 = fstElmnt2.getAttribute("element");
                                    if (aaa1.equals("Creator")
                                            || aaa1.equals("Client")
                                            || aaa1.equals("Prototype")
                                            || aaa1.equals("Singleton")
                                            || aaa1.equals("Adaptee/Receiver")
                                            || aaa1.equals("Adapter/ConcreteCommand")
                                            || aaa1.equals("Creator")
                                            || aaa1.equals("Component")
                                            || aaa1.equals("Decorator")
                                            || aaa1.equals("Context")
                                            || aaa1.equals("State/Strategy")
                                            || aaa1.equals("AbstractClass")
                                            || aaa1.equals("TemplateMethod")
                                            || aaa1.equals("RealSubject")
                                            || aaa1.equals("Proxy")
                                            || aaa1.equals("Composite")
                                            || aaa1.equals("Subject")
                                            || aaa1.equals("Observer")
                                            || aaa1.equals("Visitor")
                                            || aaa1.equals("Facade")
                                            || aaa1.equals("HiddenType")
                                            || aaa1.equals("Abstract Factory")
                                            || aaa1.equals("Abstraction")
                                            || aaa1.equals("Implementor")
                                            || aaa1.equals("Handler")
                                            || aaa1.equals("Product")
                                            || aaa1.equals("Factory")
                                            || aaa1.equals("ConcreteElement")
                                            || aaa1.equals("Mediator")
                                            || aaa1.equals("Subclass")
                                            || aaa1.equals("State")
                                            || aaa1.equals("Strategy")
                                            || aaa1.equals("Abstract Class")
                                            || aaa1.equals("Element")
                                            || aaa1.equals("Flyweight")
                                            || aaa1.equals("Colleague")) {
                                        aaa3 = aaa3 + ";" + aaa1 + ";" + aaa2;
                                    }
                                }
                            }
                            out.write(arg + ";" + aaa + aaa3 + "\n");
                        }
                    }
                }
            }
            out.close();

//      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
//      NodeList fstNm = fstNmElmnt.getChildNodes();
//      System.out.println("First Name : "  + ((Node) fstNm.item(0)).getNodeValue());
//      NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("lastname");
            //Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
            // NodeList lstNm = lstNmElmnt.getChildNodes();
            // System.out.println("Last Name : " + ((Node) lstNm.item(0)).getNodeValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}