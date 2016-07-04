package eu.interedition.collatex.simple;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.neo4j.graphdb.Transaction;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphEdge;
import eu.interedition.collatex.graph.VariantGraphTransposition;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.nio.charset.Charset;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;
import java.util.Set;
import java.util.SortedMap;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleVariantGraphSerializer {

  private static String dotPath = "/usr/bin/dot";

  public static void setDotPath(String path) { dotPath = path; }

  /**
   * CollateX custom namespace.
   */
  protected static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";

  /**
   * The TEI P5 namespace.
   */
  protected static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

  private final VariantGraph graph;

  public SimpleVariantGraphSerializer(VariantGraph graph) {
    this.graph = graph;
  }

  public void toTEI(XMLStreamWriter xml) throws XMLStreamException {
    final Set<Witness> allWitnesses = graph.witnesses();

    xml.writeStartDocument();
    xml.writeStartElement("cx", "apparatus", COLLATEX_NS);
    xml.writeNamespace("cx", COLLATEX_NS);
    xml.writeNamespace("", TEI_NS);

    for (Iterator<Set<VariantGraphVertex>> rowIt = graph.join().rank().adjustRanksForTranspositions().ranks().iterator(); rowIt.hasNext();) {
      final Set<VariantGraphVertex> row = rowIt.next();

      final SetMultimap<Witness, Token> tokenIndex = HashMultimap.create();
      for (VariantGraphVertex v : row) {
        for (Token token : v.tokens()) {
          tokenIndex.put(token.getWitness(), token);
        }
      }

      final SortedMap<Witness, String> cellContents = Maps.newTreeMap(Witness.SIGIL_COMPARATOR);
      for (Witness witness : tokenIndex.keySet()) {
        final StringBuilder cellContent = new StringBuilder();
        for (SimpleToken token : Ordering.natural().sortedCopy(Iterables.filter(tokenIndex.get(witness), SimpleToken.class))) {
          cellContent.append(token.getContent()).append(" ");
        }
        cellContents.put(witness, cellContent.toString().trim());
      }

      final SetMultimap<String, Witness> segments = LinkedHashMultimap.create();
      for (Map.Entry<Witness, String> cell : cellContents.entrySet()) {
        segments.put(cell.getValue(), cell.getKey());
      }

      final String firstSegment = Iterables.getFirst(segments.keySet(), "");
      if (segments.keySet().size() == 1 && segments.get(firstSegment).size() == allWitnesses.size()) {
        xml.writeCharacters(firstSegment);
      } else {
        xml.writeStartElement("", "app", TEI_NS);
        for (String segment : segments.keySet()) {
          final StringBuilder witnesses = new StringBuilder();
          for (Witness witness : segments.get(segment)) {
            witnesses.append(witness.getSigil()).append(" ");
          }
          xml.writeStartElement("", "rdg", TEI_NS);
          xml.writeAttribute("wit", witnesses.toString().trim());
          xml.writeCharacters(segment);
          xml.writeEndElement();
        }
        xml.writeEndElement();
      }

      if (rowIt.hasNext()) {
        xml.writeCharacters(" ");
      }
    }

    xml.writeEndElement();
    xml.writeEndDocument();
  }

  public void toPlainTextApparatus(Writer out) throws XMLStreamException {
    final Set<Witness> allWitnesses = graph.witnesses();

    for (Iterator<Set<VariantGraphVertex>> rowIt = graph.join().rank().ranks().iterator(); rowIt.hasNext(); ) {
      final Set<VariantGraphVertex> row = rowIt.next();

      final SetMultimap<Witness, Token> tokenIndex = HashMultimap.create();
      for (VariantGraphVertex v : row) {
        for (Token token : v.tokens()) {
          tokenIndex.put(token.getWitness(), token);
        }
      }

      final SortedMap<Witness, String> cellContents = Maps.newTreeMap(Witness.SIGIL_COMPARATOR);
      for (Witness witness : tokenIndex.keySet()) {
        final StringBuilder cellContent = new StringBuilder();
        for (SimpleToken token : Ordering.natural().sortedCopy(Iterables.filter(tokenIndex.get(witness), SimpleToken.class))) {
          cellContent.append(token.getContent()).append(" ");
        }
        cellContents.put(witness, cellContent.toString().trim());
      }

      final SetMultimap<String, Witness> segments = LinkedHashMultimap.create();
      for (Map.Entry<Witness, String> cell : cellContents.entrySet()) {
        segments.put(cell.getValue(), cell.getKey());
      }

      final String firstSegment = Iterables.getFirst(segments.keySet(), "");
      try {
        if (segments.keySet().size() == 1 && segments.get(firstSegment).size() == allWitnesses.size()) {
          // don't output if all witnesses agree
        } else {
	  int count = 0;
          for (String segment : segments.keySet()) {
		  final StringBuilder witnesses = new StringBuilder();
		  for (Witness witness : segments.get(segment)) {
		    witnesses.append(witness.getSigil()).append(" ");
		  }
		  if (count > 1) out.write(",  ");
		  out.write(segment);
		  if (count == 0) {
			out.write(" ] ");
		  }
		  else {
			out.write(" ");
			out.write(witnesses.toString().trim());
		  }
                  ++count;
            }
            out.write("\n");
        }
      }
      catch (Exception e) { e.printStackTrace(); }
    }
  }

  public void toGraphML(XMLStreamWriter xml) throws XMLStreamException {
    try {
    xml.writeStartDocument();
    xml.writeStartElement("", GRAPHML_TAG, GRAPHML_NS);
    xml.writeNamespace("", GRAPHML_NS);
    xml.writeAttribute(XMLNSXSI_ATT, GRAPHML_XMLNSXSI);
    xml.writeAttribute(XSISL_ATT, GRAPHML_XSISL);

    for (GraphMLProperty p : GraphMLProperty.values()) {
      p.declare(xml);
    }

    xml.writeStartElement(GRAPHML_NS, GRAPH_TAG);
    xml.writeAttribute(ID_ATT, GRAPH_ID);
    xml.writeAttribute(EDGEDEFAULT_ATT, EDGEDEFAULT_DEFAULT_VALUE);
    xml.writeAttribute(PARSENODEIDS_ATT, PARSENODEIDS_DEFAULT_VALUE);
    xml.writeAttribute(PARSEEDGEIDS_ATT, PARSEEDGEIDS_DEFAULT_VALUE);
    xml.writeAttribute(PARSEORDER_ATT, PARSEORDER_DEFAULT_VALUE);

    final Map<VariantGraphVertex, String> vertexToId = Maps.newHashMap();
    int vertexNumber = 0;
    for (VariantGraphVertex vertex : graph.vertices()) {
      final String vertexNodeID = "n" + vertexNumber;
      xml.writeStartElement(GRAPHML_NS, NODE_TAG);
      xml.writeAttribute(ID_ATT, vertexNodeID);
      GraphMLProperty.NODE_NUMBER.write(Integer.toString(vertexNumber++), xml);
      GraphMLProperty.NODE_TOKEN.write(VariantGraphVertex.TO_CONTENTS.apply(vertex), xml);
      xml.writeEndElement();
      vertexToId.put(vertex, vertexNodeID);
    }

    int edgeNumber = 0;
    for (VariantGraphEdge edge : graph.edges()) {
      xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
      xml.writeAttribute(ID_ATT, "e" + edgeNumber);
      String id = vertexToId.get(edge.from());
      if (id != null) {
	      xml.writeAttribute(SOURCE_ATT, id);
      }
      else {
    	  System.out.println("null attributed id for: " + edge.from());
      }
	
      id = vertexToId.get(edge.to());
      if (id != null) {
	      xml.writeAttribute(TARGET_ATT, id);
      }
      else {
    	  System.out.println("null attributed id for: " + edge.to());
      }
      GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
      GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_PATH, xml);
      GraphMLProperty.EDGE_WITNESSES.write(VariantGraphEdge.TO_CONTENTS.apply(edge), xml);
      xml.writeEndElement();
    }
    for (VariantGraphTransposition transposition : graph.transpositions()) {
      xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
      xml.writeAttribute(ID_ATT, "e" + edgeNumber);
      String id = vertexToId.get(transposition.from());
      if (id != null) {
	      xml.writeAttribute(SOURCE_ATT, id);
      }
      else {
    	  System.out.println("null attributed id for: " + transposition.from());
      }
      id = vertexToId.get(transposition.to());
      if (id != null) {
	      xml.writeAttribute(TARGET_ATT, id);
      }
      else {
    	  System.out.println("null attributed id for: " + transposition.to());
      }
      GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
      GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_TRANSPOSITION, xml);
      xml.writeEndElement();

    }

    xml.writeEndElement();

    xml.writeEndElement();
    xml.writeEndDocument();
    } finally {
    }
  }

  private String toLabel(VariantGraphEdge e) {
    return toLabel(e, false);
  }
  public static int LABEL_MAX_WITNESSES = 8;
  public static int LABEL_MAX_ROW = 4;
  private String toLabel(VariantGraphEdge e, boolean full) {
    StringBuffer label = new StringBuffer();
    Vector<String> witnesses = new Vector<String>();
    for (Witness w : e.witnesses()) witnesses.add(w.getSigil());

    java.util.Collections.sort(witnesses, new Comparator<String>() {
	public int compare(String w1, String w2) {
		if (w1.startsWith("P") && !w2.startsWith("P")) return -1;
		if (w1.startsWith("0") && !w2.startsWith("0")) return -1;
		if (w2.startsWith("l") && !w1.startsWith("l")) return 1;
		return w1.compareTo(w2);
	}
    });

// Collapse corrector hand into original hand if they are both the same
    for (int i = 0; i < witnesses.size(); ++i) {
        if (witnesses.get(i) != null) {
          String sig1 = witnesses.get(i);
	  if (sig1.indexOf("-*") > -1) {
            boolean collapse = false;
            String main = sig1.substring(0, sig1.indexOf('-')).trim();
            for (int j = 0; j < witnesses.size(); ++j) {
                if (i != j && witnesses.get(j) != null) {
                  String sig2 = witnesses.get(j);
                  if (sig2.indexOf('-') > -1) {
                    String pair = sig2.substring(0, sig2.indexOf('-')).trim();
                    if (main.equals(pair)) {
                        collapse = true;
                        witnesses.set(j, null); }
                  }
                }
            }
            if (collapse) witnesses.set(i, main);
          }
        }
    }

    int count = 0;
    for (int i = 0; i < witnesses.size(); ++i) {
      if (witnesses.get(i) != null) {
        ++count;
        if (full || count <= LABEL_MAX_WITNESSES) {
          if (count > 1) { label.append(", ");if (!full && count % LABEL_MAX_ROW == 1) label.append("\\n"); }
          String sig = witnesses.get(i).trim();
          if (sig.length() > 0) {
            label.append(witnesses.get(i).trim());
          }
        }
      }
    }
    if (!full && count > LABEL_MAX_WITNESSES) {
      label.append(" ... ("+count+")");
    }

    String retVal = label.toString().replaceAll("\"", "\\\"");
System.out.println(retVal);
    return retVal;
  }

  public void toDot(Writer writer) {
    try {
      final PrintWriter out = new PrintWriter(writer);
      final String indent = "  ";
      final String connector = " -> ";

      out.println("digraph G {");

      Iterator<VariantGraphVertex> iv = graph.vertices().iterator();
      int nodeCount = 0;
      while (iv.hasNext()) {
        VariantGraphVertex v = iv.next();
        ++nodeCount;
        out.print(indent + "v" + v.getNode().getId());
        out.print(" [label=\"" + ((nodeCount == 1)?"#START#\",tooltip=\"#START#":(!iv.hasNext()?"#END#\",tooltip=\"#END#":toLabel(v))) + "\"]");
        out.println(";");
      }

      for (VariantGraphEdge e : graph.edges()) {
        out.print(indent + "v" + e.from().getNode().getId() + connector + "v" + e.to().getNode().getId());
        out.print(" [label=\"" + (toLabel(e)+"\",tooltip=\""+toLabel(e,true)) + "\"]");
        out.println(";");
      }

      for (VariantGraphTransposition t : graph.transpositions()) {
        out.print(indent + "v" + t.from().getNode().getId() + connector + "v" + t.to().getNode().getId());
        out.print(" [color=\"lightgray\", style=\"dashed\" arrowhead=\"none\", arrowtail=\"none\" ]");
        out.println(";");
      }

      out.println("}");

      out.flush();
    } finally {
    }
  }

  public void toSVG(Writer out) throws IOException {
    Process dotProc = Runtime.getRuntime().exec(dotPath + " -Grankdir=LR -Gid=VariantGraph -Tsvg");
    Writer dotWriter = null;
    try {
      toDot(dotWriter = new OutputStreamWriter(dotProc.getOutputStream(), Charset.forName("UTF-8")));
    } finally {
      if (dotWriter != null) dotWriter.close();
    }
	char[] buffer = new char[8192];
	int len = 0;
	InputStreamReader is = new InputStreamReader(dotProc.getInputStream(), "UTF-8");
	while ((len = is.read(buffer)) > -1) {
		out.write(buffer, 0, len);
	}
  }

  private static final String NODE_TAG = "node";
  private static final String TARGET_ATT = "target";
  private static final String SOURCE_ATT = "source";
  private static final String EDGE_TAG = "edge";
  private static final String EDGE_TYPE_PATH = "path";
  private static final String EDGE_TYPE_TRANSPOSITION = "transposition";
  private static final String EDGEDEFAULT_DEFAULT_VALUE = "directed";
  private static final String EDGEDEFAULT_ATT = "edgedefault";
  private static final String GRAPH_ID = "g0";
  private static final String GRAPH_TAG = "graph";
  private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
  private static final String GRAPHML_TAG = "graphml";
  private static final String XMLNSXSI_ATT = "xmlns:xsi";
  private static final String XSISL_ATT = "xsi:schemaLocation";
  private static final String GRAPHML_XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
  private static final String GRAPHML_XSISL = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
  private static final String PARSENODEIDS_ATT = "parse.nodeids";
  private static final String PARSENODEIDS_DEFAULT_VALUE = "canonical";
  private static final String PARSEEDGEIDS_ATT = "parse.edgeids";
  private static final String PARSEEDGEIDS_DEFAULT_VALUE = "canonical";
  private static final String PARSEORDER_ATT = "parse.order";
  private static final String PARSEORDER_DEFAULT_VALUE = "nodesfirst";

  private static final String ATTR_TYPE_ATT = "attr.type";
  private static final String ATTR_NAME_ATT = "attr.name";
  private static final String FOR_ATT = "for";
  private static final String ID_ATT = "id";
  private static final String KEY_TAG = "key";
  private static final String DATA_TAG = "data";

  private enum GraphMLProperty {
    NODE_NUMBER(NODE_TAG, "number", "int"), //
    NODE_TOKEN(NODE_TAG, "tokens", "string"), //
    EDGE_NUMBER(EDGE_TAG, "number", "int"), //
    EDGE_TYPE(EDGE_TAG, "type", "string"), //
    EDGE_WITNESSES(EDGE_TAG, "witnesses", "string");

    private String name;
    private String forElement;
    private String type;

    private GraphMLProperty(String forElement, String name, String type) {
      this.name = name;
      this.forElement = forElement;
      this.type = type;
    }

    public void write(String data, XMLStreamWriter xml) throws XMLStreamException {
      xml.writeStartElement(GRAPHML_NS, DATA_TAG);
      xml.writeAttribute(KEY_TAG, "d" + ordinal());
      xml.writeCharacters(data);
      xml.writeEndElement();
    }

    public void declare(XMLStreamWriter xml) throws XMLStreamException {
      xml.writeEmptyElement(GRAPHML_NS, KEY_TAG);
      xml.writeAttribute(ID_ATT, "d" + ordinal());
      xml.writeAttribute(FOR_ATT, forElement);
      xml.writeAttribute(ATTR_NAME_ATT, name);
      xml.writeAttribute(ATTR_TYPE_ATT, type);
    }
  }

  public void toDot(VariantGraph graph, Writer writer) {
    final Transaction tx = graph.newTransaction();
    try {
      final PrintWriter out = new PrintWriter(writer);
      final String indent = "  ";
      final String connector = " -> ";

      out.println("digraph G {");

      for (VariantGraphVertex v : graph.vertices()) {
        out.print(indent + "v" + v.getNode().getId());
        out.print(" [label = \"" + toLabel(v) + "\"]");
        out.println(";");
      }

      for (VariantGraphEdge e : graph.edges()) {
        out.print(indent + "v" + e.from().getNode().getId() + connector + "v" + e.to().getNode().getId());
        out.print(" [label = \"" + toLabel(e) + "\"]");
        out.println(";");
      }

      for (VariantGraphTransposition t : graph.transpositions()) {
        out.print(indent + "v" + t.from().getNode().getId() + connector + "v" + t.to().getNode().getId());
        out.print(" [color = \"lightgray\", style = \"dashed\" arrowhead = \"none\", arrowtail = \"none\" ]");
        out.println(";");
      }

      out.println("}");

      out.flush();
      tx.success();
    } finally {
      tx.finish();
    }
  }


  private String toLabel(VariantGraphVertex v) {
    return VariantGraphVertex.TO_CONTENTS.apply(v).replaceAll("\"", "\\\"");
  }
}
