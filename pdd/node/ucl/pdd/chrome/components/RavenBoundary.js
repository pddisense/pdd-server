/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from 'react';
import Raven from 'raven-js';
import { NonIdealState } from '@blueprintjs/core';

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
