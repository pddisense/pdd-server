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

import Tabs from './Tabs';

class ViewCampaign extends React.Component {
  render() {
    const { campaign } = this.props;
    return (
      <div>
        <h2>{campaign.displayName}</h2>

        <Tabs campaign={campaign} />

        <div className="attr-row">
          <div className="attr-name">E-mail addresse(s)</div>
          <div className="attr-value">{campaign.email.join(', ')}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Start date</div>
          <div className="attr-value">{campaign.startTime ? new Date(campaign.startTime).toLocaleDateString() : '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">End date</div>
          <div className="attr-value">{campaign.endTime ? new Date(campaign.endTime).toLocaleDateString() : campaign.startTime ? 'never' : '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Vocabulary size</div>
          <div className="attr-value">{campaign.vocabulary.queries.length} word{campaign.vocabulary.queries.length > 1 ? 's' : ''}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Delay</div>
          <div className="attr-value">
            {campaign.delay} day{campaign.delay > 1 ? 's' : ''} (+{campaign.graceDelay} day{campaign.graceDelay > 1 ? 's' : ''})
          </div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Sampling rate</div>
          <div className="attr-value">{campaign.samplingRate}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Collect raw data</div>
          <div className="attr-value">{campaign.collectRaw ? 'yes' : 'no'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Collect encrypted data</div>
          <div className="attr-value">{campaign.collectEncrypted ? 'yes' : 'no'}</div>
        </div>

        {campaign.collectEncrypted ?
          <div className="attr-row">
            <div className="attr-name">Group size</div>
            <div className="attr-value">{campaign.groupSize}</div>
          </div>: null}
      </div>
    );
  }
}

ViewCampaign.propTypes = {
  campaign: PropTypes.object.isRequired,
};

export default ViewCampaign;
