package eu.interedition.collatex2.implementation.edit_graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;

// This class represents vertices in the EditGraph
// This class is implemented as an immutable value object
// private fields are final
// toString(), hashCode() and equals methods are overridden
public class EditGraphVertex {
  private final INormalizedToken baseToken;
  private final INormalizedToken witnessToken;
  
  public EditGraphVertex(INormalizedToken witnessToken, INormalizedToken baseToken) {
    this.baseToken = baseToken;
    this.witnessToken = witnessToken;
  }

  public INormalizedToken getBaseToken() {
    return baseToken;
  }

  @Override
  public String toString() {
    if (witnessToken==null||baseToken==null) {
      return "start/end vertex";
    }
    return witnessToken.toString()+"->"+baseToken.toString();
  }
  
  @Override
  public int hashCode() {
    int hc = super.hashCode();
    //TODO: Should it be possible that baseToken is null
    //TODO: it has to do with the start and end tokens who are special
    //TODO: at the moment
    if (baseToken!=null) {
      hc = hc * 59 + baseToken.hashCode();
    }
    if (witnessToken!=null) {
      hc = hc * 59 + witnessToken.hashCode();
    }
//    System.out.println("hashcode called on: "+this.toString()+":"+hc);
    return hc;
  }
  
  @Override
  public boolean equals(final Object obj) {
    //System.out.println(this.toString()+" comparing with "+obj.toString());
    if ((obj != null) && (obj instanceof EditGraphVertex)) {
      final EditGraphVertex vertex = (EditGraphVertex) obj;
      boolean result = true;
      //TODO: Should it be possible that baseToken is null
      //TODO: it has to do with the start and end tokens who are special
      //TODO: at the moment
      if (baseToken!=null) {
        result = result && baseToken.equals(vertex.baseToken);
      }
      if (witnessToken!=null) {
        result = result && witnessToken.equals(vertex.witnessToken);
      }
      //System.out.println("result: "+result);
      return result;
    }
    return false;
  }
}
