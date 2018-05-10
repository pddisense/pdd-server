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
import { noop } from 'lodash';
import { Intent, Callout } from '@blueprintjs/core';

import Tabs from './Tabs';
import Title from './Title';
import StrategyForm from './StrategyForm';

class EditStrategy extends React.Component {
  render() {
    return (
      <div>
        <Title campaign={this.props.campaign}/>

        <Tabs campaign={this.props.campaign}/>

        <p className="pt-ui-text-large" style={{ marginBottom: '25px' }}>
          This page allows to configure the collection strategy that is deployed by this campaign.
        </p>

        {this.props.campaign.completed ?
          <Callout intent={Intent.PRIMARY}>
            Because this campaign is completed, the strategy cannot be changed anymore.
          </Callout> :
          this.props.campaign.started ?
            <Callout intent={Intent.PRIMARY}>
              Because this campaign is running, some properties cannot be changed anymore.
            </Callout> : null}

        <StrategyForm {...this.props} />
      </div>
    );
  }
}

EditStrategy.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
  errors: PropTypes.array.isRequired,
  campaign: PropTypes.object.isRequired,
};

export default EditStrategy;
