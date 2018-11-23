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

import { findIndex, isEqual, sum } from 'lodash';

/**
 * Aggregate the complete search history according to a given vocabulary.
 *
 * @param history Search history.
 * @param vocabulary Monitored vocabulary.
 * @param blacklist Blacklisted vocabulary.
 * @returns int[]
 */
export function aggregateCounters(history, vocabulary, blacklist = {}) {
  // The first counter is always the total number of searches performed across the period, whether
  // or not they are actually monitored. Then there is one counter per query in the vocabulary
  // (even if no search was performed for that query).
  const counters = Array(vocabulary.queries.length + 1);
  counters.fill(0);
  counters[ 0 ] = sum(history.map(search => search.count));

  // Compute the identifiers of blacklisted queries inside the vocabulary.
  const blacklisted = [];
  vocabulary.queries.forEach((query, idx) => {
    if (findIndex(blacklist.queries, q => isEqual(q, query)) > -1) {
      blacklisted.push(idx);
    }
  });

  // Each search inside the history may correspond to multiple monitored queries. For each search,
  // we find the identifiers of the associated query(ies) and update the appropriate counters.
  vocabulary.queries.forEach((query, idx) => {
    history.forEach(search => {
      if (blacklisted.indexOf(idx) > -1) {
        // If query is blacklisted, never take it into account.
        return;
      }
      if (query.exact) {
        if (search.query === query.exact) {
          counters[ idx + 1 ] += search.count;
        }
      } else if (query.terms) {
        // TODO: tokenize to handle quotes.
        const keywords = search.query.split(' ').map(s => s.trim());
        if (query.terms.every(v => keywords.indexOf(v) > -1)) {
          counters[ idx + 1 ] += search.count;
        }
      }
    });
  });
  return counters;
}

export function formatQuery(query) {
  if (query.exact) {
    return query.exact;
  } else if (query.terms) {
    return query.terms.join(', ');
  } else {
    return '–';
  }
}
