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
import { Callout, Intent } from '@blueprintjs/core';

import Tabs from './Tabs';
import Title from './Title';
import ResultTableContainer from './ResultTableContainer';

class ViewResults extends React.Component {
  render() {
    return (
      <div>
        <Title campaign={this.props.campaign}/>

        <Tabs campaign={this.props.campaign}/>

        {this.props.campaign.started
          ? <ResultTableContainer campaign={this.props.campaign}/>
          : <Callout intent={Intent.DANGER}>
            This campaign has not yet started. Results will be available once the campaign will be
            running.
          </Callout>}
      </div>
    );
  }
}

ViewResults.propTypes = {
  campaign: PropTypes.object.isRequired,
};

export default ViewResults;
