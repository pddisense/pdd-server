import { map } from 'lodash';

import xhr from '../util/xhr';

const types = {};

export const FETCH_COLLECTION_START = 'FETCH_COLLECTION_START';
export const FETCH_COLLECTION_SUCCESS = 'FETCH_COLLECTION_SUCCESS';
export const FETCH_COLLECTION_ERROR = 'FETCH_COLLECTION_ERROR';

export const FETCH_NAMED_START = 'FETCH_NAMED_START';
export const FETCH_NAMED_SUCCESS = 'FETCH_NAMED_SUCCESS';
export const FETCH_NAMED_ERROR = 'FETCH_NAMED_ERROR';

export const UPDATE_START = 'UPDATE_START';
export const UPDATE_SUCCESS = 'UPDATE_SUCCESS';
export const UPDATE_ERROR = 'UPDATE_ERROR';

export const CREATE_START = 'CREATE_START';
export const CREATE_SUCCESS = 'CREATE_SUCCESS';
export const CREATE_ERROR = 'CREATE_ERROR';

export function getActionTypes(pluralName) {
  if (!(pluralName in types)) {
    const upperPluralName = pluralName.toUpperCase();
    const newTypes = {};

    newTypes[FETCH_COLLECTION_START] = `FETCH_${upperPluralName}_COLLECTION_START`;
    newTypes[FETCH_COLLECTION_SUCCESS] = `FETCH_${upperPluralName}_COLLECTION_SUCCESS`;
    newTypes[FETCH_COLLECTION_ERROR] = `FETCH_${upperPluralName}_COLLECTION_ERROR`;

    newTypes[FETCH_NAMED_START] = `FETCH_${upperPluralName}_NAMED_START`;
    newTypes[FETCH_NAMED_SUCCESS] = `FETCH_${upperPluralName}_NAMED_SUCCESS`;
    newTypes[FETCH_NAMED_ERROR] = `FETCH_${upperPluralName}_NAMED_ERROR`;

    newTypes[UPDATE_START] = `UPDATE_${upperPluralName}_START`;
    newTypes[UPDATE_SUCCESS] = `UPDATE_${upperPluralName}_SUCCESS`;
    newTypes[UPDATE_ERROR] = `UPDATE_${upperPluralName}_ERROR`;

    newTypes[CREATE_START] = `CREATE_${upperPluralName}_START`;
    newTypes[CREATE_SUCCESS] = `CREATE_${upperPluralName}_SUCCESS`;
    newTypes[CREATE_ERROR] = `CREATE_${upperPluralName}_ERROR`;

    types[pluralName] = newTypes;
  }
  return types[pluralName];
}

function buildAction(pluralName, type, payload = {}) {
  return {
    type: getActionTypes(pluralName)[type],
    ...payload,
  }
}


export function createAction(item, pluralName) {
  return (dispatch) => {
    dispatch(buildAction(pluralName, CREATE_START, { item }));
    return xhr(
      `/api/v1/namespaces/${item.metadata.namespace}/${pluralName}`,
      { method: 'POST', body: JSON.stringify(item) }
    ).then(
      json => dispatch(buildAction(pluralName, CREATE_SUCCESS, { item: json })),
      error => dispatch(buildAction(pluralName, CREATE_ERROR, {
        namespace: item.metadata.namespace,
        name: item.metadata.name,
        error
      }))
    );
  }
}

export function updateAction(item, pluralName) {
  return (dispatch) => {
    dispatch(buildAction(pluralName, UPDATE_START, { item }));
    return xhr(
      `/api/v1/namespaces/${item.metadata.namespace}/${pluralName}/${item.metadata.name}`,
      { method: 'PUT', body: JSON.stringify(item) }
    ).then(
      json => dispatch(buildAction(pluralName, UPDATE_SUCCESS, { item: json })),
      error => dispatch(buildAction(pluralName, UPDATE_ERROR, {
        namespace: item.metadata.namespace,
        name: item.metadata.name,
        error
      }))
    );
  }
}

export function fetchNamedAction(namespace, name, pluralName) {
  return (dispatch) => {
    dispatch(buildAction(pluralName, FETCH_NAMED_START, { namespace, name }));
    return xhr(`/api/v1/namespaces/${namespace}/${pluralName}/${name}`)
      .then(
        item => dispatch(buildAction(pluralName, FETCH_NAMED_SUCCESS, { item })),
        error => dispatch(buildAction(pluralName, FETCH_NAMED_ERROR, { namespace, name, error }))
      );
  }
}

export function fetchCollectionAction(namespace, pluralName, params = {}) {
  let url = `/api/v1/namespaces/${namespace}/${pluralName}?`;
  if (params.fieldSelector) {
    url += 'fieldSelector=' + encodeURIComponent(map(params.fieldSelector, (v, k) => `${k}=${v}`).join(',')) + '&';
  }
  if (params.labelSelector) {
    url += 'labelSelector=' + encodeURIComponent(map(params.labelSelector, (v, k) => `${k}=${v}`).join(',')) + '&';
  }
  return (dispatch) => {
    dispatch(buildAction(pluralName, FETCH_COLLECTION_START, { namespace, ...params }));
    return xhr(url)
      .then(
        results => dispatch(buildAction(pluralName, FETCH_COLLECTION_SUCCESS, { ...results })),
        error => dispatch(buildAction(pluralName, FETCH_COLLECTION_ERROR, { namespace, error }))
      );
  }
}
