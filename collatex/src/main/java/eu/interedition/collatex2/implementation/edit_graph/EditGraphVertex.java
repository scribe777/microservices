package eu.interedition.collatex2.implementation.edit_graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class EditGraphVertex {

  private final INormalizedToken baseToken;

  public EditGraphVertex(INormalizedToken witnessToken, INormalizedToken baseToken) {
    this.baseToken = baseToken;
  }

  public INormalizedToken getBaseToken() {
    return baseToken;
  }

  @Override
  public String toString() {
    return baseToken.toString();
  }
}
