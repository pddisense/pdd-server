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

import xhr from './xhr';

export function checkAuthenticated() {
  return xhr('/auth').then(resp => resp.authenticated);
}

export function authenticate(password) {
  return xhr('/auth', { method: 'POST', body: JSON.stringify({ password }) }).then(resp => {
    if (resp.authenticated) {
      if (resp.accessToken) {
        window.localStorage.setItem('access_token', resp.accessToken);
      }
      return true;
    } else {
      return false;
    }
  });
}

export function logout() {
  window.localStorage.removeItem('access_token');
}
