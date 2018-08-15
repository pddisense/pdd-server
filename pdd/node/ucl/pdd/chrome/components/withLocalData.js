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
import { Intent } from '@blueprintjs/core';
import { keys, pick } from 'lodash';

import { getData, setData } from '../browser/storage';
import xhr from '../util/xhr';
import toaster from './toaster';

export default function withLocalData(WrappedComponent) {
  return class WithLocalDataContainer extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        data: {},
      };
    }

    componentDidMount() {
      getData().then(data => this.setState({ data }));
    }

    handleChange(data) {
      const patch = pick(data, [ 'externalName' ]);
      patchClient(this.state.data.name, patch).then(
        () => setData(data),
        () => {
          console.log('Cannot contact the server, changes are discarded.');
          return {};
        }
      ).then(data => this.setState({ data }));
    }

    render() {
      return <WrappedComponent localData={this.state.data}
                               onChange={data => this.handleChange(data)}/>;
    }
  };
}

/**
 * Send a request to the server, if needed, to update the client's metadata.
 *
 * @param clientName Client name.
 * @param data Client Metadata.
 * @return Promise<object>
 */
function patchClient(clientName, data) {
  if (!clientName) {
    // The client is not (yet) registered with the server.
    return Promise.resolve();
  }
  if (keys(data).length === 0) {
    // There is nothing to patch.
    return Promise.resolve();
  }
  return xhr(
    `/api/clients/${clientName}`,
    { method: 'PATCH', body: JSON.stringify(data) }
  ).then(
    () => {
      toaster.show({ message: 'The settings have been updated.', intent: Intent.SUCCESS });
      return Promise.resolve();
    },
    (reason) => {
      toaster.show({
        message: 'An error occurred while contacting the server.',
        intent: Intent.DANGER
      });
      return Promise.reject(reason);
    }
  );
}
