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
