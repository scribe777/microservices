package eu.interedition.collatex2.implementation.edit_graph;

//This class represents edges in the EditGraph
//This class is implemented as an immutable value object
//private fields are final
//toString(), hashCode() and equals methods are overridden
public class EditGraphEdge {

  private final EditGraphVertex sourceVertex;
  private final EditGraphVertex targetVertex;
  private final int weight;

  public EditGraphEdge(EditGraphVertex source, EditGraphVertex target, int weight) {
    this.sourceVertex = source;
    this.targetVertex = target;
    this.weight = weight;
  }

  public EditGraphVertex getSourceVertex() {
    return sourceVertex;
  }

  public EditGraphVertex getTargetVertex() {
    return targetVertex;
  }

  public Integer getWeight() {
    return weight;
  }

  @Override
  public String toString() {
    return "("+sourceVertex+")->("+targetVertex+")";
  }
  
  @Override
  public int hashCode() {
    int hc = super.hashCode();
    hc = hc * 59 + sourceVertex.hashCode();
    hc = hc * 59 + targetVertex.hashCode();
    hc = hc * 59 + weight;
//    System.out.println("hashcode called on: "+this.toString()+":"+hc);
    return hc;
  }
  
  @Override
  public boolean equals(final Object obj) {
    //System.out.println(this.toString()+" comparing with "+obj.toString());
    if ((obj != null) && (obj instanceof EditGraphEdge)) {
      final EditGraphEdge edge = (EditGraphEdge) obj;
      boolean result = true;
      result = result && sourceVertex.equals(edge.sourceVertex);
      result = result && targetVertex.equals(edge.targetVertex);
      //System.out.println("result: "+result);
      return result;
    }
    return false;
  }

}
