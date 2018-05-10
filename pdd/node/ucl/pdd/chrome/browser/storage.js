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

// Key under which the data is actually stored inside Chrome local storage.
const CLIENT_KEY = 'client';

// In-memory cache of the data.
let loaded = false;
let localClient = null;

function reload() {
  if (loaded) {
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    chrome.storage.local.get({ [CLIENT_KEY]: '{}' }, (items) => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        localClient = JSON.parse(items[CLIENT_KEY]);
        loaded = true;
        resolve();
      }
    });
  });
}

function write(key, item) {
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

/**
 *
 * @returns PromiseLike<object>
 */
export function getClient() {
  return reload().then(() => localClient);
}

/**
 *
 * @param client
 * @returns PromiseLike<object>
 */
export function setClient(client) {
  return write(CLIENT_KEY, client).then(() => {
    // console.log('Client data written to local storage', client);
    localClient = client;
    return client;
  });
}
