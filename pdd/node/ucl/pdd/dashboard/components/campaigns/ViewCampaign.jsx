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

import Tabs from './Tabs';
import Title from './Title';

class ViewCampaign extends React.Component {
  render() {
    const { campaign } = this.props;
    return (
      <div>
        <Title campaign={campaign} />

        <Tabs campaign={campaign} />

        <div className="attr-row">
          <div className="attr-name">Start date</div>
          <div className="attr-value">{campaign.startTime ? new Date(campaign.startTime).toLocaleDateString() : '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">End date</div>
          <div className="attr-value">{campaign.endTime ? new Date(campaign.endTime).toLocaleDateString() : campaign.startTime ? 'never' : '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">E-mail address</div>
          <div className="attr-value">{campaign.email || '–'}</div>
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
          <div className="attr-value">
            {campaign.samplingRate ? `${campaign.samplingRate * 100} %` : '–'}
          </div>
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
