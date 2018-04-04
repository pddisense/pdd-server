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
import {noop} from 'lodash';

import CampaignForm from './CampaignForm';

class NewCampaign extends React.Component {
  render() {
    const campaign = {
      displayName: 'Untitled campaign',
    };
    return (
      <div>
        <h2>New campaign</h2>
        <CampaignForm campaign={campaign} onSubmit={this.props.onSubmit} />
      </div>
    );
  }
}

NewCampaign.propTypes = {
  onSubmit: PropTypes.func,
};

NewCampaign.defaultProps = {
  onSubmit: noop,
};

export default NewCampaign;
