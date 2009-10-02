package eu.interedition.collatex.alignment.multiple_witness.segments;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class SegmentSuperbase extends Witness {
  private final List<SegmentColumn> columnForEachWord;

  public static SegmentSuperbase create(AlignmentTable3 alignmentTable) {
    SegmentSuperbase superbase = new SegmentSuperbase();
    for (SegmentColumn column : alignmentTable.getColumns()) {
      column.addToSuperbase(superbase);
    }
    return superbase;
  }

  // TODO: could be made private!
  public SegmentSuperbase() {
    this.columnForEachWord = Lists.newArrayList();
  }

  public void addSegment(Segment segment, SegmentColumn segmentColumn) {
    for (Word word : segment.getWords()) {
      Word newWord = new Word("sb", word.original, getWords().size() + 1);
      getWords().add(newWord);
      columnForEachWord.add(segmentColumn);
    }
  }

}