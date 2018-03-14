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

const status = (response) => {
  if (response.status >= 200 && response.status < 300) {
    return Promise.resolve(json(response));
  } else if (response.status === 500 || response.status === 404 || response.status === 204) {
    return Promise.reject({});
  } else {
    return Promise.reject(json(response));
  }
};

const json = (response) => response.json();

export default function xhr(url, params = {}) {
  params = {
    headers: {'Content-Type': 'application/json'},
    credentials: 'same-origin',
    method: 'GET',
    ...params
  };
  return fetch(url, params).then(status, e => Promise.reject(e));
}
