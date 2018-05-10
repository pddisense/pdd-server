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

import CampaignForm from './CampaignForm';
import Title from './Title';
import Tabs from './Tabs';

class EditCampaign extends React.Component {
  render() {
    return (
      <div>
        <Title campaign={this.props.campaign}/>

        <Tabs campaign={this.props.campaign}/>

        <p className="pt-ui-text-large" style={{ marginBottom: '25px' }}>
          This page allows to edit the basic metadata of this campaign.
          The monitored vocabulary and the collection strategy can be modified in other tabs.
        </p>

        <CampaignForm {...this.props} />
      </div>
    );
  }
}

EditCampaign.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
  errors: PropTypes.array.isRequired,
  campaign: PropTypes.object.isRequired,
};

export default EditCampaign;
