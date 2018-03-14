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

import Raven from 'raven-js';
import moment from 'moment';

import { history, storage } from './browser';
import xhr from './util/xhr';
import { encryptCounters, generateKeyPair } from './crypto';

import type { Client, SubmitSketchCommand } from './types';

// Configure Sentry reporting. The environment variables are provided at build time.
Raven.config(process.env.SENTRY_DSN, { environment: process.env.NODE_ENV }).install();

const API_URL = process.env.API_URL || 'https://api.ppd.cs.ucl.ac.uk';

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
getOrRegisterClient();

// At the heart of the extension, there is the "ping" feature. Periodically (typically once a day),
// the API server will be contacted for instructions about what to do. The extension will then
// process those commands and send the server the required information.
chrome.alarms.onAlarm.addListener(alarm => {
  if ('ping' === alarm.name) {
    getOrRegisterClient().then(client => ping(client));
  }
});
chrome.alarms.create('ping', { when: nextDay().valueOf() });

function ping(client) {
  console.log(`Pinging the server for client ${client.name}...`);
  xhr(`${API_URL}/api/clients/${client.name}/ping`)
    .then(resp => {
      // Submit each sketch that was requested.
      resp.submit.forEach(command => submitSketch(client, command));

      // Schedule next ping time. Normally, the response comes with a suggested time. If for any
      // reason it is not present, we still schedule one for the next day (otherwise the extension
      // will simply stop sending data).
      const nextPingTime = resp.nextPingTime ? moment(resp.nextPingTime) : nextDay();
      chrome.alarms.create('ping', { when: nextPingTime.valueOf() });
    });
}

function getOrRegisterClient(): Promise<Client> {
  return storage.getClient().then(client => {
    return client || registerClient();
  });
}

function registerClient(): Promise<Client> {
  console.log('Registering client...');
  const keyPair = generateKeyPair();
  const attrs = {
    publicKey: keyPair.publicKey,
    browser: 'chrome',
    externalName: null,
  };
  return xhr(`${API_URL}/api/clients`, { method: 'POST', body: JSON.stringify(attrs) })
    .then(client => storage.setClient({
      keyPair,
      name: client.name,
      createTime: client.createTime,
      browser: client.browser,
      externalName: client.externalName,
    }))
    .then(client => {
      console.log(`Registered as client ${client.name}`);
      return client;
    });
}

function submitSketch(client: Client, command: SubmitSketchCommand): Promise<void> {
  const startTime = moment(command.startTime).valueOf();
  const endTime = moment(command.startTime).valueOf();
  return history
    .aggregate(startTime, endTime, command.vocabulary)
    .then(rawValues => {
      const encryptedValues = command.collectEncrypted
        ? encryptCounters(command.publicKeys, command.round, client.keyPair, rawValues)
        : [];
      const sketch = {
        name: command.sketchName,
        submitTime: moment().toISOString(),
        rawValues,
        encryptedValues,
      };
      return xhr(
        `${API_URL}/api/sketches/${command.sketchName}`,
        { method: 'PUT', body: JSON.stringify(sketch) }
      );
    });
}

function nextDay() {
  return moment().add(1, 'day').hours(1);
}
