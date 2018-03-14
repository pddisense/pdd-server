import React from 'react';
import Raven from 'raven-js';
import {NonIdealState} from '@blueprintjs/core';

export default class RavenBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      error: null,
    };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({error});
    Raven.captureException(error, {extra: errorInfo});
  }

  render() {
    if (this.state.error) {
      // Render fallback UI.
      return <NonIdealState
        title="Something went wrong."
        visual="error"
        description="We are very sorry about this. Our team has been notified and is investigating on this issue."
      />;
    } else {
      // when there is no error, render children untouched.
      return this.props.children;
    }
  }
}
