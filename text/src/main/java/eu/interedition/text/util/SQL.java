/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text.util;

import com.google.common.collect.AbstractIterator;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SQL {
  public static String select(String tableName, String... columns) {
    final StringBuilder select = new StringBuilder();
    for (int cc = 0; cc < columns.length; cc++) {
      final String column = columns[cc];
      select.append(cc == 0 ? "" : ", ");
      select.append(tableName).append(".").append(column).append(" as ").append(tableName).append("_").append(column);
    }
    return select.toString();
  }
  
  public static String inClause(Iterable<?> params) {
    final StringBuilder sql = new StringBuilder(" in (");
    for (Iterator<?> it = params.iterator(); it.hasNext(); ) {
      it.next();
      sql.append("?").append(it.hasNext() ? ", " : "");
    }
    sql.append(")");
    return sql.toString();
  }

  public static <T> Iterable<T> iterate(final Criteria c, Class<T> type) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new AbstractIterator<T>() {

          final ScrollableResults results = c.scroll(ScrollMode.FORWARD_ONLY);

          @Override
          @SuppressWarnings("unchecked")
          protected T computeNext() {
            return (results.next() ? (T) results.get()[0]: endOfData());
          }
        };
      }
    };
  }
}
