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

import URL from 'url-parse';
import moment from 'moment';

/**
 * Search across the browser history to extract the Google searches performed during a given period.
 *
 * @param startTime Start of the period of interest.
 * @param endTime End of the period of interest.
 * @returns PromiseLike<object[]>
 */
export function searchHistory(startTime, endTime) {
  // Ensure that the time range is formed of two moments.
  startTime = moment(startTime);
  endTime = moment(endTime);

  // Chrome documentation does not indicate that chrome.history.search may fail, so the promise
  // always resolves.
  return new Promise((resolve, reject) => {
    const query = {
      // This is set to the maximum signed int32. We have to override its default value otherwise
      // set to 100. We do have a problem if the user hit Google more than that many times, but it
      // should not happen in "normal" usage conditions.
      maxResults: 2147483647,

      // Restrict results to Google searches under any TLD. "*" seems to be used as a wildcard, but
      // this is a free-text search that is not well specified.
      text: 'https://www.google.*/search',

      // The `startTime` and `endTime` are used to retrieve only history items that were visited
      // at least once during a given period. The Chrome API is under-specified and is was not made
      // clear whether the range restriction was applied on individual visits or on the the
      // `lastVisitTime` of each history item. I checked the implementation and as of today (06/2018)
      // the range restriction is indeed applied on individual visits:
      // https://github.com/chromium/chromium/blob/e0a597faf7cdc0b1a909545c19235e947d44fc66/components/history/core/browser/history_backend.cc#L1300
      //
      // Note: According to Mozilla's documentation, the opposite choice was made, although the API
      // provides the same interface:
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/history/search
      startTime: startTime.valueOf(),
      endTime: endTime.valueOf(),
    };

    chrome.history.search(query, (items) => {
      // If there are no items found, resolve the promise now.
      if (items.length === 0) {
        resolve([]);
        return;
      }

      const searches = {};
      let completed = 0;
      items.forEach((item) => {
        // Because the above `text` filter behavior is not completed specified, we need to perform
        // a second pass to ensure that the returned results actually correspond to a Google search
        // (we may have false positives otherwise).
        const url = new URL(item.url, true);
        if (!url.host.startsWith('www.google.') || url.pathname !== '/search' || !url.query.q) {
          completed++;
          return;
        }

        // The `history.search` is quite tricky. Indeed, although `startTime` and `endTime` filters
        // do apply on individual visits (cf. the note above), the `visitCount` field of each
        // history item contains the total number of visits of all time (and hence ignores the
        // range restriction imposed by the `startTime` and `endTime` filters). This is why we have
        // to retrieve for every history item the associated visits, filter them by time (the API
        // does not provide a way to do it) and count them, in order to have an accurate visits
        // count for that specific history item.
        chrome.history.getVisits({ url: item.url }, (visits) => {
          const count = visits.filter(visit => {
            if (!visit.visitTime) {
              // `visitTime` is described as optional and may not always be filled, depending on
              // the associated transition.
              return false;
            }
            const visitTime = moment(visit.visitTime);
            return visitTime.isAfter(startTime) && visitTime.isBefore(endTime);
          }).length;

          // Different Google URLs may in fact contain the same query. We take care of merging
          // those identical queries.
          if (url.query.q in searches) {
            const search = searches[url.query.q];
            search.count += count;
            search.lastTime = Math.max(search.lastTime, item.lastVisitTime);
          } else {
            searches[url.query.q] = {
              query: url.query.q,
              lastTime: item.lastVisitTime,
              count: count,
            };
          }

          // The promise is only resolved once we have processed all history items.
          if (++completed === items.length) {
            resolve(Object.values(searches));
          }
        });
      });
    });
  });
}
