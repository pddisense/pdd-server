//@flow
/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
 */

import URL from 'url-parse';
import moment from 'moment';

import type { Vocabulary, WebSearch } from '../types';

function findIndices(q: string, vocabulary: Vocabulary): Array<number> {
  return vocabulary.queries.map((query, idx) => {
    if (query.exact) {
      return q === query.exact ? idx : -1;
    } else if (query.terms) {
      // TODO: handle quotes.
      const keywords = q.split(' ').map(s => s.trim());
      return query.terms.every(v => keywords.indexOf(v) > -1) ? idx : -1;
    }
    return -1;
  }).filter(idx => -1 !== idx);
}

function search(startTime: number, endTime: number, vocabulary: ?Vocabulary = null): Promise<Array<WebSearch>> {
  return new Promise((resolve, reject) => {
    // Chrome documentation does not indicate that chrome.history.search may fail, so the promise
    // always resolves.
    const query = {
      // This is set to the maximum signed int32. Of course, we have a problem if the user hit
      // Google more than that many times. However, we do not expect this to happen in "normal"
      // usage conditions, and have anyway few available workarounds.
      maxResults: 2147483647,
      // Restrict to Google searches on any domain.
      text: 'https://www.google.*/search',
      startTime: moment(startTime).valueOf(),
      endTime: moment(endTime).valueOf(),
    };
    chrome.history.search(query, (items) => {
      const searches = items
        .map((item) => {
          const url = new URL(item.url, true);
          // Because the above `text` filter is only a plain text query, we need to ensure that
          // returned results actually correspond to a Google search. This is why we check again
          // the domain name and path.
          if (!url.host.startsWith('www.google.') || url.pathname !== '/search') {
            return {};
          }
          // Moreover, if a vocabulary to search against was provided, we validate that the query
          // matches this vocabulary.
          const indices = vocabulary ? findIndices(url.query.q, vocabulary) : [];
          if (vocabulary && indices.length === 0) {
            return {};
          }
          return {
            indices,
            query: url.query.q,
            lastTime: item.lastVisitTime,
            count: item.visitCount,
          };
        })
        .filter(item => {
          // Because arrays have no flatMap method, we return an empty object above if we want to
          // delete it, which we eventually do here.
          return Object.keys(item).length > 0
        });
      resolve(searches);
    });
  });
}

function aggregate(startTime: number, endTime: number, vocabulary: Vocabulary): Promise<Array<number>> {
  return search(startTime, endTime, vocabulary).then(searches => {
    const counters = [];
    counters.fill(0, 0, vocabulary.queries.length);
    searches.forEach(search => search.indices.forEach(idx => counters[idx] += 1));
    return counters;
  });
}

export default {
  search,
  aggregate,
};
