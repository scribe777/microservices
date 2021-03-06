package eu.interedition.server.collatex;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Transactional
public class VariantGraphSerializer extends JsonSerializer<VariantGraph> {

  @Override
  public void serialize(VariantGraph graph, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    final List<Witness> witnesses = Ordering.from(Witness.SIGIL_COMPARATOR).sortedCopy(graph.witnesses());
    final RowSortedTable<Integer,Witness,Set<Token>> table = graph.toTable();

    jgen.writeStartObject();

    // switch rows and columns due to horizontal content orientation
    jgen.writeNumberField("columns", table.rowKeySet().size());
    jgen.writeNumberField("rows", table.columnKeySet().size());
    jgen.writeArrayFieldStart("sigils");

    for (Witness witness : witnesses) {
      jgen.writeString(witness.getSigil());
    }
    jgen.writeEndArray();


    jgen.writeArrayFieldStart("table");
    for (Integer row : table.rowKeySet()) {
      final Map<Witness,Set<Token>> cells = table.row(row);
      jgen.writeStartArray();
      for (Witness witness : witnesses) {
        final Set<Token> cellContents = cells.get(witness);
        if (cellContents == null) {
          jgen.writeNull();
        } else {
          final List<SimpleToken> cell = Ordering.natural().sortedCopy(Iterables.filter(cellContents, SimpleToken.class));
          jgen.writeStartArray();
          for (SimpleToken token : cell) {
            if (token instanceof WebToken) {
              jgen.writeTree(((WebToken) token).getJsonNode());
            } else {
              jgen.writeString(token.getContent());
            }
          }
          jgen.writeEndArray();
        }

      }
      jgen.writeEndArray();
    }
    jgen.writeEndArray();

    jgen.writeEndObject();
  }
}
