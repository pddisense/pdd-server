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
import PropTypes from 'prop-types';
import { Button, Alert, Intent } from '@blueprintjs/core';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import { countries } from 'country-data';

import ActivityPlot from './ActivityPlot';
import toaster from '../toaster';
import xhr from '../../util/xhr';

class ViewClient extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showDeleteAlert: false,
      showCopyButton: false,
    };
  }

  handleToggleDeleteAlert(shown) {
    this.setState({ showDeleteAlert: shown });
  }

  handleToggleCopyButton(shown) {
    this.setState({ showCopyButton: shown });
  }

  handleConfirm() {
    this.setState({ showDeleteAlert: false });
    xhr(`/api/clients/${this.props.client.name}`, { method: 'DELETE' })
      .then(() => {
        toaster.show({
          message: `Client "${this.props.client.name} has been deleted.`,
          intent: Intent.SUCCESS,
        });
        this.props.history.push(`/clients`);
      })
  }

  render() {
    const { client, activity } = this.props;
    const country = client.countryCode ? countries[client.countryCode] : null;
    return (
      <div>
        <div className="actions">
          <Button text="Delete client"
                  icon="delete"
                  onClick={() => this.handleToggleDeleteAlert(true)}/>
        </div>

        <h2 onMouseEnter={() => this.handleToggleCopyButton(true)}
            onMouseLeave={() => this.handleToggleCopyButton(false)}>
          Client {client.name}
          {this.state.showCopyButton ?
            <CopyToClipboard text={client.name}>
              <Button icon="clipboard" small={true} style={{ marginLeft: '10px' }}/>
            </CopyToClipboard> : null}
        </h2>

        <Alert onConfirm={() => this.handleConfirm()}
               intent={Intent.PRIMARY}
               confirmButtonText="Yes"
               cancelButtonText="Cancel"
               onCancel={() => this.handleToggleDeleteAlert(false)}
               isOpen={this.state.showDeleteAlert}>
          Are you sure you want to delete this client? It will not prevent it from sending further
          data (if doing so, it will automatically register again under a new name).
        </Alert>

        <div className="attr-row">
          <div className="attr-name">Join time</div>
          <div className="attr-value">{new Date(client.createTime).toLocaleString()}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Browser</div>
          <div className="attr-value">
            {client.browser}
            {client.extensionVersion ? ` (extension v${client.extensionVersion})` : null}
          </div>
        </div>

        <div className="attr-row">
          <div className="attr-name">External name</div>
          <div className="attr-value">{client.externalName || '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Last known timezone</div>
          <div className="attr-value">{client.timezone || '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Last known country</div>
          <div className="attr-value">{country ? `${country.emoji} ${country.name}` : '–'}</div>
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
