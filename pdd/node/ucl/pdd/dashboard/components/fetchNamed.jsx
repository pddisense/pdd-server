import React from 'react';
import { connect } from 'react-redux';
import { Spinner, NonIdealState } from '@blueprintjs/core';
import { fetchNamedAction } from '../actions';

function getName(props) {
  return props.match.params.name;
}

function isSameNamed(props, nextProps) {
  return getName(nextProps) === getName(props);
}

export default function fetchNamed(pluralName) {
  function dispatchAction(dispatch, props) {
    dispatch(fetchNamedAction(getName(props), pluralName));
  }

  function mapStateToProps(state, ownProps) {
    const name = getName(ownProps);
    const entities = state.entities[pluralName];
    if (name in entities.fetchStatus) {
      return {
        ...entities.fetchStatus[name],
        item: entities.entities[name],
      };
    }
    return {
      isLoaded: false,
      isLoading: false,
      lastError: null,
    };
  }

  return function fetchNamedWrapper(WrappedComponent) {
    class FetchNamedContainer extends React.Component {
      componentDidMount() {
        dispatchAction(this.props.dispatch, this.props);
      }

      componentWillReceiveProps(nextProps) {
        if (!isSameNamed(this.props, nextProps)) {
          dispatchAction(this.props.dispatch, nextProps);
        }
      }

      render() {
        if (this.props.isLoaded) {
          return <WrappedComponent {...this.props} />;
        } else if (this.props.isLoading) {
          return <Spinner/>;
        } else if (this.props.lastError !== null) {
          const title = (this.props.lastError.code === 404)
            ? 'Resource not found.'
            : 'An error occurred while loading this resource.';
          return <NonIdealState visual="error" title={title} />;
        }
        return null;
      }
    }
    return connect(mapStateToProps)(FetchNamedContainer);
  };
}
