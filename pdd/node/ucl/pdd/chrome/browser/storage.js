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
