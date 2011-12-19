package eu.interedition.collatex.alignment;

import eu.interedition.collatex.ITokenLinker;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.EditGraph;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Comparator;
import java.util.Map;

public class EditGraphTokenLinker implements ITokenLinker {

  private final GraphFactory graphFactory;

  public EditGraphTokenLinker(GraphFactory graphFactory) {
    this.graphFactory = graphFactory;
  }

  @Override
  public Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    final EditGraph editGraph = graphFactory.newEditGraph(base);
    final Map<Token, VariantGraphVertex> linkedTokens = editGraph.build(base, witness, comparator).linkedTokens();
    graphFactory.delete(editGraph);
    return linkedTokens;
  }
}
