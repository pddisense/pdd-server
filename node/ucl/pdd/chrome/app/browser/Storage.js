// @flow

import type { KeyPair } from '../crypto';

export type Tracer = {
  keyPair: KeyPair,
  namespace: string,
  clientName: string,
  campaignName: string,
  joinTime: string,
  vocabulary: Array<any>,
  activeQueries: Array<boolean>,
};

type Storage = { [key: string]: Tracer };

// Key under which the data is actually stored inside Chrome local storage.
const KEY = 'tracers';

// In-memory cache of the data.
let localCache: Storage = {};

function save(cache: Storage): Promise<void> {
  return new Promise((resolve, reject) => {
    chrome.storage.local.set({ [KEY]: JSON.stringify(Object.values(cache)) }, () => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        resolve();
      }
    });
  });
}

export default {
  getAll: function (): Array<mixed> {
    return Object.values(localCache);
  },

  get: function (key: string): ?Tracer {
    return (key in localCache) ? localCache[key] : null;
  },

  set: function (key: string, value: Tracer): Promise<void> {
    const cache = { ...localCache };
    if (key in cache) {
      cache[key] = { ...cache[key], ...value };
    } else {
      cache[key] = value;
    }
    return save(cache).then(() => {
      localCache = cache;
    });
  },

  remove: function (key: string): Promise<void> {
    const cache = { ...localCache };
    delete cache[key];
    return save(cache).then(() => {
      localCache = cache;
    });
  },

  clear: function (): Promise<void> {
    return save({}).then(() => {
      localCache = {};
    });
  },

  reload: function (): Promise<Storage> {
    return new Promise((resolve, reject) => {
      chrome.storage.local.get({ [KEY]: '[]' }, (items) => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          const cache = {};
          JSON.parse(items[KEY]).forEach(tracer => cache[tracer.campaignName] = tracer);
          localCache = cache;
          resolve(cache);
        }
      });
    });
  },
};
