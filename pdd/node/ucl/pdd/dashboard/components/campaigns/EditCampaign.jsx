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
