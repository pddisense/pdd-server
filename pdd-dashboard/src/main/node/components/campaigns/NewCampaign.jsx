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
