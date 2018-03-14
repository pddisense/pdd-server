/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import URL from 'url-parse';
import moment from 'moment';

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
