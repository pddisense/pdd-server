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
import { Intent } from '@blueprintjs/core';

import { getClient, setClient } from '../browser/storage';
import SettingsSection from './SettingsSection';
import xhr from '../util/xhr';
import toaster from './toaster';

export default class SettingsSectionContainer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: {},
    };
  }

  @autobind
  handleChange(client) {
    let p;
    if (this.state.data.name) {
      p = xhr(
        `/api/clients/${this.state.data.name}`,
        { method: 'PATCH', body: JSON.stringify(client) }
      ).then(
        () => {
          toaster.show({ message: 'The settings have been updated.', intent: Intent.SUCCESS });
          return Promise.resolve();
        },
        (reason) => {
          toaster.show({ message: 'There was an error while updating the settings.', intent: Intent.DANGER });
          return Promise.reject(reason);
        }
      );
    } else {
      // It means that the client is not (yet) registered against the server.
      p = Promise.resolve();
    }
    p.then(
      () => setClient({ ...this.state.data, ...client }),
      () => console.log('Cannot contact the server, changes are discarded.'),
    );
  }

  componentDidMount() {
    getClient().then(data => this.setState({ data }));
  }

  render() {
    return <SettingsSection client={this.state.data} onChange={this.handleChange}/>;
  }
}