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

function status(response) {
  if (response.status >= 200 && response.status < 300) {
    return Promise.resolve(json(response));
  } else {
    return Promise.reject(json(response));
  }
}

function json(response) {
  return response.json().catch(() => {
    // This `catch` is here to handle the case where the content length is 0, in which case the
    // JSON deserialization fails with "Unexpected end of input". However, it seems impossible to
    // get the content length from the fetch response object (in particular the "Content-Length"
    // header is undefined). So we end up using this catch block, which is far from ideal as it
    // will also hide legitimate JSON deserialization errors...
    // Cf. https://stackoverflow.com/questions/48266678/how-to-get-the-content-length-of-the-response-from-a-request-with-fetch#comment83517087_48266842
    return {};
  });
}

export default function xhr(url, params = {}) {
  params = {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin',
    method: 'GET',
    ...params
  };
  return fetch(url, params).then(status, e => Promise.reject(e));
}
