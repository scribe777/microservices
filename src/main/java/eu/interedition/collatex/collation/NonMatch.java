package eu.interedition.collatex.collation;

import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;

import eu.interedition.collatex.visualization.Modification;

// TODO: rename class to Gap!
public class NonMatch {
  final Phrase base;
  final Phrase witness;

  public NonMatch(Phrase _base, Phrase _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  // TODO: rename method -- it does return a Phrase, not a Witness
  public Phrase getBase() {
    return base;
  }

  // TODO: rename method -- it does return a Gap, not a Witness
  public Phrase getWitness() {
    return witness;
  }

  Addition createAddition() {
    return new Addition(base.getStartPosition(), witness);
  }

  Omission createOmission() {
    return new Omission(base);
  }

  Replacement createReplacement() {
    return new Replacement(base, witness);
  }

  public boolean isAddition() {
    return !base.hasGap() && witness.hasGap();
  }

  boolean isOmission() {
    return base.hasGap() && !witness.hasGap();
  }

  public boolean isReplacement() {
    return base.hasGap() && witness.hasGap();
  }

  public boolean isValid() {
    return base.hasGap() || witness.hasGap();
  }

  @Override
  public String toString() {
    String result = "NonMatch: addition: " + isAddition() + " base: " + base;
    if (base.isAtTheEnd()) {
      result += "; nextWord: none";
    } else {
      result += "; nextWord: " + base.getNextWord();
    }
    result += "; witness: " + witness;
    return result;
  }

  public Modification analyse() {
    if (isAddition()) {
      return createAddition();
    }
    if (isOmission()) {
      return createOmission();
    }
    if (isReplacement()) {
      return createReplacement();
    }
    throw new RuntimeException("Not a modification!");
  }

}
