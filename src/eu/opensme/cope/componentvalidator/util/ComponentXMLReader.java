/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author barius
 */
public class ComponentXMLReader {
    
    public String name = "";
    public String directory = "";
    public List<String> javaFiles = new ArrayList<String>();
    public String execFile = "";
    public boolean junit = false;
    public String aspectJFile = "";
    public List<String> libs = new ArrayList<String>();
    public List<String> classFolders = new ArrayList<String>();
    public List<String> aspectJIncludedInterfaces = new ArrayList<String>();
    public List<String> aspectJExcludedInterfaces = new ArrayList<String>();
    public List<String> aspectJ_IncludedScope =  new ArrayList<String>();
    public List<String> aspectJ_ExcludedScope =  new ArrayList<String>();
    
    public ComponentXMLReader() {
        super();
    }

    public void readComponentProperties(String fileName) {
        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(fileName));

            // normalize text representation
            doc.getDocumentElement().normalize();
            //System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            // System.out.println("Name : " + getTagValue("name", doc.getDocumentElement()));
            name = getTagValue("name", doc.getDocumentElement()).replaceAll(" ", "_");
            
            // System.out.println("Directory : " + getTagValue("directory", doc.getDocumentElement()));
            directory = getTagValue("directory", doc.getDocumentElement());

            Node nNode = doc.getDocumentElement().getElementsByTagName("java_files").item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                NodeList nList = eElement.getElementsByTagName("java_file");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element javaFileElement = (Element) nList.item(s);
                    Node javaFileNode = javaFileElement.getChildNodes().item(0);    
                    //System.out.println("Java File : "+ javaFileNode.getNodeValue().trim());
                    javaFiles.add(javaFileNode.getNodeValue().trim());
                    
                }//end of for loop with s var

            }//end of if clause

            //System.out.println("Execute file : " + getTagValue("execfile", doc.getDocumentElement()));
            execFile = getTagValue("execfile", doc.getDocumentElement());
            //System.out.println("Junit file : " + getTagValue("execfileJUnit", doc.getDocumentElement()));
            if (getTagValue("execfileJUnit", doc.getDocumentElement()).equals("yes"))
                junit =  true;
            else 
                junit =  false;
            //System.out.println("AspectJ file : " + getTagValue("aspectJ_file", doc.getDocumentElement()));
            //aspectJFile = getTagValue("aspectJ_file", doc.getDocumentElement());
            aspectJFile  = System.getProperty("user.dir") + "/compiled/"+name+".aj"; 

            nNode = doc.getDocumentElement().getElementsByTagName("libs").item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                NodeList nList = eElement.getElementsByTagName("lib");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element javaFileElement = (Element) nList.item(s);
                    Node javaFileNode = javaFileElement.getChildNodes().item(0);    
                    //System.out.println("Lib : "+ javaFileNode.getNodeValue().trim());
                    libs.add(javaFileNode.getNodeValue().trim());
                    
                }//end of for loop with s var

            }//end of if clause

            nNode = doc.getDocumentElement().getElementsByTagName("classfolders").item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                NodeList nList = eElement.getElementsByTagName("classfolder");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element javaFileElement = (Element) nList.item(s);
                    Node javaFileNode = javaFileElement.getChildNodes().item(0);    
                    //System.out.println("Class Folder : "+ javaFileNode.getNodeValue().trim());
                    classFolders.add(javaFileNode.getNodeValue().trim());
                    
                }//end of for loop with s var

            }//end of if clause

            nNode = doc.getDocumentElement().getElementsByTagName("aspectJ_Interface").item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                NodeList nList = eElement.getElementsByTagName("included");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element includedElement = (Element) nList.item(s);
                    Node includedNode = includedElement.getChildNodes().item(0);    
                    //System.out.println("Java File : "+ javaFileNode.getNodeValue().trim());
                    aspectJIncludedInterfaces.add(includedNode.getNodeValue().trim());
                    
                }//end of for loop with s var
                
                nList = eElement.getElementsByTagName("excluded");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element excludedElement = (Element) nList.item(s);
                    Node excludedNode = excludedElement.getChildNodes().item(0);    
                    //System.out.println("Java File : "+ javaFileNode.getNodeValue().trim());
                    aspectJExcludedInterfaces.add(excludedNode.getNodeValue().trim());
                    
                }//end of for loop with s var                

            }//end of if clause

            nNode = doc.getDocumentElement().getElementsByTagName("aspectJ_Scope").item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                NodeList nList = eElement.getElementsByTagName("in_scope");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element includedElement = (Element) nList.item(s);
                    Node includedNode = includedElement.getChildNodes().item(0);    
                    //System.out.println("Java File : "+ javaFileNode.getNodeValue().trim());
                    aspectJ_IncludedScope.add(includedNode.getNodeValue().trim());
                    
                }//end of for loop with s var
 
                nList = eElement.getElementsByTagName("out_scope");

                for (int s = 0; s < nList.getLength(); s++) {
                    Element excludedElement = (Element) nList.item(s);
                    Node excludedNode = excludedElement.getChildNodes().item(0);    
                    //System.out.println("Java File : "+ javaFileNode.getNodeValue().trim());
                    aspectJ_ExcludedScope.add(excludedNode.getNodeValue().trim());
                    
                }//end of for loop with s var                

            }//end of if clause
            
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
        //System.exit (0);
    }

    private String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);

        return nValue.getNodeValue();
    }
}
