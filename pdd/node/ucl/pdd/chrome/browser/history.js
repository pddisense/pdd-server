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
  return new Promise((resolve, reject) => {
    // Chrome documentation does not indicate that chrome.history.search may fail, so the promise
    // always resolves.
    const query = {
      // This is set to the maximum signed int32. Of course, we have a problem if the user hit
      // Google more than that many times. However, we do not expect this to happen in "normal"
      // usage conditions, and have anyway few available workarounds.
      maxResults: 2147483647,
      // Restrict to Google searches on any domain.
      text: 'https://www.google.*/search',
      startTime: moment(startTime).valueOf(),
      endTime: moment(endTime).valueOf(),
    };
    chrome.history.search(query, (items) => {
      const searches = {};
      items.forEach((item) => {
        const url = new URL(item.url, true);
        // Because the above `text` filter is only a plain text query, we need to ensure that
        // returned results actually correspond to a Google search. This is why we check again
        // the domain name and path.
        if (!url.host.startsWith('www.google.') || url.pathname !== '/search') {
          return;
        }
        if (url.query.q in searches) {
          const search = searches[url.query.q];
          search.count += item.visitCount;
          search.lastTime = Math.max(search.lastTime, item.lastVisitTime);
        } else {
          searches[url.query.q] = {
            query: url.query.q,
            lastTime: item.lastVisitTime,
            count: item.visitCount,
          };
        }
      });
      resolve(Object.values(searches));
    });
  });
}
