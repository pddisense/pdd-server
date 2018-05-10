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

const API_URL = process.env.API_URL || 'https://api.ppd.cs.ucl.ac.uk';

function status(response) {
  if (response.status >= 200 && response.status < 300) {
    return json(response);
  } else {
    return json(response).then(resp => Promise.reject(resp));
  }
}

function json(response) {
  return response.json()
    .catch(() => {
      // This `catch` is here to handle the case where the content length is 0, in which case the
      // JSON deserialization fails with "Unexpected end of input". However, it seems impossible to
      // get the content length from the fetch response object (in particular the "Content-Length"
      // header is undefined). So we end up using this catch block, which is far from ideal as it
      // will also hide legitimate JSON deserialization errors...
      // Cf. https://stackoverflow.com/questions/48266678/how-to-get-the-content-length-of-the-response-from-a-request-with-fetch#comment83517087_48266842
      return {};
    }).then(data => {
      // We add the HTTP status code to help differentiate between errors.
      return { httpStatus: response.status, ...data };
    });
}

export default function xhr(url, params = {}) {
  params = {
    headers: { 'Content-Type': 'application/json' },
    method: 'GET',
    mode: 'cors',
    ...params,
  };
  return fetch(API_URL + url, params).then(status);
}
