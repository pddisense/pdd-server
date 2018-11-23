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

// Preliminary note: The history API provides two types of objects: HistoryItem, which represents a
// URL, and VisitItem, which represents a visit to a particular URL (i.e., a HistoryItem).
// HistoryItem's come with the total number of visits and the last time the URL was visited.

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
    // The workflow is the following:
    // 1) We retrieve a list of HistoryItem's that "look like" Google searches;
    // 2) Because this is a fuzzy free-text search, we filter them to ensure they are indeed Google
    // searches;
    // 3) We retrieve the VisitItem's associated with each HistoryItem and count those falling
    // within the window of interest.

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
      // clear whether the range restriction was applied on visits or on the HistoryItem's last
      // visit time. According to the implementation (in June 2018) the range restriction is
      // indeed applied on individual visits:
      // https://github.com/chromium/chromium/blob/e0a597faf7cdc0b1a909545c19235e947d44fc66/components/history/core/browser/history_backend.cc#L1300
      //
      // As a side not, according to Mozilla's documentation, the opposite choice was made in Firefox,
      // although the API comes with the same interface.
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
        // Because the above free-text filter does not allow for a precise filtering, we need to
        // perform a second pass to ensure that the returned results actually correspond to a
        // Google search (we may have false positives otherwise).
        const url = new URL(item.url, true);
        if (!url.host.startsWith('www.google.') || url.pathname !== '/search' || !url.query.q) {
          completed++;
          return;
        }

        // The `start` query string parameter is filled when the user navigates across Google's
        // search results pages. We ignore such pages and do not count them as a new search.
        if (url.query.start) {
          completed++;
          return;
        }

        // Although `startTime` and `endTime` filters do apply on individual visits (cf. the note
        // above), the `visitCount` field of each history item is immutable and still contains the
        // total number of visits of all time (and hence ignores the time range restriction). This
        // is why we have to retrieve for every history item all the associated visits, filter them
        // by time (the API does not provide a way to do it) and count them.
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

          // The actual query is embedded into the `q` query string parameter, but there are other
          // possible parameters. Therefore, different Google URLs may in fact contain the same
          // query, so we take care of merging those identical queries.
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
