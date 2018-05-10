import { identity } from 'lodash';

const status = (response) => {
  if (response.status >= 200 && response.status < 300) {
    return Promise.resolve(json(response));
  } else {
    return Promise.reject(json(response));
  }
};

const json = (response) => {
  if (response.status === 204) { // 204 means "No content"
    return {};
  }
  return response.json();
};

export default function xhr(url, params = {}) {
  if (url.indexOf('://') === -1) {
    url = process.env.API_URL + url;
  }
  params = {
    headers: {'Content-Type': 'application/json'},
    credentials: 'same-origin',
    method: 'GET',
    ...params
  };
  return fetch(url, params).then(status, e => Promise.reject(e));
}
