package com.tinkerpop.gremlin.model.parser;

import com.tinkerpop.gremlin.model.Edge;
import com.tinkerpop.gremlin.model.Graph;
import com.tinkerpop.gremlin.model.Vertex;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class GraphMLReader {

    public static void inputGraph(Graph graph, InputStream graphMLInputStream) throws XMLStreamException {

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(graphMLInputStream);

        Map<String, String> keyTypes = new HashMap<String, String>();
        Vertex currentVertex = null;
        Edge currentEdge = null;

        while (reader.hasNext()) {

            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String elementName = reader.getName().getLocalPart();
                if (elementName.equals(GraphMLTokens.KEY)) {
                    String attributeName = reader.getAttributeValue(null, GraphMLTokens.ATTR_NAME);
                    String attributeType = reader.getAttributeValue(null, GraphMLTokens.ATTR_TYPE);
                    keyTypes.put(attributeName, attributeType);
                } else if (elementName.equals(GraphMLTokens.NODE)) {
                    String vertexId = reader.getAttributeValue(null, GraphMLTokens.ID);
                    currentVertex = graph.getVertex(vertexId);
                    if(null == currentVertex)
                        currentVertex = graph.addVertex(vertexId);

                } else if (elementName.equals(GraphMLTokens.EDGE)) {
                    String edgeId = reader.getAttributeValue(null, GraphMLTokens.ID);
                    String edgeLabel = reader.getAttributeValue(null, GraphMLTokens.LABEL);
                    String outId = reader.getAttributeValue(null, GraphMLTokens.SOURCE);
                    String inId = reader.getAttributeValue(null, GraphMLTokens.TARGET);

                    // TODO: current edge get by id first?

                    Vertex outVertex = graph.getVertex(outId);
                    Vertex inVertex = graph.getVertex(inId);
                    if (null == outVertex) {
                        outVertex = graph.addVertex(outId);
                    }
                    if (null == inVertex) {
                        inVertex = graph.addVertex(inId);
                    }
                    currentEdge = graph.addEdge(edgeId, outVertex, inVertex, edgeLabel);
                } else if (elementName.equals(GraphMLTokens.DATA)) {
                    String key = reader.getAttributeValue(null, GraphMLTokens.KEY);
                    String value = reader.getElementText();
                    if (currentVertex != null) {
                        currentVertex.setProperty(key, typeCastValue(key, value, keyTypes));
                    } else if (currentEdge != null) {
                        currentEdge.setProperty(key, typeCastValue(key, value, keyTypes));
                    }
                }
            } else if (eventType.equals(XMLEvent.END_ELEMENT)) {
                String elementName = reader.getName().getLocalPart();
                if (elementName.equals(GraphMLTokens.NODE))
                    currentVertex = null;
                else if (elementName.equals(GraphMLTokens.EDGE))
                    currentEdge = null;

            }
        }
        reader.close();
    }

    public static Object typeCastValue(String key, String value, Map<String, String> keyTypes) {
        String type = keyTypes.get(key);
        if (null == type || type.equals(GraphMLTokens.STRING))
            return value;
        else if (type.equals(GraphMLTokens.FLOAT))
            return Float.valueOf(value);
        else if (type.equals(GraphMLTokens.INT))
            return Integer.valueOf(value);
        else if (type.equals(GraphMLTokens.DOUBLE))
            return Double.valueOf(value);
        else if (type.equals(GraphMLTokens.BOOLEAN))
            return Boolean.valueOf(value);
        else if (type.equals(GraphMLTokens.LONG))
            return Long.valueOf(value);
        else
            return value;
    }
}
