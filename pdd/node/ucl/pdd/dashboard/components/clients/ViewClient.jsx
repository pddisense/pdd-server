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
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import { Button, Alert, Intent } from '@blueprintjs/core';
import { CopyToClipboard } from 'react-copy-to-clipboard';

import ActivityPlot from './ActivityPlot';
import toaster from '../toaster';
import xhr from '../../util/xhr';

class ViewClient extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
    };
  }

  @autobind
  handleClick() {
    this.setState({ isOpen: true });
  }

  @autobind
  handleConfirm() {
    this.setState({ isOpen: false });
    xhr(`/api/clients/${this.props.client.name}`, { method: 'DELETE' })
      .then(() => {
        toaster.show({
          message: `Client "${this.props.client.name} has been deleted.`,
          intent: Intent.SUCCESS,
        });
        this.props.history.push(`/clients`);
      })
  }

  @autobind
  handleCancel() {
    this.setState({ isOpen: false });
  }

  render() {
    const { client, activity } = this.props;
    return (
      <div>
        <div className="actions">
          <CopyToClipboard text={client.name}>
            <Button text="Copy name to clipboard" icon="clipboard"/>
          </CopyToClipboard>
          <Button text="Delete client" icon="delete" onClick={this.handleClick}/>
        </div>

        <Alert onConfirm={this.handleConfirm}
               intent={Intent.PRIMARY}
               confirmButtonText="Yes"
               cancelButtonText="Cancel"
               onCancel={this.handleCancel}
               isOpen={this.state.isOpen}>
          Are you sure you want to delete this client? It will not prevent it from sending further
          data (if doing so, it will automatically register again under a new name).
        </Alert>

        <h2>Client {client.name}</h2>

        <div className="attr-row">
          <div className="attr-name">Join time</div>
          <div className="attr-value">{new Date(client.createTime).toLocaleString()}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Browser</div>
          <div className="attr-value">{client.browser}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">External name</div>
          <div className="attr-value">{client.externalName || '–'}</div>
        </div>

        <ActivityPlot activity={activity}/>
      </div>
    );
  }
}

ViewClient.propTypes = {
  client: PropTypes.object.isRequired,
  activity: PropTypes.object.isRequired,
};

export default ViewClient;
