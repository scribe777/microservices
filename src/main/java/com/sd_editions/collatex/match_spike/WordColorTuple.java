package com.sd_editions.collatex.match_spike;

public class WordColorTuple {
  public final String color;
  public final String word;

  public WordColorTuple(String word1, String color1) {
    this.word = word1;
    this.color = color1;
  }

  public String toHtml() {
    return "<span class=\"" + color + "\">" + word + "</span>";
  }
}