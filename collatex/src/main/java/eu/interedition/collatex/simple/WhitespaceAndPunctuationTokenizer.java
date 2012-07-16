package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.StringTokenizer;

public class WhitespaceAndPunctuationTokenizer implements Function<String, List<String>> {

  @Override
  public List<String> apply(String input) {
    final List<String> tokens = Lists.newArrayList();
    final StringTokenizer tokenizer = new StringTokenizer(input.trim(), " ,.-?;:\n", true);
    boolean inApp = false;
    String appToken = "";
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (inApp) {
        if (token.indexOf("}") > -1) {
          token = appToken + token;
          inApp = false;
        }
        else {
          appToken += token;
          continue;
        }
      }
      else if (token.indexOf("{") > -1) {
        appToken = token;
        inApp = true;
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
