import React from 'react';
import { connect } from 'react-redux';
import { Spinner, NonIdealState } from '@blueprintjs/core';
import { mapValues } from 'lodash';

import { fetchCollectionAction } from '../actions';

function replaceProps(selector, props) {
  return mapValues(selector, v => {
    if (typeof v === 'function') {
      return v(props);
    }
    return v;
  });
}

export default function fetchCollection(pluralName, opts = {}) {
  function dispatchAction(dispatch, props) {
    const params = {};
    if (opts.fieldSelector) {
      params.fieldSelector = replaceProps(opts.fieldSelector, props);
    }
    if (opts.labelSelector) {
      params.labelSelector = replaceProps(opts.labelSelector, props);
    }
    dispatch(fetchCollectionAction(pluralName, params));
  }

  function mapStateToProps(state) {
    return {
      isLoaded: state.entities[pluralName].isLoaded,
      isLoading: state.entities[pluralName].isLoading,
      lastError: state.entities[pluralName].lastError,
      items: Object.values(state.entities[pluralName].entities),
    };
  }

  return function fetchCollectionWrapper(WrappedComponent) {
    class FetchCollectionContainer extends React.Component {
      componentDidMount() {
        dispatchAction(this.props.dispatch, { ...this.props });
      }

      componentWillReceiveProps(nextProps) {
        // dispatchAction(this.props.dispatch, {namespace: 'default'});
      }

      render() {
        if (this.props.isLoaded) {
          return <WrappedComponent {...this.props} />;
        } else if (this.props.isLoading) {
          return <Spinner/>;
        } else if (this.props.lastError !== null) {
          return <NonIdealState
            visual="error"
            title="An error occurred while loading this resource."/>;
        }
        return null;
      }
    }

    return connect(mapStateToProps)(FetchCollectionContainer);
  }
}
