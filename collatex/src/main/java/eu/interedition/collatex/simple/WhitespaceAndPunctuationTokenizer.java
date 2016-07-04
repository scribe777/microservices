package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class WhitespaceAndPunctuationTokenizer implements Function<String, List<String>> {

  @Override
  public List<String> apply(String input) {
    final List<String> tokens = Lists.newArrayList();
    final StringTokenizer tokenizer = new StringTokenizer(input.trim(), " ,.-?;:\n", true);
    boolean inSeg = false;
    String segToken = "";
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (inSeg) {
        if (token.indexOf("))") > -1) {
          token = segToken + token.replaceFirst(Pattern.quote("))"), "");
          inSeg = false;
        }
        else {
          segToken += token;
          continue;
        }
      }
      else if (token.indexOf("((") > -1) {
        segToken = token.replaceFirst(Pattern.quote("(("), "");
        inSeg = true;
        continue;
      }
      token = token.trim();
      if (token.length() > 0) {
        tokens.add(token);
      }
    }
    return tokens;
  }
}
