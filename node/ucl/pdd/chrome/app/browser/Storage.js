// @flow
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

import type { Client } from '../types';

// Key under which the data is actually stored inside Chrome local storage.
const CLIENT_KEY = 'client';

// In-memory cache of the data.
let loaded = false;
let localClient: ?Client = null;

function reload(): Promise<void> {
  if (loaded) {
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    chrome.storage.local.get({ [CLIENT_KEY]: '{}' }, (items) => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        let client = JSON.parse(items[CLIENT_KEY]);
        if (Object.keys(client).length > 0) {
          localClient = client;
        }
        loaded = true;
        resolve();
      }
    });
  });
}

function write(key: string, item: any): Promise<void> {
  return new Promise((resolve, reject) => {
    chrome.storage.local.set({ [key]: JSON.stringify(item) }, () => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        resolve();
      }
    });
  });
}

function getClient(): Promise<?Client> {
  return reload().then(() => localClient);
}

function setClient(client: Client): Promise<void> {
  return write(CLIENT_KEY, client).then(() => {
    localClient = client;
  });
}

export default {
  getClient,
  setClient,
};
