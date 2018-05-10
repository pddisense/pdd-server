import React from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import { Toaster, Intent } from '@blueprintjs/core';
import { noop } from 'lodash';

import fetchNamed from './fetchNamed';
import { updateAction } from '../actions';

export default function updateNamed(pluralName) {
  function mapStateToProps(state, ownProps) {
    const name = ownProps.item.metadata.name;
    const entities = state.entities[pluralName];
    if (name in entities.mutateStatus) {
      return { ...entities.mutateStatus[name] };
    }
    return {
      isLoaded: false,
      isLoading: false,
      lastError: null,
    };
  }

  function mapDispatchToProps(dispatch) {
    return {
      onSubmit: (obj) => dispatch(updateAction(obj, pluralName)),
      onChange: (obj) => dispatch(updateAction(obj, pluralName)),
    };
  }

  return function updateNamedWrapper(WrappedComponent) {
    class UpdateNamedContainer extends React.Component {
      constructor(props) {
        super(props);
        this.refHandlers = {
          toaster: (ref) => this.toaster = ref,
        };
      }

      render() {
        if (this.props.isLoaded) {
          if ('onSubmit' in WrappedComponent.propTypes) {
            const to = `/${pluralName}/view/${this.props.item.metadata.namespace}/${this.props.item.metadata.name}`;
            return <Redirect to={to} push={true}/>;
          }
        } else {
          if (this.props.isLoading) {
            // TODO: display a warning if the user tries to leave the page.
          } else if (this.props.lastError !== null) {
            const message = this.props.lastError.message || 'An error occurred while saving this resource.';
            // TODO: not very good, as the render method is not pure anymore.
            this.toaster.show({ message, intent: Intent.DANGER });
          }
        }
        return (
          <div>
            <Toaster ref={this.refHandlers.toaster} />
            <WrappedComponent {...this.props} />
          </div>
        );
      }
    }
    return fetchNamed(pluralName)(connect(mapStateToProps, mapDispatchToProps)(UpdateNamedContainer));
  }
}
