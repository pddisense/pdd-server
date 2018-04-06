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

import React from 'react';
import autobind from 'autobind-decorator';
import { Spinner, NonIdealState } from '@blueprintjs/core';

import xhr from '../../util/xhr';

export default function withStats(WrappedComponent) {
  return class StatsContainer extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        isLoading: false,
        isLoaded: false,
        data: null,
      };
    }

    @autobind
    onSuccess(resp) {
      this.setState({ isLoading: false, isLoaded: true, data: resp });
    }

    @autobind
    onError(resp) {
      console.log('Unexpected error while fetching stats', resp);
      this.setState({ isLoading: false, isLoaded: true });
    }

    load() {
      this.setState({ isLoading: true });
      let url = '/api/stats';
      xhr(url).then(this.onSuccess, this.onError)
    }

    componentDidMount() {
      this.load();
    }

    componentWillReceiveProps() {
      this.load();
    }

    render() {
      if (this.state.isLoading) {
        return <Spinner/>;
      } else if (this.state.isLoaded && null !== this.state.data) {
        return <WrappedComponent stats={this.state.data}/>;
      } else if (this.state.isLoaded) {
        return <NonIdealState visual="error" title="An error occurred while loading stats."/>;
      }
      return null;
    }
  };
}
