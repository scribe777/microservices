package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.StrictEqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchTableLinkerTest extends AbstractTest {

  @Test
  //Note: test taken from HermansTest
  public void testHermansText2c() throws XMLStreamException {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer.";
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer.";
    String textDMD1 = "Over de Atlantische Oceaan voer een vreselijk grote stomer.";
    SimpleWitness[] witnesses = createWitnesses(textD1, textD9, textDMD1);

    VariantGraph graph = collate(witnesses[0], witnesses[1]);

    MatchTableLinker linker = new MatchTableLinker(1);
    Map<Token, VariantGraphVertex> linkedTokens = linker.link(graph, witnesses[2], new EqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    System.out.println(tokensAsString);
    assertTrue(tokensAsString.contains("C:0:'over'"));
    assertTrue(tokensAsString.contains("C:1:'de'"));
    assertTrue(tokensAsString.contains("C:2:'atlantische'"));
    assertTrue(tokensAsString.contains("C:3:'oceaan'"));
    assertTrue(tokensAsString.contains("C:4:'voer'"));
    assertTrue(tokensAsString.contains("C:5:'een'"));
    assertTrue(tokensAsString.contains("C:7:'grote'"));
    assertTrue(tokensAsString.contains("C:8:'stomer'"));
  }

  //  String newLine = System.getProperty("line.separator");

  @Test
  public void test1() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTableLinker linker = new MatchTableLinker(1);
    Map<Token, VariantGraphVertex> linkedTokens = linker.link(vg, sw[1], new EqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    assertTrue(tokensAsString.contains("B:0:'a'"));
    assertTrue(tokensAsString.contains("B:1:'b'"));
    assertTrue(tokensAsString.contains("B:2:'c'"));
    assertTrue(tokensAsString.contains("B:3:'a'"));
    assertTrue(tokensAsString.contains("B:4:'b'"));
  }

  @Test
  public void testOverDeAtlantischeOceaan() {
    int outlierTranspositionsSizeLimit = 1;
    collationAlgorithm = CollationAlgorithmFactory.dekkerMatchMatrix(new StrictEqualityTokenComparator(), outlierTranspositionsSizeLimit);
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/> Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
    String textDMD1 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/>\nOp sommige dekken van de stomer lagen mensen in de zon, op andere dekken werd getennist, op nog andere liepen de passagiers heen en weer en praatten. Wie over de reling hing en recht naar beneden keek, kon vaststellen dat het schip vorderde; of draaide alleen de aarde er onderdoor?<p/>\nOp de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.<p/>";
    SimpleWitness[] sw = createWitnesses(textD9, textDMD1);
    VariantGraph vg = collate(sw[0]);
    Map<Token, VariantGraphVertex> linkedTokens = new MatchTableLinker(outlierTranspositionsSizeLimit).link(vg, sw[1], new StrictEqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    List<String> l = Lists.newArrayList(tokensAsString);
    Collections.sort(l);
    LOG.info("tokensAsString={}", l);
    assertTrue(tokensAsString.contains("B:75:'onder'"));
    assertTrue(tokensAsString.contains("B:0:'over'"));
    assertTrue(tokensAsString.contains("B:1:'de'"));
    assertTrue(tokensAsString.contains("B:2:'atlantische'"));
    assertTrue(tokensAsString.contains("B:3:'oceaan'"));
    assertTrue(tokensAsString.contains("B:4:'voer'"));
  }
}
