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

import Raven from 'raven-js';
import moment from 'moment';
import { sum } from 'lodash';
import jstz from 'jstz';

import { getData, setData } from './browser/storage';
import { searchHistory } from './browser/history';
import xhr from './util/xhr';
import { encryptCounters, generateKeyPair } from './protocol/crypto';
import sendPing from './protocol/ping';

// Configure Sentry reporting. The environment variables are provided at build time.
Raven.config(process.env.SENTRY_DSN, { environment: process.env.NODE_ENV }).install();

// Open the options page just after the extension has been installed.
chrome.runtime.onInstalled.addListener((details) => {
  if (details.reason === chrome.runtime.OnInstalledReason.INSTALL) {
    chrome.runtime.openOptionsPage();
  }
});

// Open the options page when the browser button is clicked.
chrome.browserAction.onClicked.addListener(() => {
  chrome.runtime.openOptionsPage();
});

// If the client is not registered, register it. It should normally happen only once, after the
// extension has been installed, but it may exceptionally happen again, e.g., if the local storage
// has been emptied. We do not store the result in a local variable, as the local cache may
// potentially be wiped out at any time, and will instead try that again at every ping.
getOrRegisterClient().catch(reason => {
  console.log('Unexpected error while registering the client');
  console.error(reason);
});

// At the heart of the extension, there is the "ping" feature. Periodically (typically once a day),
// the API server will be contacted for instructions about what to do. The extension will then
// process those commands and send the server the required information.
chrome.alarms.onAlarm.addListener(alarm => {
  if ('ping' === alarm.name) {
    getOrRegisterClient()
      .then(client => ping(client))
      .then(
        nextPingTime => {
          chrome.alarms.create('ping', { when: nextPingTime.valueOf() });
          console.log(`Next ping will be on ${nextPingTime.format()}`);
        },
        reason => {
          // There has been an unhandled error, and it can be anything...
          console.log('Unexpected error while pinging the server');
          console.error(reason);

          // Schedule a ping during the next hour to try recovering. It is important to do so when
          // an error occurs, otherwise the extension will simply stop sending data.
          const nextPingTime = moment().add(30, 'minutes').add((Math.random() * 30) | 0, 'minutes');
          chrome.alarms.create('ping', { when: nextPingTime.valueOf() });
        }
      );
  }
});

// We schedule a first ping in 1 minute. Normally, scheduling it the next day would be sufficient,
// but for testing purposes the duration of a "day" may be reduced at first. So we prefer to do a
// first useless ping query, that will give us the next ping time.
chrome.alarms.create('ping', { when: moment().add(1, 'minute').valueOf() });

/**
 * Contact the API server to get instructions, and perform them.
 *
 * @param client Current client.
 */
function ping(client) {
  return sendPing(client)
    .catch(reason => {
      if (reason.httpStatus === 404) {
        // A 404 means that the client is not registered, or more likely has been deleted by the
        // server (either manually or due to inactivity). We need to re-register.
        console.log('Re-registering the client...');
        return getData().then(registerClient).then(client => ping(client));
      } else {
        return Promise.reject(reason);
      }
    });
}

/**
 * Return the currently registered client, or register a new client it if no one was found.
 *
 * @returns PromiseLike<Client>
 */
function getOrRegisterClient() {
  return getData().then(client => {
    // If the client is registered, it has a name property.
    if (client.name) {
      return client;
    } else {
      console.log('Registering the client...');
      return registerClient(client);
    }
  });
}

/**
 * Register a new client.
 *
 * @param data Client data in the storage (some fields might be already defined, e.g., `externalName`).
 * @returns PromiseLike<Client>
 */
function registerClient(data) {
  const keyPair = generateKeyPair();
  const attrs = {
    ...data,
    publicKey: keyPair.publicKey,
    browser: 'chrome',
  };
  return xhr('/api/clients', { method: 'POST', body: JSON.stringify(attrs) })
    .then(created => setData({
      keyPair,
      name: created.name,
      createTime: created.createTime,
      browser: created.browser,
      externalName: created.externalName,
    }))
    .then(client => {
      console.log(`Registered as client ${client.name}`);
      return client;
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
