import { map } from 'lodash';
import xhr from './xhr';

/**
 * HTTP client to communicate with a Colossus server.
 */
const HttpClient = {
  create: function (obj, pluralName, subresource = null) {
    let url = '/api/v1';
    if (obj.metadata.namespace) {
      url += `/namespaces/${obj.metadata.namespace}`;
    }
    url += `/${pluralName}`;
    if (null !== subresource) {
      url += `/${subresource}`
    }
    return xhr(url, { method: 'POST', body: JSON.stringify(obj) });
  },

  replace: function (obj, pluralName, subresource = null) {
    let url = '/api/v1';
    if (obj.metadata.namespace) {
      url += `/namespaces/${obj.metadata.namespace}`;
    }
    url += `/${pluralName}/${obj.metadata.name}`;
    if (null !== subresource) {
      url += `/${subresource}`
    }
    return xhr(url, { method: 'PUT', body: JSON.stringify(obj) });
  },

  list: function (pluralName, namespace, params = {}) {
    let url = '/api/v1';
    if (namespace) {
      url += `/namespaces/${namespace}`;
    }
    url += `/${pluralName}?`;
    if (params.fieldSelector) {
      url += 'fieldSelector=' + encodeURIComponent(map(params.fieldSelector, (v, k) => `${k}=${v}`).join(',')) + '&';
    }
    if (params.labelSelector) {
      url += 'labelSelector=' + encodeURIComponent(map(params.labelSelector, (v, k) => `${k}=${v}`).join(',')) + '&';
    }
    return xhr(url);
  },

  get: function (pluralName, namespace, name, subresource = null, params = {}) {
    let url = '/api/v1';
    if (namespace) {
      url += `/namespaces/${namespace}`;
    }
    url += `/${pluralName}/${name}`;
    if (null !== subresource) {
      url += `/${subresource}`
    }
    if (params) {
      url += '?' + map(params, (v, k) => `${k}=${encodeURIComponent(v)}`).join('&');
    }
    return xhr(url);
  },

  delete: function (pluralName, namespace, name, subresource = null) {
    let url = '/api/v1';
    if (namespace) {
      url += `/namespaces/${namespace}`;
    }
    url += `/${pluralName}/${name}`;
    if (null !== subresource) {
      url += `/${subresource}`
    }
    return xhr(url, { method: 'DELETE' });
  }
};

export default HttpClient;
