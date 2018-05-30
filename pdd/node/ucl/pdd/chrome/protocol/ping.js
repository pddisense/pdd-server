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

import moment from 'moment';
import { sum } from 'lodash';
import jstz from 'jstz';

import { searchHistory } from '../browser/history';
import xhr from '../util/xhr';
import { encryptCounters } from './crypto';

/**
 * Contact the API server to get instructions, and perform them.
 *
 * @param client Current client.
 */
export default function sendPing(client) {
  console.log(`Pinging the server for client ${client.name}...`);
  const timezone = jstz.determine();
  const obj = {
    timezone: timezone.name(),
    extensionVersion: chrome.runtime.getManifest().version,
  };
  return xhr(
    `/api/clients/${client.name}/ping`,
    { method: 'POST', body: JSON.stringify(obj) }
  ).then(
    resp => {
      // Submit each sketch that was requested.
      resp.submit.forEach(command => submitSketch(client, command));

      // Schedule next ping time. Normally, the response comes with a suggested time. If for any
      // reason it is not present, we still schedule one for the next day (otherwise the extension
      // will simply stop sending data).
      return resp.nextPingTime ? moment(resp.nextPingTime) : moment().add(1, 'day').hours(2);
    });
}

function submitSketch(client, command) {
  console.log(`Submitting sketch ${command.sketchName}...`);
  const startTime = moment(command.startTime);
  const endTime = moment(command.endTime);

  return searchHistory(startTime, endTime)
    .then(history => aggregateHistory(history, command.vocabulary))
    .then(rawValues => {
      const encryptedValues = command.collectEncrypted
        ? encryptCounters(command.publicKeys, command.round, client.keyPair, rawValues)
        : [];
      const sketch = { rawValues, encryptedValues };
      return xhr(
        `/api/sketches/${command.sketchName}`,
        { method: 'PATCH', body: JSON.stringify(sketch) }
      );
    });
}

/**
 * Aggregate the complete search history according to a given vocabulary.
 *
 * @param history Search history.
 * @param vocabulary Monitored vocabulary.
 * @returns int[]
 */
function aggregateHistory(history, vocabulary) {
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
