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
const DATA_KEY = 'client';

// In-memory cache of the data.
let loaded = false;
let localData = null;

function reload() {
  if (loaded) {
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    chrome.storage.local.get({ [DATA_KEY]: '{}' }, (items) => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        localData = JSON.parse(items[DATA_KEY]);
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
        localData = item;
        resolve(item);
      }
    });
  });
}

/**
 * Return data from the browser local storage.
 *
 * @returns PromiseLike<object>
 */
export function getData() {
  return reload().then(() => localData);
}

/**
 * Persist data to the browser local storage.
 *
 * @param data Data to persist. It must be an object, and can contain only the keys to update.
 * @returns PromiseLike<object>
 */
export function setData(data) {
  return reload().then(() => write(DATA_KEY, { ...localData, ...data }));
}
