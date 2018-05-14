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
import moment from 'moment';
import { Alert, Button, Intent } from '@blueprintjs/core';
import { CopyToClipboard } from 'react-copy-to-clipboard';

import withUpdateCampaign from '../hoc/withUpdateCampaign';
import xhr from '../../util/xhr';

function download(url, contentType, filename) {
  const anchor = document.createElement('a');
  xhr(url, { headers: { Accept: contentType }, blob: true })
    .then(blob => {
      const objectUrl = window.URL.createObjectURL(blob);
      anchor.href = objectUrl;
      anchor.download = filename;
      anchor.click();
      window.URL.revokeObjectURL(objectUrl);
    });
}

@withUpdateCampaign({ redirect: false })
class Title extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showDeleteAlert: false,
      showStartAlert: false,
      showStopAlert: false,
      showCopyButton: false,
    };
  }

  handleDownloadClick() {
    const url = `/api/campaigns/${this.props.campaign.name}/results?export=1`;
    const filename = `${this.props.campaign.name}.csv`;
    download(url, 'application/csv', filename);
  }

  handleToggleCopyButton(shown) {
    this.setState({ showCopyButton: shown });
  }

  handleToggleDeleteAlert(shown) {
    this.setState({ showDeleteAlert: shown });
  }

  handleToggleStartAlert(shown) {
    this.setState({ showStartAlert: shown });
  }

  handleToggleStopAlert(shown) {
    this.setState({ showStopAlert: shown });
  }

  handleDeleteConfirm() {
    this.setState({ showDeleteAlert: false });
    this.props.onDelete();
  }

  handleStartConfirm() {
    this.setState({ showStartAlert: false });
    const obj = { ...this.props.campaign, startTime: moment().format() };
    this.props.onSubmit(obj);
  }

  handleStopConfirm() {
    this.setState({ showStartAlert: false });
    const obj = { ...this.props.campaign, endTime: moment().format() };
    this.props.onSubmit(obj);
  }

  render() {
    const { campaign } = this.props;
    return (
      <div>
        <div className="actions">
          {campaign.started ?
            <Button intent={Intent.PRIMARY}
                    onClick={() => this.handleDownloadClick()}
                    icon="download">
              Download results as CSV
            </Button> : null}

          {campaign.completed ?
            <Button onClick={() => this.handleToggleDeleteAlert(true)} icon="delete">
              Delete campaign
            </Button> :
            campaign.started ?
              <Button onClick={() => this.handleToggleStopAlert(true)} icon="stop">
                Stop collection
              </Button> :
              <Button intent={Intent.PRIMARY}
                      onClick={() => this.handleToggleStartAlert(true)}
                      icon="play">
                Start collection
              </Button>}
        </div>

        <h2 onMouseEnter={() => this.handleToggleCopyButton(true)}
            onMouseLeave={() => this.handleToggleCopyButton(false)}>
          {this.props.campaign.displayName}
          {this.state.showCopyButton ?
            <CopyToClipboard text={campaign.name}>
              <Button icon="clipboard" small={true} style={{ marginLeft: '10px' }}/>
            </CopyToClipboard> : null}
        </h2>

        <Alert isOpen={this.state.showDeleteAlert}
               cancelButtonText="Cancel"
               intent={Intent.PRIMARY}
               onCancel={() => this.handleToggleDeleteAlert(false)}
               onConfirm={() => this.handleDeleteConfirm()}>
          Are you sure you want to delete this campaign?
          This is permanent and cannot be undone, all associated results will be deleted as well.
        </Alert>

        <Alert isOpen={this.state.showStartAlert}
               cancelButtonText="Start"
               intent={Intent.PRIMARY}
               onCancel={() => this.handleToggleStartAlert(false)}
               onConfirm={() => this.handleStartConfirm()}>
          Are you sure you want to start this campaign?
          You will not be able to modify the campaign's strategy once it has started.
          Only the vocabulary can still be modified on a running campaign.
        </Alert>

        <Alert isOpen={this.state.showStopAlert}
               cancelButtonText="Stop"
               intent={Intent.PRIMARY}
               onCancel={() => this.handleToggleStopAlert(false)}
               onConfirm={() => this.handleStopConfirm()}>
          Are you sure you want to stop this campaign?
          The campaign cannot be reactivated once it has been stopped.
        </Alert>
      </div>
    );
  }
}

Title.propTypes = {
  campaign: PropTypes.object.isRequired,
};

export default Title;
