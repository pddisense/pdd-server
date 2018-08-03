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
import { getData, setData } from './browser/storage';
import xhr from './util/xhr';
import { generateKeyPair } from './protocol/crypto';
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

// We schedule a first ping in 30 seconds. We schedule one very soon after installing the extension
// because it will contain the latest vocabulary and accurate time for the next ping.
chrome.alarms.create('ping', { when: moment().add(30, 'seconds').valueOf() });

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
