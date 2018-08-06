/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

import { sum } from 'lodash';

/**
 * Aggregate the complete search history according to a given vocabulary.
 *
 * @param history Search history.
 * @param vocabulary Monitored vocabulary.
 * @returns int[]
 */
export function aggregateHistory(history, vocabulary) {
  // The first counter is always the total number of searches performed across the period, whether
  // or not they are actually monitored. Then there is one counter per query in the vocabulary
  // (even if no search was performed for that query).
  const counters = Array(vocabulary.queries.length + 1);
  counters.fill(0);
  counters[0] = sum(history.map(search => search.count));
  history.forEach(search => {
    const indices = findIndices(search.query, vocabulary);
    indices.forEach(idx => counters[idx + 1] += search.count);
  });
  return counters;
}

function findIndices(q, vocabulary) {
  return vocabulary.queries.map((query, idx) => {
    if (query.exact) {
      return q === query.exact ? idx : -1;
    } else if (query.terms) {
      // TODO: tokenize to handle quotes.
      const keywords = q.split(' ').map(s => s.trim());
      return query.terms.every(v => keywords.indexOf(v) > -1) ? idx : -1;
    }
    return -1;
  }).filter(idx => -1 !== idx);
}
