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

import DataContainer from './DataContainer';

export default function withClientActivity(WrappedComponent) {
  return class WithClientActivityContainer extends DataContainer {
    constructor(props) {
      super(props, WrappedComponent);
    }

    getApiEndpoint(props) {
      return `/api/clients/${props.match.params.name}/activity?tail=14`;
    }

    stateToProps(state) {
      return { activity: state.data };
    }
  }
}
