package com.sd_editions.collatex.match_spike;

import junit.framework.TestCase;

public class WordCoordinateTest extends TestCase {

  public final void testWordCoordinate() {
    WordCoordinate c = new WordCoordinate(1, 2);
    assertEquals(1, c.getWitnessNumber());
    assertEquals(2, c.getPositionInWitness());
  }

}