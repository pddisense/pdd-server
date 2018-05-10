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
import { Button, Icon, Intent } from '@blueprintjs/core';
import { CopyToClipboard } from 'react-copy-to-clipboard';
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

class Title extends React.Component {
  handleDownload() {
    const url = `/api/campaigns/${this.props.campaign.name}/results?export=1`;
    const filename = `${this.props.campaign.name}.csv`;
    download(url, 'application/csv', filename);
  }

  render() {
    const { campaign } = this.props;
    let iconName;
    let iconTitle;
    if (campaign.completed) {
      // Finished.
      iconName = 'tick';
      iconTitle = 'Finished campaign';
    } else if (campaign.started) {
      // Running.
      iconName = 'repeat';
      iconTitle = 'Running campaign';
    } else if (campaign.startTime) {
      // Scheduled.
      iconName = 'time';
      iconTitle = 'Scheduled campaign';
    } else {
      // Pending.
      iconName = 'issue';
      iconTitle = 'Pending campaign';
    }
    return (
      <div>
        <div className="actions">
          {campaign.started ?
            <Button intent={Intent.PRIMARY} onClick={() => this.handleDownload()} icon="download">
              Download results as CSV
            </Button> : null}

          <CopyToClipboard text={campaign.name}>
            <Button text="Copy name to clipboard" icon="clipboard"/>
          </CopyToClipboard>
        </div>

        <h2>
          {this.props.campaign.displayName}

          <Icon icon={iconName}
                title={iconTitle}
                iconSize={20}
                style={{ position: 'relative', top: '6px', marginLeft: '15px' }}/>
        </h2>
      </div>
    );
  }
}

Title.propTypes = {
  campaign: PropTypes.object.isRequired,
};

export default Title;
