package eu.interedition.text.graph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.QNames;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

import static eu.interedition.text.graph.TextRelationshipType.ANNOTATES;
import static eu.interedition.text.graph.TextRelationshipType.NAMES;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphAnnotationRepository extends AbstractAnnotationRepository {

  private GraphDatabaseService db;
  private QNameRepository nameRepository;

  private final GraphQueryCriteriaTranslator queryCriteriaTranslator = new GraphQueryCriteriaTranslator();

  @Required
  public void setGraphDataSource(GraphDataSource ds) {
    this.db = ds.getGraphDatabaseService();
  }

  @Required
  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  @Override
  public Iterable<Annotation> create(Iterable<Annotation> annotations) {
    final List<Annotation> created = Lists.newArrayListWithExpectedSize(Iterables.size(annotations));
    for (Annotation a : annotations) {
      final Range range = a.getRange();

      final Node annotationNode = db.createNode();
      annotationNode.setProperty(GraphAnnotation.PROP_RANGE_START, range.getStart());
      annotationNode.setProperty(GraphAnnotation.PROP_RANGE_END, range.getEnd());
      annotationNode.createRelationshipTo(((GraphText) a.getText()).getNode(), ANNOTATES);
      ((GraphQName) nameRepository.get(a.getName())).getNode().createRelationshipTo(annotationNode, NAMES);
      created.add(new GraphAnnotation(annotationNode));
    }
    return created;
  }

  @Override
  public Iterable<Annotation> find(Criterion criterion) {
    db.index().forNodes("annotations").query(queryCriteriaTranslator.toQuery(criterion));
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Iterable<Annotation> annotations) {
    for (GraphAnnotation a : Iterables.filter(annotations, GraphAnnotation.class)) {
      final Node node = a.getNode();
      node.getSingleRelationship(ANNOTATES, OUTGOING).delete();
      node.getSingleRelationship(NAMES, INCOMING).delete();
      node.delete();
    }
  }

  @Override
  public void delete(Criterion criterion) {
    delete(find(criterion));
  }

  @Override
  public Map<Annotation, Map<QName, String>> get(Iterable<Annotation> links, Set<QName> names) {
    final ArrayList<QName> nameList = Lists.newArrayList(names);

    final QName[] attrQNames = nameList.toArray(new QName[nameList.size()]);
    final String[] attrNames = Lists.transform(nameList, QNames.TO_STRING).toArray(new String[nameList.size()]);

    final Map<Annotation, Map<QName, String>> result = Maps.newLinkedHashMap();
    for (GraphAnnotation a : Iterables.filter(links, GraphAnnotation.class)) {
      final Map<QName, String> attributes = Maps.newLinkedHashMap();
      final Node node = a.getNode();
      for (int ac = 0; ac < attrNames.length; ac++) {
        final String av = (String) node.getProperty(attrNames[ac]);
        if (av != null) {
          attributes.put(attrQNames[ac], av);
        }
      }
      result.put(a, attributes);
    }
    return result;
  }

  @Override
  public void set(Map<Annotation, Map<QName, String>> data) {
    for (GraphAnnotation a : Iterables.filter(data.keySet(), GraphAnnotation.class)) {
      final Node node = a.getNode();
      final Map<QName, String> attributes = data.get(a);
      for (QName an : attributes.keySet()) {
        node.setProperty(an.toString(), attributes.get(an));
      }
    }
  }

  @Override
  public void unset(Map<Annotation, Iterable<QName>> data) {
    for (GraphAnnotation a : Iterables.filter(data.keySet(), GraphAnnotation.class)) {
      final Node node = a.getNode();
      for (QName an : data.get(a)) {
        node.removeProperty(an.toString());
      }
    }
  }

  @Override
  protected SortedSet<QName> getNames(Text text) {
    SortedSet<QName> names = Sets.newTreeSet();
    for (Relationship ar : ((GraphText) text).getNode().getRelationships(ANNOTATES, INCOMING)) {
      names.add(new GraphAnnotation(ar.getStartNode()).getName());
    }
    return names;
  }
}