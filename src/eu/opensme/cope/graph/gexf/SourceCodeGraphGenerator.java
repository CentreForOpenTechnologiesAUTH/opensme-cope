/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.graph.gexf;

import com.ojn.gexf4j.core.EdgeType;
import com.ojn.gexf4j.core.Gexf;
import com.ojn.gexf4j.core.Node;
import com.ojn.gexf4j.core.data.AttributeClass;
import com.ojn.gexf4j.core.data.AttributeList;
import com.ojn.gexf4j.core.impl.GexfImpl;
import com.ojn.gexf4j.core.impl.StaxGraphWriter;
import com.ojn.gexf4j.core.impl.data.AttributeListImpl;
import eu.opensme.cope.recommenders.entities.ClassAnalysis;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author krap
 */
public class SourceCodeGraphGenerator {

    private Gexf graph;
    private List<Node> nodes;
    private long edgesCounter;

    public SourceCodeGraphGenerator(Date lastModifiedDate, String creator, Boolean isDirected) {

        //Generic init
        graph = new GexfImpl();
        nodes = new ArrayList<Node>();
        edgesCounter = 0;

        //Generic initialization for the graph
        graph.getMetadata().setLastModified(lastModifiedDate).setCreator(creator).setDescription("Component's Graph");
        if (isDirected) {
            graph.getGraph().setDefaultEdgeType(EdgeType.DIRECTED);
        } else {
            graph.getGraph().setDefaultEdgeType(EdgeType.UNDIRECTED);
        }

        //Attribute initialization
        AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
        graph.getGraph().getAttributeLists().add(attrList);

        //Attribute attClassType = attrList.createAttribute("0", AttributeType.STRING, "Class Type");

    }

    public void addNode(String label) {
        long index = 0;

        if (!nodes.isEmpty()) {
            index = nodes.size();
        }

        Node tempNode = graph.getGraph().createNode("" + index);
        tempNode.setLabel(label);//.getAttributeValues().addValue(attUrl, "http://gephi.org").addValue(attIndegree, "1");
        nodes.add(tempNode);
    }

    public void addEdge(ClassAnalysis caSource, ClassAnalysis caTarget) {

        Node source = getNodeFromClassAnalysis(caSource);
        Node target = getNodeFromClassAnalysis(caTarget);

        if (target != null) {
            source.connectTo("" + edgesCounter, target).setEdgeType(EdgeType.DIRECTED);
            edgesCounter++;
        }
    }

    private Node getNodeFromClassAnalysis(ClassAnalysis ca) {
        Node resNode = null;

        Iterator it2 = nodes.iterator();
        while (it2.hasNext()) {
            Node node = (Node) it2.next();
            if (ca.getName().equals(node.getLabel())) {
                resNode = node;
            }
        }
        return resNode;
    }

    public void exportGraphToFile(String path) {
        
        File checkFile = new File(path).getParentFile();
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }

        StaxGraphWriter graphWriter = new StaxGraphWriter();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            graphWriter.writeToStream(graph, out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
